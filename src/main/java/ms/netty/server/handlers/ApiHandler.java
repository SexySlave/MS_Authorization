package ms.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

public class ApiHandler extends Http3RequestStreamInboundHandler {
    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        System.out.println("ApiHandler received HeaderFrame\nOnly public api can be used");
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("ApiHandler received DataFrame\nOnly public api can be used");
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }
}
