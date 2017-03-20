package uk.tanton.streaming.live;

import uk.tanton.streaming.live.streams.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StreamManager implements Runnable {

    final List<Stream> streams;

    public StreamManager() {
        this.streams = new ArrayList<>();
    }

    public void addStreamAndMarkAsStarted(final Stream stream) {
        this.streams.add(stream);
    }

    public void markStreamAsFinished(final Stream stream) {

    }

    @Override
    public void run() {
        streams.forEach(new Consumer<Stream>() {
            @Override
            public void accept(Stream stream) {

            }
        });
    }
}
