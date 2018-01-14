package uk.tanton.streaming.live.exception;

public class NoSuchAccountException extends Exception {
    public NoSuchAccountException(int accountId) {
        super(String.format("No such account for id: %s", accountId));
    }
}
