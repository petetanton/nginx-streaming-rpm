package uk.tanton.streaming.live.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.StreamManager;
import uk.tanton.streaming.live.streams.Stream;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpHandlerTest {
    private static final CharSequence CONNECTION_ENTITY = HttpHeaders.newEntity("Connection");
    private static final CharSequence CLOSE_ENTITY = HttpHeaders.newEntity("close");

    private HttpHandler underTest;
    @Mock private ChannelHandlerContext ctx;
    @Mock private FullHttpRequest request;
    @Mock private ByteBuf requestContent;
    @Mock private HttpHeaders requestHeaders;
    @Mock private StreamAuthenticator streamAuthenticator;
    @Mock private StreamManager streamManager;

    @Before
    public void setUp() throws Exception {
        this.underTest = new HttpHandler(streamAuthenticator, streamManager);
        when(request.content()).thenReturn(requestContent);
        when(request.headers()).thenReturn(requestHeaders);
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(requestHeaders.get(CONNECTION_ENTITY)).thenReturn(null);
        when(streamAuthenticator.isAuthorised(any(Stream.class))).thenReturn(true);
    }

    @Test
    public void itIsAuthorisedOnPublishDone() throws Exception {
        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("app=testApp&name=testStream&user=testUser&password=testPassword");
        when(request.getUri()).thenReturn("/on_publish_done");

        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        ArgumentCaptor<Stream> streamCaptor = ArgumentCaptor.forClass(Stream.class);

        this.underTest.channelRead(ctx, request);

        verify(ctx).writeAndFlush(responseCaptor.capture());
        verify(request, times(2)).getMethod();
        verify(request, times(2)).getUri();
        verify(request).content();
        verify(requestContent).toString(CharsetUtil.UTF_8);
        verify(streamAuthenticator).isAuthorised(streamCaptor.capture());
        verify(streamManager).markStreamAsFinished(streamCaptor.getValue());

        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders, streamAuthenticator, streamManager);

        final FullHttpResponse actual = responseCaptor.getValue();
        assertEquals("stream ended", actual.content().toString(StandardCharsets.UTF_8));
        assertEquals(200, actual.getStatus().code());
    }

    @Test
    public void itIsAuthorisedOnPublish() throws Exception {
        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("app=testApp&name=testStream&user=testUser&password=testPassword");
        when(request.getUri()).thenReturn("/on_publish");

        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        ArgumentCaptor<Stream> streamCaptor = ArgumentCaptor.forClass(Stream.class);

        this.underTest.channelRead(ctx, request);

        verify(ctx).writeAndFlush(responseCaptor.capture());
        verify(request, times(2)).getMethod();
        verify(request, times(2)).getUri();
        verify(request).content();
        verify(requestContent).toString(CharsetUtil.UTF_8);
        verify(streamAuthenticator).isAuthorised(streamCaptor.capture());
        verify(streamManager).addStreamAndMarkAsStarted(streamCaptor.getValue());

        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders, streamAuthenticator, streamManager);

        final FullHttpResponse actual = responseCaptor.getValue();
        assertEquals("stream started", actual.content().toString(StandardCharsets.UTF_8));
        assertEquals(200, actual.getStatus().code());
    }

    @Test
    public void itIsNotAuthorised() throws Exception {
        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("app=testApp&name=testStream&user=testUser&password=testPassword");
        when(request.getUri()).thenReturn("/on_publish");
        when(streamAuthenticator.isAuthorised(any(Stream.class))).thenReturn(false);

        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        ArgumentCaptor<Stream> streamCaptor = ArgumentCaptor.forClass(Stream.class);

        this.underTest.channelRead(ctx, request);

        verify(ctx).writeAndFlush(responseCaptor.capture());
        verify(request, times(2)).getMethod();
        verify(request, times(1)).getUri();
        verify(request).content();
        verify(requestContent).toString(CharsetUtil.UTF_8);
        verify(streamAuthenticator).isAuthorised(streamCaptor.capture());

        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders, streamAuthenticator, streamManager);

        final FullHttpResponse actual = responseCaptor.getValue();
        assertEquals("stream not authorised", actual.content().toString(StandardCharsets.UTF_8));
        assertEquals(403, actual.getStatus().code());
    }

    @Test
    public void itRejectsBadPathsWithGoodParams() throws Exception {
        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("app=testapp&name=streamName&user=userName&password=userPassword");

        itRejectsARequest(HttpMethod.POST, "/");
    }

    @Test
    public void itRejectsGoodPathsWithBadParams() throws Exception {
        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("name=streamName&user=userName&password=userPassword");
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getUri()).thenReturn("/on_publish");


        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);

        this.underTest.channelRead(ctx, request);

        verify(request, times(2)).getMethod();
        verify(request, times(1)).getUri();
        verify(ctx).writeAndFlush(responseCaptor.capture());

        final FullHttpResponse actual = responseCaptor.getValue();

        verify(request).content();
        verify(requestContent).toString(CharsetUtil.UTF_8);
        assertEquals(400, actual.getStatus().code());
        assertEquals("Missing parameter: app", actual.content().toString(StandardCharsets.UTF_8));

        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders, streamAuthenticator, streamManager);
    }

    @Test
    public void itRejectsARequestCONNECT() throws Exception {
        itRejectsARequest(HttpMethod.CONNECT);
    }

    @Test
    public void itRejectsARequestDELETE() throws Exception {
        itRejectsARequest(HttpMethod.DELETE);
    }

    @Test
    public void itRejectsARequestHEAD() throws Exception {
        itRejectsARequest(HttpMethod.HEAD);
    }

    @Test
    public void itRejectsARequestOPTIONS() throws Exception {
        itRejectsARequest(HttpMethod.OPTIONS);
    }

    @Test
    public void itRejectsARequestPATCH() throws Exception {
        itRejectsARequest(HttpMethod.PATCH);
    }

    @Test
    public void itRejectsARequestPUT() throws Exception {
        itRejectsARequest(HttpMethod.PUT);
    }

    @Test
    public void itRejectsARequestTRACE() throws Exception {
        itRejectsARequest(HttpMethod.TRACE);
    }

    @Test
    public void itRejectsARequestGET() throws Exception {
        itRejectsARequest(HttpMethod.GET);
    }

    private void itRejectsARequest(final HttpMethod method) throws Exception {
        itRejectsARequest(method, null);
    }

    private void itRejectsARequest(final HttpMethod method, final String uri) throws Exception {
        int noOfTimesToGetUri = 1;
        when(request.getMethod()).thenReturn(method);
        if (uri != null) {
            when(request.getUri()).thenReturn(uri);
            noOfTimesToGetUri++;
        }
        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);

        this.underTest.channelRead(ctx, request);

        verify(request, times(2)).getMethod();
        verify(request, times(noOfTimesToGetUri)).getUri();
        verify(ctx).writeAndFlush(responseCaptor.capture());

        final FullHttpResponse actual = responseCaptor.getValue();

        if (method.equals(HttpMethod.POST)) {
            verify(streamAuthenticator).isAuthorised(any(Stream.class));
            verify(request).content();
            verify(requestContent).toString(CharsetUtil.UTF_8);
            assertEquals(400, actual.getStatus().code());
            assertEquals("Unknown path", actual.content().toString(StandardCharsets.UTF_8));
        } else {
            assertEquals(405, actual.getStatus().code());
            assertEquals("", actual.content().toString(StandardCharsets.UTF_8));
        }

        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders, streamAuthenticator, streamManager);
    }

}