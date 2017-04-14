package uk.tanton.streaming.live;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;

import static com.github.tomakehurst.wiremock.client.WireMock.get;

public class StreamManagerTest {

    private StreamManager underTest;

    private WireMockServer wireMockServer;

    @Before
    public void setup() {
        wireMockServer = new WireMockServer(1935);
        wireMockServer.start();
        underTest = new StreamManager(HttpClientBuilder.create().build());

        final StubMapping stubMapping = wireMockServer.stubFor(get("/hls-live/stream.m3u8"));
        stubMapping.setResponse(new ResponseDefinition(200, "accountsTable:some-accounts-table\npublishersTable:some-publishers-table\nstreamsTable:some-streams-table\n"));

    }

}