package uk.tanton.streaming.live.transcode;

public class TranscodeRequest {

    private final String srcUrl;
    private final TranscodeSettings transcode;
    private final int account;
    private final String streamName;

    public TranscodeRequest(String srcUrl, TranscodeSettings transcode, int account, String streamName) {
        this.srcUrl = srcUrl;
        this.transcode = transcode;
        this.account = account;
        this.streamName = streamName;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public TranscodeSettings getTranscode() {
        return transcode;
    }

    public int getAccount() {
        return account;
    }

    public String getStreamName() {
        return streamName;
    }
}
