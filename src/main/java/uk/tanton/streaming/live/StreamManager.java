package uk.tanton.streaming.live;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.pasers.HLSManifest;
import uk.tanton.streaming.live.streams.Stream;
import uk.tanton.streaming.live.transcode.TranscodeRequest;
import uk.tanton.streaming.live.transcode.TranscodeSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamManager {
    private static final Logger LOG = LogManager.getLogger(StreamManager.class);


    private StreamChecker streamChecker;

    public StreamManager(final CloseableHttpClient httpClient, final AmazonSQS sqs, final StreamAuthenticator streamAuthenticator) {
        this.streamChecker = new StreamChecker(httpClient, sqs, streamAuthenticator);


        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
        scheduledExecutorService.scheduleAtFixedRate(streamChecker, 0L, 1000L, TimeUnit.MILLISECONDS);

    }

    public void addStreamAndMarkAsStarted(final Stream stream) {
        this.streamChecker.addStream(stream);
    }

    public void markStreamAsFinished(final Stream stream) {
        this.streamChecker.removeStream(stream);
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

            streams.forEach(stream -> {
                try {
                    final CloseableHttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:8080/hls/%s.m3u8", stream.getName())));
                    final String responseString = EntityUtils.toString(response.getEntity());

                    final HLSManifest hlsManifest = HLSManifest.parseFromString(responseString);

                    hlsManifest.getSegments().forEach(s -> {
                        final String url = generateUrlFromFilename(s);
                        if (!pathsSent.contains(url)) {
                            sendTranscodeRequest(stream, url);
                            pathsSent.add(url);
                        }
                    });


                } catch (IOException e) {
                    LOG.error("An error occurred whilst trying to get the manifest", e);
                }
            });


//            do something with the stream
        }

        private String generateUrlFromFilename(final String fileName) {
            return String.format("http://%s:8080/hls/%s", EC2MetadataUtils.getPrivateIpAddress(), fileName);
        }

        private void sendTranscodeRequest(final Stream stream, final String path) {
            TranscodeSettings.standardTranscodeSet().forEach(transcode -> {

                final TranscodeRequest transcodeRequest = new TranscodeRequest(path, transcode, streamAuthenticator.getAccountForStream(stream), stream.getName());
                final String transcodeJson = gson.toJson(transcodeRequest);

                LOG.info(String.format("Sending message: %s", transcodeJson));
                this.sqs.sendMessage(new SendMessageRequest(System.getProperty("uk.tanton.streaming.live.sqs.transcodeQueue"), transcodeJson));
            });
        }



    }
}
