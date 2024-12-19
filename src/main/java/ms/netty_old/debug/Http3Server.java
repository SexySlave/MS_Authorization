//package ms.netty.debug;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioDatagramChannel;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//import io.netty.incubator.codec.http3.*;
//import io.netty.incubator.codec.quic.*;
//
//
//import javax.net.ssl.SSLException;
//import java.security.cert.CertificateException;
//
//public class Http3Server {
//
//    public static void main(String[] args) throws CertificateException, SSLException, InterruptedException {
//        // Создаем SSL-контекст для HTTP/3
//        QuicSslContext sslContext = QuicSslContextBuilder.forServer()
//                .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                .applicationProtocols("h3") // HTTP/3 ALPN
//                .build();
//
//        // Пулы потоков
//        EventLoopGroup group = new NioEventLoopGroup();
//
//        try {
//            ServerBootstrap bootstrap = new ServerBootstrap();
//            bootstrap.group(group)
//                    .channel() // Используется UDP
//                    .handler(QuicServerCodecBuilder.forServer(sslContext)
//                            .streamHandler(new ChannelInitializer<QuicChannel>() {
//                                @Override
//                                protected void initChannel(QuicChannel quicChannel) {
//                                    quicChannel.pipeline().addLast(new Http3ServerConnectionHandler());
//                                }
//                            }).build());
//
//            Channel channel = bootstrap.bind(8080).sync().channel();
//            System.out.println("HTTP/3 Server запущен на порту 8080.");
//            channel.closeFuture().sync();
//
//        } finally {
//            group.shutdownGracefully();
//        }
//    }
//
//    static class Http3ServerConnectionHandler extends ChannelInitializer<Channel> {
//        @Override
//        protected void initChannel(Channel ch) {
//            // Добавляем обработчики для HTTP/3
//            ch.pipeline().addLast(new QuicCodecDispatcher() {
//                @Override
//                protected void initChannel(Channel channel, int i, QuicConnectionIdGenerator quicConnectionIdGenerator) throws Exception {
//                    System.out.println("jdflsfksd");
//                }
//            }); // Кодировщик HTTP/3
//            ch.pipeline().addLast(new SimpleHttp3ServerHandler());
//        }
//    }
//
//    static class SimpleHttp3ServerHandler extends Http3RequestStreamInboundHandler {
//        @Override
//        protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame headersFrame) {
//            // Обрабатываем запрос
//            System.out.println("Получен запрос: " + headersFrame.headers().toString());
//
//            // Формируем ответ
//            Http3HeadersFrame responseHeaders = new DefaultHttp3HeadersFrame();
//            responseHeaders.headers().status("200");
//            responseHeaders.headers().add("content-type", "text/plain");
//
//            Http3DataFrame responseData = new DefaultHttp3DataFrame(ctx.alloc().buffer().writeBytes("Hello, HTTP/3!".getBytes()));
//
//            // Отправляем заголовки и данные ответа
//            ctx.write(responseHeaders);
//            ctx.writeAndFlush(responseData).addListener(future -> {
//                if (future.isSuccess()) {
//                    System.out.println("Ответ успешно отправлен.");
//                }
//                ctx.close(); // Завершаем поток
//            });
//        }
//
//        @Override
//        protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
//
//        }
//
//        @Override
//        protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
//
//        }
//    }
//}
