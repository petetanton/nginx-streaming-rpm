package uk.tanton.streaming.live.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
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

import java.util.function.Supplier;

public class HttpServer {
    private static final Logger LOG = LogManager.getLogger(HttpServer.class);
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    private final Supplier<ChannelInboundHandlerAdapter> httpHandlerSupplier;
    private final int port;

    private ChannelFuture channel;

    public HttpServer(Supplier<ChannelInboundHandlerAdapter> httpHandlerSupplier, int port) {
        this.httpHandlerSupplier = httpHandlerSupplier;
        this.port = port;
        this.masterGroup = new NioEventLoopGroup();
        this.slaveGroup = new NioEventLoopGroup();
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
                        sc.pipeline().addLast("request", httpHandlerSupplier.get());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            channel = bootstrap.bind(port).sync();
            LOG.info("Started server on port {}", port);
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
