package uk.tanton.streaming.live.pasers;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HLSManifestTest {


    public static final String RAW_M3U8 = "#EXTM3U\n" +
            "#EXT-X-VERSION:3\n" +
            "#EXT-X-MEDIA-SEQUENCE:0\n" +
            "#EXT-X-TARGETDURATION:10\n" +
            "#EXT-X-DISCONTINUITY\n" +
            "#EXTINF:9.991,\n" +
            "test-0.ts\n" +
            "#EXT-X-DISCONTINUITY\n" +
            "#EXTINF:9.984,\n" +
            "test-1.ts\n" +
            "#EXT-X-DISCONTINUITY\n" +
            "#EXTINF:9.985,\n" +
            "test-2.ts\n" +
            "#EXT-X-DISCONTINUITY\n" +
            "#EXTINF:9.985,\n" +
            "test-3.ts";


    @Test
    public void itParsesAnM3u8() {
        final HLSManifest hlsManifest = HLSManifest.parseFromString(RAW_M3U8);
        for (int i = 0; i < 4; i++) {
            assertTrue(
                    String.format("Segment list does not contain test-%s.ts", i),
                    hlsManifest.getSegments().contains(String.format("test-%s.ts", i))
            );
        }
    }
}