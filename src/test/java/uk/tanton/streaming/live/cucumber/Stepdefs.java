package uk.tanton.streaming.live.cucumber;

import com.amazonaws.util.StringInputStream;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import uk.tanton.streaming.live.Main;

import static org.junit.Assert.assertEquals;

public class Stepdefs {

    private static final String HAS_STARTED_PROPERTY = "nginx.streaming.rpm.cuke.hasStarted";
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private CloseableHttpResponse response;

    @Before
    public void setup() throws Exception {
        if (!hasStarted()) {
            Main.main(null);
            System.setProperty(HAS_STARTED_PROPERTY, "true");
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
}
