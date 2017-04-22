package uk.tanton.streaming.live.transcode;

public class TranscodeRequest {

    private final String srcUrl;
    private final TranscodeSettings transcode;
    private final String account;
    private final String streamName;

    public TranscodeRequest(String srcUrl, TranscodeSettings transcode, String account, String streamName) {
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

    public String getAccount() {
        return account;
    }

    public String getStreamName() {
        return streamName;
    }
}
