package uk.tanton.streaming.live.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
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

    @Before
    public void setUp() throws Exception {
        this.underTest = new HttpHandler();
        when(request.content()).thenReturn(requestContent);
        when(request.headers()).thenReturn(requestHeaders);

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(requestHeaders.get("Connection")).thenReturn(null);
    }

    @Test
    public void itTriesToPublish() throws Exception {
//        app=app&flashver=Wirecast/FM%201.0%20(compatible%3B%20MS&swfurl=&tcurl=rtmp://54.171.130.111:1935/app&pageurl=&addr=82.9.169.215&clientid=1&call=publish&name=wirecastTest&type=LIVE&user=123&password=2323

        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("something=blah&yo=yolo");
        when(request.getUri()).thenReturn("/on_publish");


        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);

        this.underTest.channelRead(ctx, request);
    }

    @Ignore
    @Test
    public void itIsHappy() throws Exception {
        when(requestContent.toString()).thenReturn("{\"msg\": \"this is a test\"}");
        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);

        this.underTest.channelRead(ctx, request);

        verify(request, times(2)).getMethod();
        verify(request).content();
        verify(requestContent).toString(StandardCharsets.UTF_8);
        verify(ctx).writeAndFlush(responseCaptor.capture());
        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders);

        final FullHttpResponse actual = responseCaptor.getValue();
        assertEquals(200, actual.getStatus().code());
        assertEquals("Hello from FlyPi!", actual.content().toString(StandardCharsets.UTF_8));
    }

    @Test
    public void itRejectsBadPathsWithGoodParams() throws Exception {
        when(requestContent.toString(CharsetUtil.UTF_8)).thenReturn("app=testapp&name=streamName&user=userName&password=userPassword");

        itRejectsARequest(HttpMethod.POST, "/");
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
            verify(request).content();
            verify(requestContent).toString(CharsetUtil.UTF_8);
            assertEquals(400, actual.getStatus().code());
            assertEquals("Unknown path", actual.content().toString(StandardCharsets.UTF_8));
        } else {
            assertEquals(405, actual.getStatus().code());
            assertEquals("", actual.content().toString(StandardCharsets.UTF_8));
        }

        verifyNoMoreInteractions(ctx, request, requestContent, requestHeaders);
    }

}