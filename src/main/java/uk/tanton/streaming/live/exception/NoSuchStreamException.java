package uk.tanton.streaming.live.exception;

public class NoSuchStreamException extends Exception {
    public NoSuchStreamException(String streamId, int accountId) {
        super(String.format("No such stream {%s} for account ID: %s", streamId, accountId));
    }
}
