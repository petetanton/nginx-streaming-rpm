package uk.tanton.streaming.live;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import uk.tanton.streaming.live.streams.Stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamManager {

    private StreamChecker streamChecker;

    public StreamManager(final CloseableHttpClient httpClient) {
        this.streamChecker = new StreamChecker(httpClient);

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

        public StreamChecker(final CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            this.streams = new ArrayList<>();
            this.pathsSent = new ArrayList<>();
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
                    final CloseableHttpResponse response = httpClient.execute(new HttpGet("http://localhost:1935/hls-live/" + stream.getName()));
                    final String responseString = EntityUtils.toString(response.getEntity());

                    for (String line : responseString.split("\n")) {

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


//            do something with the stream
        }
    }
}
