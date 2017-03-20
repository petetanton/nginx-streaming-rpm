package uk.tanton.streaming.live.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.StreamManager;
import uk.tanton.streaming.live.streams.Stream;

import java.util.Optional;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class HttpHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger(HttpHandler.class);

    private final StreamAuthenticator streamAuthenticator;
    private final StreamManager streamManager;

    public HttpHandler(final StreamAuthenticator streamAuthenticator, final StreamManager streamManager) {
        this.streamAuthenticator = streamAuthenticator;
        this.streamManager = streamManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;


            ctx.writeAndFlush(parseRequestAndCreateResponse(request));
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private FullHttpResponse parseRequestAndCreateResponse(final FullHttpRequest request) {
        LOG.debug(request.getMethod().name() + ": " + request.getUri());


        if (request.getMethod().equals(HttpMethod.POST)) {
            final Optional<String> requestMsg = Optional.ofNullable(request.content().toString(CharsetUtil.UTF_8));

            if (requestMsg.isPresent()) {

                final Stream stream;

                try {
                    stream = parseStringIntoStream(requestMsg.get());
                } catch (final MissingParameterException e) {
                    LOG.info(e);
                    return buildDefaultResponse(HttpResponseStatus.BAD_REQUEST, e.getMessage());
                }

                if (!streamAuthenticator.isAuthorised(stream)) {
                    return buildNotAuthorisedResponse();
                }


                switch (request.getUri()) {
                    case "/on_publish":
                        LOG.info("on_publish");
                        this.streamManager.addStreamAndMarkAsStarted(stream);
                        return buildDefaultResponse(HttpResponseStatus.OK, "stream started");
                    case "/on_publish_done":
                        LOG.info("on_publish_done");
                        this.streamManager.markStreamAsFinished(stream);
                        return buildDefaultResponse(HttpResponseStatus.OK, "stream ended");
                    default:
                        return buildDefaultResponse(HttpResponseStatus.BAD_REQUEST, "Unknown path");

                }
//              app=app&flashver=Wirecast/FM%201.0%20(compatible%3B%20MS&swfurl=&tcurl=rtmp://54.171.130.111:1935/app&pageurl=&addr=82.9.169.215&clientid=1&call=publish&name=wirecastTest&type=LIVE&user=123&password=2323

//              app=app&flashver=Wirecast/FM%201.0%20(compatible%3B%20MS&swfurl=&tcurl=rtmp://54.171.130.111:1935/app&pageurl=&addr=82.9.169.215&clientid=1&call=publish_done&name=wirecastTest&user=123&password=2323
            }

        }
        return buildDefaultResponse(HttpResponseStatus.METHOD_NOT_ALLOWED, null);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("An exception was caught", cause);
        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));
    }

    private FullHttpResponse buildDefaultResponse(final HttpResponseStatus status, final String message) {
        final FullHttpResponse httpResponse;
        if (message != null) {
            httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    status,
                    copiedBuffer(message.getBytes())
            );
            httpResponse.headers().add(HttpHeaders.Names.CONTENT_LENGTH, message.length());
            httpResponse.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        } else {
            httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    status
            );
            httpResponse.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }

        return httpResponse;
    }

    private FullHttpResponse buildNotAuthorisedResponse() {
        return buildDefaultResponse(HttpResponseStatus.FORBIDDEN, "stream not authorised");
    }

    private Stream parseStringIntoStream(final String requestString) throws MissingParameterException {
        final ParameterMap parameterMap = ParameterMap.buildParamMapFromString(requestString);

        return new Stream(parameterMap.get("app"), parameterMap.get("name"), parameterMap.get("user"), parameterMap.get("password"));
    }

}
