package uk.tanton.streaming.live.http;

public class MissingParameterException extends Exception {
    public MissingParameterException(final String message) {
        super(message);
    }
}
