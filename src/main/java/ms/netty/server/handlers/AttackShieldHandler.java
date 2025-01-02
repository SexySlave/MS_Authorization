package ms.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

/**
 * <p>This class is responsible for handling attacks.
 * It is a handler that is added to the pipeline to intercept the incoming request and check if it is an attack.
 * If it is an attack, it will block the request and return an error message.
 * </p>
 * <p>This class have no @Route annotation, because it is not a endpoint.</p>
 */

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
