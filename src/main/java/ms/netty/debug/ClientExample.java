package ms.netty.debug;

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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.NetUtil;
import org.apache.log4j.BasicConfigurator;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public final class ClientExample {
    private ClientExample() {
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
                    new Http3RequestStreamInboundHandler() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("channelActive");
                            Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                            headersFrame.headers().path("/").authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999).method("GET").scheme("https");
                            ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
                            Http3.newRequestStream(quicChannel, new Http3RequestStreamInboundHandler() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                                    headersFrame.headers().path("/").authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999).method("GET").scheme("https");
                                    ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
                                }

                                @Override
                                protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
                                    System.out.println("Client receive a header frame");
                                    System.out.println(frame.headers());
                                }

                                @Override
                                protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
                                    System.out.println("Client receive a data frame");
                                }

                                @Override
                                protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("Client channelInputClosed");
                                }
                            });
                        }

                        @Override
                        protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {

                        }

                        @Override
                        protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {

                        }

                        @Override
                        protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

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