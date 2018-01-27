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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.exception.NoSuchAccountException;
import uk.tanton.streaming.live.exception.NoSuchPublisherException;
import uk.tanton.streaming.live.exception.NoSuchStreamException;
import uk.tanton.streaming.live.exception.StreamException;
import uk.tanton.streaming.live.managment.StreamManager;
import uk.tanton.streaming.live.streams.Stream;

import java.io.IOException;
import java.util.Optional;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class HttpHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger(HttpHandler.class);

    private final StreamAuthenticator streamAuthenticator;
    private final StreamManager streamManager;
    private final ProxyClient proxyClient;

    public HttpHandler(final StreamAuthenticator streamAuthenticator, final StreamManager streamManager, ProxyClient proxyClient) {
        this.streamAuthenticator = streamAuthenticator;
        this.streamManager = streamManager;
        this.proxyClient = proxyClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;


            try {
                FullHttpResponse response;
                final String uri = request.getUri();
                if (uri.endsWith(".m4v") || uri.endsWith(".m4a")) {
                    response = redirectToMainNginx(uri);
                } else if (uri.endsWith(".mpd")) {
                     response = getManifest(request);
                } else {
                    response = parseRequestAndCreateResponse(request);
                }

                ctx.writeAndFlush(response);
            } catch (NoSuchPublisherException | NoSuchAccountException | NoSuchStreamException e) {
                LOG.error("Error caught in handler", e);
                ctx.writeAndFlush(buildDefaultResponse(HttpResponseStatus.CONFLICT, e.getMessage()));
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private FullHttpResponse redirectToMainNginx(String uri) {
        final String msg = "Redirected";
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.FOUND,
                copiedBuffer(msg.getBytes())
        );

        response.headers().add(HttpHeaders.Names.LOCATION, "http://localhost:8080" + uri);
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, msg.length());

        return response;
    }

    private FullHttpResponse parseRequestAndCreateResponse(final FullHttpRequest request) throws NoSuchPublisherException, NoSuchAccountException, NoSuchStreamException {
        final String path = request.getUri();
        LOG.info(request.getMethod().name() + ": " + path);

        if (!request.getMethod().equals(HttpMethod.POST)) {
            return buildDefaultResponse(HttpResponseStatus.METHOD_NOT_ALLOWED, null);
        }
        final Optional<String> requestMsg = Optional.ofNullable(request.content().toString(CharsetUtil.UTF_8));

        if (requestMsg.isPresent() && StringUtils.isNotEmpty(requestMsg.get())) {
            try {
                final String requestString = requestMsg.get().replace("%26", "&");
                LOG.info(String.format("Request string: %s", requestString));
                final ParameterMap parameterMap = ParameterMap.buildParamMapFromString(requestString);

                return handleMsg(path, parameterMap);
            } catch (MissingParameterException e) {
                LOG.info(e);

                return buildDefaultResponse(HttpResponseStatus.BAD_REQUEST, e.getMessage());
            }
        }

        return buildDefaultResponse(HttpResponseStatus.BAD_REQUEST, "No request body was sent");
    }

    private FullHttpResponse getManifest(FullHttpRequest request) {
        try {
            final String uri = "http://localhost:8080" + request.getUri();
            LOG.info("Proxying to {}", uri);
            return proxyClient.proxyRequest(uri);
        } catch (IOException e) {
            LOG.error(e);
            return buildDefaultResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
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

    private FullHttpResponse handleMsg(final String path, final ParameterMap parameterMap) throws MissingParameterException, NoSuchPublisherException, NoSuchStreamException, NoSuchAccountException {
        final Stream stream;

        if (isForwardedConnection(parameterMap)) {
            return buildDefaultResponse(HttpResponseStatus.OK, "OK");
        }
        stream = parseStringIntoStream(parameterMap);

        if (!streamAuthenticator.isAuthorised(stream)) {
            return buildNotAuthorisedResponse();
        }

        try {
            return determineActionBasedOnPath(path, stream);
        } catch (StreamException e) {
            LOG.error("An exception occured", e);
            return buildDefaultResponse(HttpResponseStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private FullHttpResponse determineActionBasedOnPath(final String path, final Stream stream) throws StreamException, NoSuchAccountException, NoSuchStreamException, NoSuchPublisherException {
        switch (path) {
            case "/on_publish":
                LOG.info("on_publish");
                this.streamManager.addStreamAndMarkAsStarted(stream);
                return buildDefaultResponse(HttpResponseStatus.OK, "stream started");
            case "/on_publish_done":
                LOG.info("on_publish_done");
                this.streamManager.markStreamAsFinished(stream);
                return buildDefaultResponse(HttpResponseStatus.OK, "stream ended");
            case "/on_connect":
                LOG.info("on_connect");
                return buildDefaultResponse(HttpResponseStatus.OK, "connected");
            default:
                LOG.error(String.format("Unknown request type: %s", path));
                return buildDefaultResponse(HttpResponseStatus.BAD_REQUEST, "Unknown path");

        }
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

    private Stream parseStringIntoStream(final ParameterMap parameterMap) throws MissingParameterException {
        return new Stream(parameterMap.get("app"), parameterMap.get("name"), parameterMap.get("user"), parameterMap.get("password"));
    }

    private boolean isForwardedConnection(final ParameterMap pm) {
        if ("connect".equalsIgnoreCase(pm.getNullable("call"))) {
            final String app = pm.getNullable("app");
            if ("dash-live".equalsIgnoreCase(app) || "hls-live".equalsIgnoreCase(app)) {
                return true;
            }
        }

        return false;
    }

}
