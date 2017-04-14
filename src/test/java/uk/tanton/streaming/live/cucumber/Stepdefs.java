package uk.tanton.streaming.live.cucumber;

import com.amazonaws.util.StringInputStream;
import com.github.tomakehurst.wiremock.WireMockServer;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.tanton.streaming.live.Main;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.modules.AwsModules;

import static org.junit.Assert.assertEquals;

public class Stepdefs {

    private static final String HAS_STARTED_PROPERTY = "nginx.streaming.rpm.cuke.hasStarted";
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private CloseableHttpResponse response;
    private WireMockServer wireMockServer;

//    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock private AwsModules awsModules;
    @Mock private StreamDataConnector streamDataConnector;
    @InjectMocks private Main main;

    @Before
    public void setup() throws Exception {
        if (!hasStarted()) {
            System.setProperty("com.amazonaws.sdk.ec2MetadataServiceEndpointOverride", "http://localhost:9001");
            System.setProperty("uk.tanton.streaming.live.dynamo.accountsTable", "some-accounts-table");
            System.setProperty("uk.tanton.streaming.live.dynamo.publishersTable", "some-publishers-table");
            System.setProperty("uk.tanton.streaming.live.dynamo.streamTable"    , "some-stream-table");
            System.setProperty(HAS_STARTED_PROPERTY, "true");


//            wireMockServer = new WireMockServer(9001);
//            wireMockServer.start();
            main.main(null);

        }
    }

    private boolean hasStarted() {
        final String property = System.getProperty(HAS_STARTED_PROPERTY);
        return Boolean.valueOf(property);
    }

    @Given("^A publish request is made for app \"([^\"]*)\" stream \"([^\"]*)\" user \"([^\"]*)\" password \"([^\"]*)\"$")
    public void A_publish_request_is_made_for_app_stream_user_password(String app, String stream, String user, String password) throws Throwable {

        final HttpPost httpPost = new HttpPost("http://localhost:8090/on_publish");
        final BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new StringInputStream(String.format("app=%s&name=%s&user=%s&password=%s", app, stream, user, password)));
        httpPost.setEntity(entity);

        response = httpClient.execute(httpPost);
    }

    @Then("^A \"([^\"]*)\" response code is returned$")
    public void A_response_code_is_returned(String arg1) throws Throwable {
        assertEquals(Integer.parseInt(arg1), response.getStatusLine().getStatusCode());
    }

    @Then("^The response has a body of \"([^\"]*)\"$")
    public void The_response_has_a_body_of(String arg1) throws Throwable {
        assertEquals(arg1, EntityUtils.toString(response.getEntity()));
    }

    @Given("^EC2 Metadata has been mocked$")
    public void EC_Metadata_has_been_mocked() throws Throwable {
//        final StubMapping stubMapping = wireMockServer.stubFor(get("/latest/user-data/"));
//        stubMapping.setResponse(new ResponseDefinition(200, "accountsTable:some-accounts-table\npublishersTable:some-publishers-table\nstreamsTable:some-streams-table\n"));
        // Express the Regexp above with the code you wish you had
//        Thread.sleep(10000);
//        throw new PendingException();
    }
}
