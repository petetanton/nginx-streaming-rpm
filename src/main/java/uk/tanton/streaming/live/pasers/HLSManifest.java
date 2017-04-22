package uk.tanton.streaming.live.pasers;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tantop01 on 22/04/17.
 */
public class HLSManifest {

    public static HLSManifest parseFromString(final String s) {
        final String[] lines = s.split("\n");

        final Set<String> segments = new HashSet<>();
        for (String l : lines) {
            if (l.contains(".ts")) {
                segments.add(l);
            }
        }

        return new HLSManifest(segments);
    }

    private HLSManifest(Set<String> segments) {
        this.segments = segments;
    }

    private final Set<String> segments;

    public Set<String> getSegments() {
        return segments;
    }
}


//#EXTM3U
//#EXT-X-VERSION:3
//#EXT-X-MEDIA-SEQUENCE:0
//#EXT-X-TARGETDURATION:10
//#EXT-X-DISCONTINUITY
//#EXTINF:9.991,
//test-0.ts
//#EXT-X-DISCONTINUITY
//#EXTINF:9.984,
//test-1.ts
//#EXT-X-DISCONTINUITY
//#EXTINF:9.985,
//test-2.ts
//#EXT-X-DISCONTINUITY
//#EXTINF:9.985,
//test-3.ts