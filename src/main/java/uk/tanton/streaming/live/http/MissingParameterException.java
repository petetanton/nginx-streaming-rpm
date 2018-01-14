package uk.tanton.streaming.live.http;

public class MissingParameterException extends Exception {
    public MissingParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingParameterException(final String message) {
        super(message);
    }
}
