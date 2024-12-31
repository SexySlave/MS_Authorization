package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.incubator.codec.http3.DefaultHttp3DataFrame;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ClientOutboundAuthHandler extends ChannelOutboundHandlerAdapter {

    QuicChannel quicChannel;

    public ClientOutboundAuthHandler(QuicChannel quicChannel) {
        this.quicChannel  = quicChannel;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Outbound handler got the message");
        if (msg instanceof Http3HeadersFrame ) {
            Http3HeadersFrame headersFrame = (Http3HeadersFrame) msg;
            quicChannel.attr(AttributeKey.valueOf("HeaderFrame")).set(headersFrame);
            System.out.println("Outbound handler got the headers frame");
            if (UIHandler.getAccessAndRefreshTokens()[0] == null) {
                headersFrame.headers().add("authorization", "Basic " + Base64.getEncoder().encodeToString(UIHandler.getLogdata().concat(":").concat(UIHandler.getMacAddress()).getBytes(StandardCharsets.UTF_8)));
            } else {
                headersFrame.headers().add("authorization", "Bearer " + UIHandler.getAccessAndRefreshTokens()[0]);
            }
            ctx.write(headersFrame, promise);

        } else if ((msg instanceof DefaultHttp3DataFrame dataFrame)){
            System.out.println("Outbound handler got the data frame");
            dataFrame.content().retain(2);
            quicChannel.attr(AttributeKey.valueOf("DataFrame")).set(dataFrame);
            ctx.write(dataFrame, promise);

        }


    }
}
