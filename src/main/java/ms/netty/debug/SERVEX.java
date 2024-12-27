package ms.netty.server;

/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */


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
import io.netty.util.CharsetUtil;
import ms.netty.debug.Handler1;
import ms.netty.debug.Handler2;
import ms.netty.server.Hibernate.RefreshTokens;
import ms.netty.server.Hibernate.UsersDefault;
import ms.netty.server.handlers.*;
import org.apache.log4j.BasicConfigurator;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


public final class SERVEX {
    private static final byte[] CONTENT = "Hello World!\r\n".getBytes(CharsetUtil.US_ASCII);
    static final int PORT = 9999;
    public static KeyPair keyPair = generateRSAKeyPair();
    public  static SessionFactory sessionFactory;
    private SERVEX() { }

    public static void main(String... args) throws Exception {

        BasicConfigurator.configure();

        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/ms_authorization");
        cfg.setProperty("hibernate.connection.username", "root");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        cfg.addAnnotatedClass(UsersDefault.class);
        cfg.addAnnotatedClass(RefreshTokens.class);
        sessionFactory = cfg.buildSessionFactory();

        int port;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            port = PORT;
        }

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        SelfSignedCertificate cert = new SelfSignedCertificate();

        QuicSslContext sslContext = QuicSslContextBuilder.forServer(cert.key(), null , cert.cert())
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
                                        ch.pipeline().addLast(new Handler1());
//                                        ch.pipeline().addLast(new AttackShieldHandler());
//                                        ch.pipeline().addLast(new RouteHandler());
//                                        //ch.pipeline().addLast(new AuthorizationHandler(keyPair, sessionFactory));
//                                        //ch.pipeline().addLast(new ServerChannelHandler(keyPair, sessionFactory));
//                                        //ch.pipeline().addLast(new MainHandler());
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
            System.out.println(channel.localAddress() +" "+ PORT);
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static KeyPair generateRSAKeyPair()   {
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
