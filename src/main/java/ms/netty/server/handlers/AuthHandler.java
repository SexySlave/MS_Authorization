package ms.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.ReferenceCountUtil;
import ms.netty.server.Authorization;
import ms.netty.server.Http3ServerExample;

import java.util.Base64;

/**
 * <p>This class is responsible for handling authorization. It`s just validating the JWT token.
 * If the token is valid, it passes the request to the next handler. Otherwise, it sends a 401 response.
 * It using the Authorization class to validate JWT tokens, operate with the database and etc.
 * </p>
 *
 * <p>This class have no @Route annotation, because it is not an endpoint.</p>
 * **/

public class AuthHandler extends Http3RequestStreamInboundHandler {

    Authorization authorization = new Authorization(Http3ServerExample.keyPair, Http3ServerExample.sessionFactory);

    private static final String ACCESSTOKEN = "accesstoken";

    private boolean isAuthorized = false;


    public AuthHandler(){System.out.println(this.hashCode());}

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        System.out.println("AuthorizationHandler receive a header frame");

        String authData = frame.headers().get("authorization").toString().split(" ")[1];

            if (authorization.validateJWT(authData)) {
                if (authorization.getJWTType(authData).equals(ACCESSTOKEN)){
                    ctx.fireChannelRead(frame);
                    isAuthorized = true;
                }
            } else {
                send401Response(ctx, "accessTokenExpired");
            }

        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("AuthHandler received data frame. isAuthorized == " + isAuthorized);
        if (isAuthorized) {
            ctx.fireChannelRead(frame);
        } else {
            send401Response(ctx, "accessTokenExpired");
        }
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
    }

    private void sendResponseWithTokens(ChannelHandlerContext ctx, String accessToken, String refreshToken) {
        Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
        headersFrame.headers().status("202");
        headersFrame.headers().add("accesstoken", accessToken);
        headersFrame.headers().add("refreshtoken", refreshToken);
        ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
    }

    private void send401Response(ChannelHandlerContext ctx, String info) {
        Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
        headersFrame.headers().status("401");
        headersFrame.headers().add("info", info);
        ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
    }
}
