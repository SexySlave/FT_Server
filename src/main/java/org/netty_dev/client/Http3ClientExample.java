package org.netty_dev.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3;
import io.netty.incubator.codec.http3.Http3ClientConnectionHandler;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.NetUtil;
import org.apache.log4j.BasicConfigurator;
import org.netty.client.ClientAuthChannelHandler;
import org.netty.client.ClientChannelHandlerDefault;
import org.netty.client.ClientOutboundAuthHandler;
import org.netty.client.UIHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public final class Http3ClientExample {
    private Http3ClientExample() {
    }

    public static void main(String... args) throws Exception {

//        BasicConfigurator.configure();

        NioEventLoopGroup group = new NioEventLoopGroup(1);

        try {
            QuicSslContext context = QuicSslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocols(Http3.supportedApplicationProtocols()).build();
            ChannelHandler codec = Http3.newQuicClientCodecBuilder()
                    .sslContext(context)
                    .maxIdleTimeout(10000, TimeUnit.MILLISECONDS)
                    .initialMaxData(10000000)
                    .initialMaxStreamDataBidirectionalLocal(1000000)
                    .build();

            Bootstrap bs = new Bootstrap();
            Channel channel = bs.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(codec)
                    .bind(0).sync().channel();

            QuicChannel quicChannel = QuicChannel.newBootstrap(channel)
                    .handler(new Http3ClientConnectionHandler())
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 9999))
                    .connect()
                    .get();


            io.netty.util.concurrent.Future<QuicStreamChannel> quicStreamChannelFuture = Http3.newRequestStream(quicChannel,
                    new ChannelInitializer<QuicStreamChannel>() {
                        @Override
                        protected void initChannel(QuicStreamChannel ch) throws IOException {
                            ch.pipeline().addLast(new ClChFileHandler());

                        }
                    });
            quicStreamChannelFuture.sync();
            QuicStreamChannel streamChannel = quicStreamChannelFuture.getNow();




            // Wait for the stream channel and quic channel to be closed (this will happen after we received the FIN).
            // After this is done we will close the underlying datagram channel.
            streamChannel.closeFuture().sync();

            // After we received the response lets also close the underlying QUIC channel and datagram channel.
            quicChannel.close().sync();
            channel.close().sync();
        } finally {
            group.shutdownGracefully();
        }

    }
}
