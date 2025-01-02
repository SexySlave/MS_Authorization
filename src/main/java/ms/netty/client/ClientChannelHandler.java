package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Objects;


public class ClientChannelHandler extends Http3RequestStreamInboundHandler {

    private final Http3Frame[] http3Frame;
    private final QuicChannel quicChannel;
    private final Logger log = Logger.getLogger(ClientChannelHandlerDefault.class);


    public ClientChannelHandler(QuicChannel quicChannel, Http3Frame... http3Frame) throws IOException {
        this.quicChannel = quicChannel;
        this.http3Frame = http3Frame;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        if (http3Frame != null) {
            for (Http3Frame frame : http3Frame) {
                ctx.write(frame);
            }
            ctx.flush();
        } else {

            // ur code her...
        }
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {

        // handling authorization status
        if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401") | Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "202")) {
            ctx.pipeline().addLast(new ClientAuthChannelHandler(quicChannel));
            ctx.fireChannelRead(frame);
        }
        // ur own logic...

        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {

        // ur own logic...

        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
    }

    // function which creates new channel and sends request
    // its main idea of QUICK protocol to send each request in new channel
    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws InterruptedException, IOException {
        log.debug("Creating new quicChannel");
        Http3.newRequestStream(quicChannel, new ClientChannelHandlerDefault(quicChannel, http3HeadersFrame)).sync().getNow().closeFuture();
    }

}

