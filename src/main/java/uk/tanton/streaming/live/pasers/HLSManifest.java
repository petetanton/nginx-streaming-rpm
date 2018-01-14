package uk.tanton.streaming.live.pasers;

import java.util.HashSet;
import java.util.Set;

public class HLSManifest {

    private final Set<String> segments;

    private HLSManifest(Set<String> segments) {
        this.segments = segments;
    }

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

    public Set<String> getSegments() {
        return segments;
    }
}
