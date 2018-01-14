package uk.tanton.streaming.live.exception;

public class NoSuchPublisherException extends Exception {
    public NoSuchPublisherException(String publisherName) {
        super(String.format("No such publisher: %s", publisherName));
    }
}
