package ms.netty.debug;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

public class Handler1 extends Http3RequestStreamInboundHandler {
    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {

        System.out.println("Handler1 receive a header frame");
        System.out.println(ctx.pipeline());
        ctx.pipeline().addLast(new Handler2());

    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("Handler1 receive a data frame");
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Handler1 channelInputClosed");
    }
}
