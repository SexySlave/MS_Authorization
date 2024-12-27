package ms.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

public class AttackShieldHandler extends Http3RequestStreamInboundHandler {

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        // handling attack...
        // or pass
        System.out.println("AttackHandler");
        ctx.fireChannelRead(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        // handling attack...
        // or pass
        ctx.fireChannelRead(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
        // handling attack...


    }
}
