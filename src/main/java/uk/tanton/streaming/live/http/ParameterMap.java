package uk.tanton.streaming.live.http;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ParameterMap {
    private static final Logger LOG = LogManager.getLogger(ParameterMap.class);

    private final Map<String, String> params;

    public ParameterMap() {
        this.params = new HashMap<>();
    }

    public static ParameterMap buildParamMapFromString(final String s) throws MissingParameterException {
        LOG.error("PETE: {}", s);

        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(s);
        LOG.info("QSD {}", queryStringDecoder.parameters());
        final ParameterMap parameterMap = new ParameterMap();
        final String[] split = s.split("&");
        for (String s1 : split) {
            final String[] items = s1.split("=");
            if (items.length > 1) {
                parameterMap.put(items[0], s1.substring(s1.indexOf("=") + 1));
            }
        }

        LOG.info("ParameterMap {}", parameterMap);
        final String tcurl = parameterMap.getNullable("tcurl");
        if (!isBlank(tcurl) && isBlank(parameterMap.getNullable("name")) && tcurl.contains("name=")) {
            String name = tcurl.substring(tcurl.indexOf("name=") + 5);
            if (name.indexOf("&") > 0) {
                name = name.substring(0, name.indexOf("&"));
            }
            parameterMap.put("name", name);
        }

        return parameterMap;
    }



    public static String getIndexOrEmptyString(final String s, final String regex, final int index) {
        try {
            return s.split(regex)[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.debug(e);
            return "";
        }
    }

    public void put(final String key, final String value) {
        this.params.put(key, value);
    }

    public String get(final String key) throws MissingParameterException {
        try {
            final String s = this.params.get(key);
            if (StringUtils.isEmpty(s)) {
                throw new NullPointerException(String.format("No value for key: %s", key));
            }
            return s;
        } catch (NullPointerException e) {
            throw new MissingParameterException("Missing parameter: " + key, e);
        }
    }

    public String getNullable(final String key) {
        final String s = this.params.get(key);
        if (StringUtils.isEmpty(s)) {
            return "";
        }
        return s;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        params.entrySet().forEach(entry -> sb
                .append("{key: ")
                .append(entry.getKey())
                .append(", value: ")
                .append(entry.getValue())
                .append("}\n"));

        return sb.toString();
    }
}
