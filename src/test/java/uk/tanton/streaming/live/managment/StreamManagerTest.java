package uk.tanton.streaming.live.managment;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.StringInputStream;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.dynamo.domain.AccountRecord;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
import uk.tanton.streaming.live.dynamo.domain.StreamRecord;
import uk.tanton.streaming.live.dynamo.domain.StreamStatus;
import uk.tanton.streaming.live.exception.NoSuchAccountException;
import uk.tanton.streaming.live.exception.NoSuchPublisherException;
import uk.tanton.streaming.live.exception.NoSuchStreamException;
import uk.tanton.streaming.live.exception.StreamException;
import uk.tanton.streaming.live.pasers.HLSManifestTest;
import uk.tanton.streaming.live.streams.Stream;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamManagerTest {

    private static final Stream STREAM = new Stream("app", "name", "user", "password");
    private StreamManager underTest;


    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private AmazonSQS sqs;

    @Mock
    private StreamAuthenticator streamAuthenticator;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StreamDataConnector streamDataConnector;

    @Before
    public void setup() throws NoSuchStreamException, NoSuchAccountException, NoSuchPublisherException {
        underTest = new StreamManager(httpClient, sqs, streamAuthenticator, true, streamDataConnector);
        StreamRecord t = new StreamRecord();
        t.setStreamId("name");
        t.setAccountId(1);
        t.setStreamStatus(StreamStatus.PENDING);
        when(streamDataConnector.getStreamRecord("name", 1)).thenReturn(t);
        AccountRecord account = new AccountRecord();
        account.setAccountId(1);
        when(streamDataConnector.getAccount(1)).thenReturn(account);
        when(streamDataConnector.getPublisher(STREAM.getUser())).thenReturn(new Publisher(
                1,
                "hash",
                "salt",
                STREAM.getUser(),
                Date.from(Instant.now()),
                Date.from(Instant.now().plusSeconds(60)))
        );
        System.setProperty("uk.tanton.streaming.live.isEC2", "false");
    }


    @Test
    public void itDoesNothingIfTheManifestDoesNotExist() throws IOException, InterruptedException, StreamException, NoSuchAccountException, NoSuchStreamException, NoSuchPublisherException {
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not Found"));

        underTest.addStreamAndMarkAsStarted(STREAM);

        final ArgumentCaptor<HttpUriRequest> httpArgumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);

        verify(httpClient, timeout(1000)).execute(httpArgumentCaptor.capture());
        verify(httpResponse).getStatusLine();
        verify(httpResponse, timeout(1000)).close();
        verifyNoMoreInteractions(httpClient, sqs, streamAuthenticator, httpResponse);

        assertEquals("http://localhost:8080/hls/name.m3u8", httpArgumentCaptor.getValue().getURI().toString());
    }


    @Test
    public void itDoesSomethingIfTheManifestDoesExist() throws IOException, InterruptedException, StreamException, NoSuchAccountException, NoSuchStreamException, NoSuchPublisherException {
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        final BasicHttpEntity t = new BasicHttpEntity();
        t.setContent(new StringInputStream(HLSManifestTest.RAW_M3U8));
        t.setContentLength(HLSManifestTest.RAW_M3U8.length());
        when(httpResponse.getEntity()).thenReturn(t);

        underTest.addStreamAndMarkAsStarted(STREAM);

        final ArgumentCaptor<HttpUriRequest> httpArgumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);

        verify(httpClient, timeout(1000)).execute(httpArgumentCaptor.capture());
        verify(httpResponse, atLeastOnce()).getStatusLine();
        verify(httpResponse, atLeastOnce()).getEntity();
        verify(httpResponse, timeout(1000)).close();
        verify(sqs, times(20)).sendMessage(any(SendMessageRequest.class));
        verify(streamAuthenticator, times(20)).getAccountForStream(STREAM);
        verifyNoMoreInteractions(httpClient, sqs, streamAuthenticator, httpResponse);

        assertEquals("http://localhost:8080/hls/name.m3u8", httpArgumentCaptor.getValue().getURI().toString());
    }

    @Test
    public void itStopsDoingThingsAfterAStreamHasBeenRemoved() throws IOException, InterruptedException, StreamException, NoSuchAccountException, NoSuchStreamException, NoSuchPublisherException {
        underTest = new StreamManager(httpClient, sqs, streamAuthenticator, true, streamDataConnector);

        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        final BasicHttpEntity t = new BasicHttpEntity();
        t.setContent(new StringInputStream(HLSManifestTest.RAW_M3U8));
        t.setContentLength(HLSManifestTest.RAW_M3U8.length());
        when(httpResponse.getEntity()).thenReturn(t);

        underTest.addStreamAndMarkAsStarted(STREAM);

        final ArgumentCaptor<HttpUriRequest> httpArgumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);

        verify(httpClient, timeout(1000)).execute(httpArgumentCaptor.capture());
        verify(httpResponse).getStatusLine();
        verify(httpResponse).getEntity();
        verify(httpResponse, timeout(1000)).close();
        verify(sqs, times(20)).sendMessage(any(SendMessageRequest.class));
        verify(streamAuthenticator, times(20)).getAccountForStream(STREAM);

        assertEquals("http://localhost:8080/hls/name.m3u8", httpArgumentCaptor.getValue().getURI().toString());

        underTest.markStreamAsFinished(STREAM);
        Thread.sleep(1500); // NOSONAR
        verifyNoMoreInteractions(httpClient, sqs, streamAuthenticator, httpResponse);
    }

}