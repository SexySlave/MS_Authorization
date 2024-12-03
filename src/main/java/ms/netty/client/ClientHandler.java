package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import ms.netty.server.Http3ServerExample;


public class ClientHandler extends Http3RequestStreamInboundHandler {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
        System.out.println(NetUtil.LOCALHOST4.getHostAddress());
        frame.headers().method("GET").path("/")
                .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                .scheme("https").add("req", "NOTE");
        ctx.writeAndFlush(frame);




    }


    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) {
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) {
        System.err.print(frame.content().toString(CharsetUtil.US_ASCII));
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) {
        ctx.close();
    }

}
