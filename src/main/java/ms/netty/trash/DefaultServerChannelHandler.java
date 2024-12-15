package ms.netty.trash;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class DefaultServerChannelHandler extends Http3RequestStreamInboundHandler {



    private static final byte[] CONTENT = "Hello World!\r\n".getBytes(CharsetUtil.US_ASCII);

    @Override
    protected void channelRead(
            ChannelHandlerContext ctx, Http3HeadersFrame frame) {
        System.out.println("Custom header: " + frame.headers().get("req"));

        Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
        headersFrame.headers().status("200");
        headersFrame.headers().add("server", "netty_copy");
        headersFrame.headers().addInt("content-length", CONTENT.length);
        ctx.write(headersFrame);
//        ctx.writeAndFlush(new DefaultHttp3DataFrame(
//                Unpooled.wrappedBuffer(CONTENT)));


        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(
            ChannelHandlerContext ctx, Http3DataFrame frame) {
        ReferenceCountUtil.release(frame);

    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) {
        Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
        headersFrame.headers().status("200");
        headersFrame.headers().add("server", "netty_copy");
        headersFrame.headers().addInt("content-length", CONTENT.length);
        ctx.write(headersFrame);
        ctx.writeAndFlush(new DefaultHttp3DataFrame(
                        Unpooled.wrappedBuffer(CONTENT)))
                .addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
    }

}
