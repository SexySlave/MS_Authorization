package ms.netty.client;

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
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3;
import io.netty.incubator.codec.http3.Http3ClientConnectionHandler;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.quic.*;
import io.netty.util.NetUtil;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public final class Http3ClientExample {
    private Http3ClientExample() {
    }

    public static void main(String... args) throws Exception {

        BasicConfigurator.configure();

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
                            // adding outbound handlers
                            ch.pipeline().addLast(new ClientOutboundAuthHandler(quicChannel));
                            // ------------------------------------------------------
                            // adding inbound handlers
                            ch.pipeline().addLast(new ClientChannelHandlerDefault(quicChannel, null));
                            ch.pipeline().addLast(new ClientAuthChannelHandler(quicChannel));

                        }
                    });
            quicStreamChannelFuture.sync();
            QuicStreamChannel streamChannel = quicStreamChannelFuture.getNow();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (quicChannel.isOpen()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000 * 5);
                            Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();

                            frame.headers().method("GET").path("/secure")
                                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                                    .scheme("https").add("authorization", "Bearer " + UIHandler.getAccessAndRefreshTokens()[1]);
                            Http3.newRequestStream(quicChannel, new ClientChannelHandlerDefault(quicChannel, frame));
                        } catch (IOException | InterruptedException e) {
                            System.out.println("sleep interrupted");
                        }
                    }
                }
            }, "RefreshAccessTokenThread");
            t.start();


            // Wait for the stream channel and quic channel to be closed (this will happen after we received the FIN).
            // After this is done we will close the underlying datagram channel.
            streamChannel.closeFuture().sync();
            t.interrupt();
            // After we received the response lets also close the underlying QUIC channel and datagram channel.
            quicChannel.close().sync();
            channel.close().sync();
        } finally {
            group.shutdownGracefully();
        }

    }
}
