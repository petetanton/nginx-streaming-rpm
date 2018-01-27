package uk.tanton.streaming.live.http;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class ProxyClient {
    private final CloseableHttpClient httpClient;
    private static final Logger LOG = LogManager.getLogger(ProxyClient.class);
    private static final String PRESENTATION_DELAY = "suggestedPresentationDelay";


    public ProxyClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public DefaultFullHttpResponse proxyRequest(String uri) throws IOException {
        final CloseableHttpResponse response = httpClient.execute(new HttpGet(uri));


        final InputStreamReader input = new InputStreamReader(response.getEntity().getContent());
        final BufferedReader reader = new BufferedReader(input);
        StringBuilder body = new StringBuilder();
        while(reader.ready())
        {
            final String line = reader.readLine();
            if (line.contains(PRESENTATION_DELAY)) {
                body.append(replaceAttribute(line, PRESENTATION_DELAY, "PT1.000S"));
            } else {
                body.append(line).append("\n");
            }

        }

        HttpClientUtils.closeQuietly(response);

        final String responseString = body.toString();
        final DefaultFullHttpResponse proxyResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.getStatusLine().getStatusCode()),
                copiedBuffer(responseString.getBytes())
        );

        proxyResponse.headers().add(CONTENT_LENGTH, responseString.length());
        proxyResponse.headers().add(CONTENT_TYPE, response.getFirstHeader(CONTENT_TYPE).getValue());


        return proxyResponse;

    }

    private static String replaceAttribute(String line, String key, String newValue) {
        final StringBuilder sb = new StringBuilder();
        sb
                .append(line.substring(0, line.indexOf(key) + key.length()))
                .append("=\"")
                .append(newValue)
                .append("\"")
                .append("\n");

        return sb.toString();
    }


}
