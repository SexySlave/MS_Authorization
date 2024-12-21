package ms.netty.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.hibernate.SessionFactory;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ServerChannelHandler extends Http3RequestStreamInboundHandler {
    //private static final byte[] CONTENT = "Hello World!\r\n".getBytes(CharsetUtil.US_ASCII);
    Authorization authorization;
    KeyPair keyPair;
    SessionFactory sessionFactory;
    public ServerChannelHandler(KeyPair keyPair, SessionFactory sessionFactory){
        this.keyPair=keyPair;
        this.sessionFactory = sessionFactory;
        System.out.println("hash");
        System.out.println(this.hashCode());
    }

    @Override
    protected void channelRead(
            ChannelHandlerContext ctx, Http3HeadersFrame frame) throws NoSuchAlgorithmException {

        System.out.println("Server receive a header frame");

        String[] authHeader = frame.headers().get("authorization").toString().split(" ");

        if (authHeader[0].equals("Basic")) {
            String logData = new String(Base64.getDecoder().decode(authHeader[1]));
            System.out.println("logData: " + logData);

            if (authorization.checkUser(logData)) {

                Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                headersFrame.headers().status("202");
                headersFrame.headers().add("accesstoken", authorization.generateAccessJWT());
                headersFrame.headers().add("refreshtoken", authorization.generateRefreshJWT(logData.split(":")[2]));
                //headersFrame.headers().addInt("content-length", CONTENT.length);
                ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;

            } else {

                if (frame.headers().get("info") != null && frame.headers().get("info").toString().equals("reg")) {
                    System.out.println("---Registration");
                    authorization.registerUser(logData);
                    Http3HeadersFrame headersFrame2 = new DefaultHttp3HeadersFrame();
                    headersFrame2.headers().status("202");
                    headersFrame2.headers().add("accesstoken", authorization.generateAccessJWT());
                    headersFrame2.headers().add("refreshtoken", authorization.generateRefreshJWT(logData.split(":")[2]));
                    //headersFrame2.headers().addInt("content-length", CONTENT.length);
                    System.out.println(headersFrame2.headers());
                    ctx.writeAndFlush(headersFrame2).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;

                } else {


                    Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                    headersFrame.headers().status("401");
                    headersFrame.headers().add("info", "User not found, do u wanna sign up?");
                    //headersFrame.headers().addInt("content-length", CONTENT.length);
                    ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;
                }
                ReferenceCountUtil.release(frame);
            }
        } else if (authHeader[0].equals("Bearer")) {
            if (authorization.validateJWT(authHeader[1])) {
                if (authorization.getJWTType(authHeader[1]).equals("refreshtoken")) {
                    System.out.println("Got valid refreshtoken");
                    Http3HeadersFrame headersFrame2 = new DefaultHttp3HeadersFrame();
                    headersFrame2.headers().status("202");
                    headersFrame2.headers().add("accesstoken", authorization.generateAccessJWT());
                    headersFrame2.headers().add("refreshtoken", authorization.generateRefereshJWTFromJWT(authHeader[1]));
                    //headersFrame2.headers().addInt("content-length", CONTENT.length);
                    ctx.writeAndFlush(headersFrame2).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;
                    System.out.println("Server sent new at and rt");
                } else  {
                    System.out.println("Got valid accesstoken");
                    Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                    headersFrame.headers().status("202");
                    headersFrame.headers().add("info", "accessed");
                    headersFrame.headers().add("accesstoken", authorization.generateAccessJWT());
                    //headersFrame.headers().addInt("content-length", CONTENT.length);
                    ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;
                }
            } else if(frame.headers().get("info")!=null && frame.headers().get("info").toString().equals("refreshToken")){
                System.out.println("refresh token is not valid");
                Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                headersFrame.headers().status("401");
                headersFrame.headers().add("info", "refreshTokenExpired");
                //headersFrame.headers().addInt("content-length", CONTENT.length);
                ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;
            } else {
                System.out.println("access token is not valid");
                Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                headersFrame.headers().status("401");
                headersFrame.headers().add("info", "accessTokenExpired");
                //headersFrame.headers().addInt("content-length", CONTENT.length);
                ctx.writeAndFlush(headersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);;
            }
        }
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(
            ChannelHandlerContext ctx, Http3DataFrame frame) {
        System.out.println("-----------------");
        ReferenceCountUtil.release(frame);

    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        authorization = new Authorization(keyPair, sessionFactory);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        System.out.println("Channel unregistered");
    }
}
