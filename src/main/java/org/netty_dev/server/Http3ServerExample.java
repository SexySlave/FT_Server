package org.netty_dev.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.incubator.codec.http3.Http3;
import io.netty.incubator.codec.http3.Http3ServerConnectionHandler;
import io.netty.incubator.codec.quic.*;
import org.apache.log4j.BasicConfigurator;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.netty.server.Hibernate.RefreshTokens;
import org.netty.server.Hibernate.UsersDefault;
import org.netty.server.handlers.AttackShieldHandler;
import org.netty.server.handlers.RouteHandler;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * <p>An enter point of Server. Here u can change different params like  @param initialMaxData , @param initialMaxData etc.</p>
 */

public final class Http3ServerExample {

    static final int PORT = 9999;
    public static KeyPair keyPair = generateRSAKeyPair();
    public static SessionFactory sessionFactory;

    private Http3ServerExample() {
    }

    public static void main(String... args) throws Exception {

        //BasicConfigurator.configure();


        int port;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            port = PORT;
        }

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        SelfSignedCertificate cert = new SelfSignedCertificate();

        QuicSslContext sslContext = QuicSslContextBuilder.forServer(cert.key(), null, cert.cert())
                .applicationProtocols(Http3.supportedApplicationProtocols()).build();
        ChannelHandler codec = Http3.newQuicServerCodecBuilder()
                .sslContext(sslContext)
                .maxIdleTimeout(100000, TimeUnit.MILLISECONDS)
                .initialMaxData(10000000)
                .initialMaxStreamDataBidirectionalLocal(1000000)
                .initialMaxStreamDataBidirectionalRemote(1000000)
                .initialMaxStreamsBidirectional(100)
                .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                .handler(new ChannelInitializer<QuicChannel>() {
                    @Override
                    protected void initChannel(QuicChannel ch) {
                        // Called for each connection
                        ch.pipeline().addLast(new Http3ServerConnectionHandler(
                                new ChannelInitializer<QuicStreamChannel>() {
                                    // Called for each request-stream,
                                    @Override
                                    protected void initChannel(QuicStreamChannel ch) {
                                        ch.pipeline().addLast(new SrvChFileHandler());
                                        //ch.pipeline().addLast(new AuthorizationHandler(keyPair, sessionFactory));
                                        //ch.pipeline().addLast(new ServerChannelHandler(keyPair, sessionFactory));
                                        //ch.pipeline().addLast(new MainHandler());
                                    }
                                }));
                    }
                }).build();
        try {
            Bootstrap bs = new Bootstrap();
            Channel channel = bs.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(codec)
                    .bind(new InetSocketAddress(PORT)).sync().channel();
            System.out.println(channel.localAddress() + " " + PORT);
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static KeyPair generateRSAKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(512); // Размер ключа: 2048 бит
        return keyPairGenerator.generateKeyPair();
    }
}
