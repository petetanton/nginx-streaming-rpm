package uk.tanton.streaming.live.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.managment.StreamManager;

public class HttpServer {
    private static final Logger LOG = LogManager.getLogger(HttpServer.class);
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    private final StreamAuthenticator streamAuthenticator;
    private final StreamManager streamManager;
    private final ProxyClient proxyClient;

    private ChannelFuture channel;

    public HttpServer(final StreamAuthenticator streamAuthenticator, final StreamManager streamManager, ProxyClient proxyClient) {
        this.streamAuthenticator = streamAuthenticator;
        this.proxyClient = proxyClient;
        this.masterGroup = new NioEventLoopGroup();
        this.slaveGroup = new NioEventLoopGroup();
        this.streamManager = streamManager;
    }

    public void start() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(this.masterGroup, this.slaveGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel sc) throws Exception {
                        sc.pipeline().addLast("codec", new HttpServerCodec());
                        sc.pipeline().addLast("agg", new HttpObjectAggregator(512 * 1024));
                        sc.pipeline().addLast("request", new HttpHandler(streamAuthenticator, streamManager, proxyClient));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            channel = bootstrap.bind(8090).sync();
        } catch (InterruptedException e) {
            LOG.error("An exception occurred whilst trying to start the server", e);
            throw e;
        }
    }

    public void shutdown() {
        this.slaveGroup.shutdownGracefully();
        this.masterGroup.shutdownGracefully();

        try {
            channel.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error("An exception occurred whilst trying to shutdown the server", e);
            Thread.currentThread().interrupt();
        }
    }
}
