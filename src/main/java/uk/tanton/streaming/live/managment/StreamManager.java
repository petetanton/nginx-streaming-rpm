package uk.tanton.streaming.live.managment;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.dynamo.domain.AccountRecord;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
import uk.tanton.streaming.live.dynamo.domain.StreamRecord;
import uk.tanton.streaming.live.dynamo.domain.StreamStatus;
import uk.tanton.streaming.live.exception.NoSuchAccountException;
import uk.tanton.streaming.live.exception.NoSuchPublisherException;
import uk.tanton.streaming.live.exception.NoSuchStreamException;
import uk.tanton.streaming.live.exception.StreamException;
import uk.tanton.streaming.live.pasers.HLSManifest;
import uk.tanton.streaming.live.streams.Stream;
import uk.tanton.streaming.live.transcode.TranscodeRequest;
import uk.tanton.streaming.live.transcode.TranscodeSettings;

import javax.inject.Inject;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamManager {
    private static final Logger LOG = LogManager.getLogger(StreamManager.class);

    private final ScheduledExecutorService scheduledExecutorService;
    private final StreamDataConnector streamDataConnector;
    private final StreamChecker streamChecker;

    @Inject
    public StreamManager(
            CloseableHttpClient httpClient,
            AmazonSQS sqs,
            StreamAuthenticator streamAuthenticator,
            boolean shouldSendSqs,
            StreamDataConnector streamDataConnector
    ) {
        this.streamChecker = new StreamChecker(httpClient, sqs, streamAuthenticator);
        this.streamDataConnector = streamDataConnector;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);

        if (shouldSendSqs) {
            this.scheduledExecutorService.scheduleAtFixedRate(streamChecker, 0L, 1000L, TimeUnit.MILLISECONDS);
        }
    }

    public void addStreamAndMarkAsStarted(final Stream stream) throws StreamException, NoSuchStreamException, NoSuchAccountException, NoSuchPublisherException {
//        Mark stream as started in DB

        StreamRecord streamRecord = getStreamRecordFromStream(stream);
        AccountRecord account = this.streamDataConnector.getAccount(streamRecord.getAccountId());

        if (streamRecord.getAccountId() != account.getAccountId()) {
            throw new StreamException("Account not authorised to start stream");
        }


        if (streamRecord.getStreamStatus() == null || StreamStatus.canStart(streamRecord.getStreamStatus())) {
            streamRecord.setStreamStatus(StreamStatus.STARTED);
            streamRecord.setDateStarted(Date.from(Instant.now()));
            this.streamDataConnector.updateStreamRecord(streamRecord);
            this.streamChecker.addStream(stream);
        } else {
            throw new StreamException(String.format("Cannot start a stream in %s state", streamRecord.getStreamStatus()));
        }
    }

    public void markStreamAsFinished(final Stream stream) throws NoSuchPublisherException, NoSuchStreamException, StreamException {
        StreamRecord streamRecord = getStreamRecordFromStream(stream);
        if (StreamStatus.canStop(streamRecord.getStreamStatus())) {
            streamRecord.setStreamStatus(StreamStatus.FINISHED);
            streamRecord.setDateEnded(Date.from(Instant.now()));
            this.streamDataConnector.updateStreamRecord(streamRecord);
            this.streamChecker.removeStream(stream);
        } else {
            throw new StreamException(String.format("Cannot stop a stream in %s state", streamRecord.getStreamStatus()));
        }
    }

    private StreamRecord getStreamRecordFromStream(Stream stream) throws NoSuchPublisherException, NoSuchStreamException {
        Publisher publisher = this.streamDataConnector.getPublisher(stream.getUser());
        return this.streamDataConnector.getStreamRecord(stream.getName(), publisher.getAccountId());
    }


    class StreamChecker implements Runnable {
        private final List<Stream> streams;
        private final CloseableHttpClient httpClient;
        private final List<String> pathsSent;
        private final AmazonSQS sqs;
        private final Gson gson;
        private final StreamAuthenticator streamAuthenticator;

        public StreamChecker(final CloseableHttpClient httpClient, final AmazonSQS sqs, final StreamAuthenticator streamAuthenticator) {
            this.httpClient = httpClient;
            this.streamAuthenticator = streamAuthenticator;
            this.streams = new ArrayList<>();
            this.pathsSent = new ArrayList<>();
            this.sqs = sqs;
            this.gson = new GsonBuilder().create();
        }

        public void addStream(final Stream stream) {
            this.streams.add(stream);
        }

        public void removeStream(final Stream stream) {
            this.streams.remove(stream);
        }


        @Override
        public void run() {
            for (Stream stream : streams) {
                try {
                    final String uri = String.format("http://localhost:8080/hls/%s.m3u8", stream.getName());
                    final CloseableHttpResponse response = httpClient.execute(new HttpGet(uri));
                    final int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != 404) {
                        final String responseString = EntityUtils.toString(response.getEntity());
                        final HLSManifest hlsManifest = HLSManifest.parseFromString(responseString);

                        hlsManifest.getSegments().forEach(s -> {
                            final String url = generateUrlFromFilename(s);
                            if (!pathsSent.contains(url)) {
                                sendTranscodeRequest(stream, url);
                                pathsSent.add(url);
                            }
                        });
                    } else {
                        LOG.error(String.format("Got a %s response code when requesting %s", statusCode, uri));
                    }
                    response.close();

                } catch (Exception e) {
                    LOG.error("An error occurred whilst trying to get the manifest", e);
                }
            }
        }

        private String generateUrlFromFilename(final String fileName) {
            if (StringUtils.isEmpty(System.getProperty("uk.tanton.streaming.live.isEC2"))) {
                return String.format("http://%s:8080/hls/%s", EC2MetadataUtils.getPrivateIpAddress(), fileName);
            }

            return String.format("http://%s:8080/hls/%s", "localhost", fileName);
        }

        private void sendTranscodeRequest(final Stream stream, final String path) {
            TranscodeSettings.standardTranscodeSet().forEach(transcode -> {

                final TranscodeRequest transcodeRequest;
                try {
                    transcodeRequest = new TranscodeRequest(path, transcode, streamAuthenticator.getAccountForStream(stream), stream.getName());
                    final String transcodeJson = gson.toJson(transcodeRequest);

                    LOG.info(String.format("Sending message: %s", transcodeJson));
                    this.sqs.sendMessage(new SendMessageRequest(System.getProperty("uk.tanton.streaming.live.sqs.transcodeQueue"), transcodeJson));
                } catch (NoSuchPublisherException e) {
                    LOG.error("Fatal error tring to send transcode request, stream does not exist in database!", e);
                }
            });
        }


    }


}
