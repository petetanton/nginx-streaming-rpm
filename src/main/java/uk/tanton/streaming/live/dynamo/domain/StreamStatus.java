package uk.tanton.streaming.live.dynamo.domain;

public enum StreamStatus {
    PENDING,
    STARTED,
    PUBLISHED,
    FINISHED;

    public static boolean canStart(StreamStatus streamStatus) {
        return streamStatus.equals(PENDING);
    }

    public static boolean canStop(StreamStatus streamStatus) {
        return streamStatus.equals(STARTED) || streamStatus.equals(PUBLISHED);
    }
}
