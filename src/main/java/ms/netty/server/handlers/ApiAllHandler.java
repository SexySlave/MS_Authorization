package ms.netty.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.util.CharsetUtil;

public class ApiAllHandler extends Http3RequestStreamInboundHandler { // handling all api request ( which requires or not authorization)
    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        System.out.println("ApiAllHandler received HeaderFrame");
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("ApiAllHandler received DataFrame");

        // handling our frame
        System.out.println("Received: " + frame.content().toString(CharsetUtil.UTF_8));
        if (frame.content().toString(CharsetUtil.UTF_8).equals("Hello, HTTP/3!")){
            System.out.println("Handeling");
            Http3DataFrame dataFrame = new DefaultHttp3DataFrame(ByteBufAllocator.DEFAULT.buffer().writeBytes("Hello world from Server! ".getBytes()));

            Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
            headersFrame.headers().status("200");

            ctx.write(headersFrame);
            ctx.write(dataFrame);
            ctx.flush();
        }
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }
}
