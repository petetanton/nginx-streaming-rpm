package uk.tanton.streaming.live.transcode;

import java.util.HashSet;
import java.util.Set;

public class TranscodeSettings {

    private static final String AUDIO_LOW = "96000";
    private static final String AUDIO_MEDIUM = "128000";
    private static final String AUDIO_HIGH = "360000";

    private static final String BASELINE = "baseline";
    private static final String MAIN = "main";
    private static final String HIGH = "high";

    private final String name;
    private final String profile;
    private final String bitrateVideo;
    private final String bitrateAudio;
    private final int height;
    private final int width;

    private TranscodeSettings(String name, String bitrateVideo, String bitrateAudio, String profile, int height, int width) {
        this.bitrateAudio = bitrateAudio;
        this.name = name;
        this.profile = profile;
        this.bitrateVideo = bitrateVideo;
        this.height = height;
        this.width = width;
    }

    public static Set<TranscodeSettings> standardTranscodeSet() {
        final HashSet<TranscodeSettings> set = new HashSet<>();

        set.add(new TranscodeSettings("240p", "350000", AUDIO_LOW, BASELINE, 240, 432));
        set.add(new TranscodeSettings("360p", "700000", AUDIO_MEDIUM, MAIN, 360, 640));
        set.add(new TranscodeSettings("480p", "1200000", AUDIO_HIGH, MAIN, 480, 854));
        set.add(new TranscodeSettings("720p", "2500000", AUDIO_HIGH, HIGH, 720, 1280));
        set.add(new TranscodeSettings("1080p", "5000000", AUDIO_HIGH, HIGH, 1080, 1920));

        return set;
    }

    public String getBitrateAudio() {
        return bitrateAudio;
    }

    public String getBitrateVideo() {
        return bitrateVideo;
    }

    public int getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public int getWidth() {
        return width;
    }
}
