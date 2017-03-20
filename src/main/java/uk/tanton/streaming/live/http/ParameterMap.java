package uk.tanton.streaming.live.http;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ParameterMap {

    private final Map<String, String> params;

    public ParameterMap() {
        this.params = new HashMap<>();
    }

    public void put(final String key, final String value) {
        this.params.put(key, value);
    }

    public String get(final String key) throws MissingParameterException {
        try {
            final String s = this.params.get(key);
            if (StringUtils.isEmpty(s)) {
                throw new NullPointerException();
            }
            return s;
        } catch (NullPointerException e) {
            throw new MissingParameterException("Missing parameter: " + key);
        }
    }

    public static ParameterMap buildParamMapFromString(final String s) throws MissingParameterException {
        final ParameterMap parameterMap = new ParameterMap();
        final String[] split = s.split("&");
        for (String s1 : split) {
            try {
                parameterMap.put(s1.split("=")[0], s1.split("=")[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new MissingParameterException(String.format("Missing param: %s", s1));
            }
        }

        return parameterMap;
    }
}
