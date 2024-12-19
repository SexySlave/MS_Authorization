package ms.netty.trash;//package ms.netty.trash;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioDatagramChannel;
//import io.netty.handler.ssl.util.SelfSignedCertificate;
//import io.netty.incubator.codec.http3.*;
//import io.netty.incubator.codec.quic.*;
//import org.apache.log4j.BasicConfigurator;
//
//import java.net.InetSocketAddress;
//import java.util.concurrent.TimeUnit;
//
//public class Htttp3Server {
//
//    private final Http3RequestStreamInboundHandler HANDLER;
//    private final int PORT;
//
//    public Htttp3Server(Http3RequestStreamInboundHandler handler, C){
//        HANDLER=handler;
//        PORT = port;
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//    //private static final byte[] CONTENT = "Hello World!\r\n".getBytes(CharsetUtil.US_ASCII);
//
//
//
//
//    public void run() throws Exception {
//
//        BasicConfigurator.configure();
//        int port;
//        // Allow to pass in the port so we can also use it to run h3spec against
//
//        System.out.println("PORT: " + PORT);
//        NioEventLoopGroup group = new NioEventLoopGroup(1);
//        SelfSignedCertificate cert = new SelfSignedCertificate();
//        QuicSslContext sslContext = QuicSslContextBuilder.forServer(cert.key(), null, cert.cert())
//                .applicationProtocols(Http3.supportedApplicationProtocols()).build();
//        ChannelHandler codec = Http3.newQuicServerCodecBuilder()
//                .sslContext(sslContext)
//                .maxIdleTimeout(5000, TimeUnit.MILLISECONDS)
//                .initialMaxData(10000000)
//                .initialMaxStreamDataBidirectionalLocal(1000000)
//                .initialMaxStreamDataBidirectionalRemote(1000000)
//                .initialMaxStreamsBidirectional(100)
//                .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
//                .handler(new ChannelInitializer<QuicChannel>() {
//                    @Override
//                    protected void initChannel(QuicChannel ch) {
//                        // Called for each connection
//                        ch.pipeline().addLast(new Http3ServerConnectionHandler(
//                                new ChannelInitializer<QuicStreamChannel>() {
//                                    // Called for each request-stream,
//                                    @Override
//                                    protected void initChannel(QuicStreamChannel ch) {
//                                        ch.pipeline().addLast(HANDLER);
//                                    }
//                                }));
//                    }
//                }).build();
//        try {
//            Bootstrap bs = new Bootstrap();
//            Channel channel = bs.group(group)
//                    .channel(NioDatagramChannel.class)
//                    .handler(codec)
//                    .bind(new InetSocketAddress(PORT)).sync().channel();
//            channel.closeFuture().sync();
//        } finally {
//            group.shutdownGracefully();
//        }
//    }
//}
