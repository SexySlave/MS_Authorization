package ms.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

public class ApiAllHandler extends Http3RequestStreamInboundHandler { // handling all api request ( which requires or not authorization)
    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        System.out.println("ApiAllHandler received HeaderFrame");
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("ApiAllHandler received DataFrame");
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }
}
