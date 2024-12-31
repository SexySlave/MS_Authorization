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

public class AuthHandler extends Http3RequestStreamInboundHandler {

    Authorization authorization = new Authorization(Http3ServerExample.keyPair, Http3ServerExample.sessionFactory);

    private static final String BEARER = "Bearer";
    private static final String BASIC = "Basic";

    private static final String ACCESSTOKEN = "accesstoken";
    private static final String REFRESHTOKEN = "refreshtoken";

    private static boolean isAuthorized = false;

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        System.out.println("AuthorizationHandler receive a header frame");

        String authType = frame.headers().get("authorization").toString().split(" ")[0];
        String authData = frame.headers().get("authorization").toString().split(" ")[1];

        if (authType.equals(BASIC)){
            String logData = new String(Base64.getDecoder().decode(authData));
            System.out.println("logData: " + logData);
            if (authorization.checkUser(logData)) {
                sendResponseWithTokens(ctx,  authorization.generateAccessJWT(), authorization.generateRefreshJWT(logData.split(":")[2]));
            } else {
                if (frame.headers().get("info") != null && frame.headers().get("info").toString().equals("reg")) {
                    authorization.registerUser(logData);
                    sendResponseWithTokens(ctx,  authorization.generateAccessJWT(), authorization.generateRefreshJWT(logData.split(":")[2]));
                } else {
                    send401Response(ctx, "User not found, do u wanna sign up?");
                }
            }
        } else if (authType.equals(BEARER)) {
            if (authorization.validateJWT(authData)) {
                if (authorization.getJWTType(authData).equals(ACCESSTOKEN)){
                    ctx.fireChannelRead(frame);
                    isAuthorized = true;
                } else if (authorization.getJWTType(authData).equals(REFRESHTOKEN)){
                    sendResponseWithTokens(ctx,  authorization.generateAccessJWT(), authorization.generateRefreshJWTFromJWT(authData));
                }
            } else if (frame.headers().get("info")!=null && frame.headers().get("info").toString().equals("refreshToken")){
                send401Response(ctx, "refreshTokenExpired");
            } else {
                send401Response(ctx, "accessTokenExpired");
            }
        }
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
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
