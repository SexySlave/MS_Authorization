package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.util.NetUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class ClientAuthChannelHandler extends Http3RequestStreamInboundHandler {

    QuicChannel quicChannel;

    public ClientAuthChannelHandler(QuicChannel quicChannel) throws IOException {
        this.quicChannel = quicChannel;
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401")) {
            handleUnauthorized(ctx, frame);
        } else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "202")) {
            handleAuthorized(ctx, frame);
        }
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {

    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }

    private void handleUnauthorized(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException, InterruptedException {
        if (frame.headers().get("info").toString().equals("accessTokenExpired")) {
            System.out.println("access token expired, sending refresh token...");

            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
            String rt = reader.readLine().split(" ")[1];
            reader.close();

            Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
            frame1.headers().method("GET").path("/secure/api-all")
                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                    .scheme("https")
                    .add("authorization", "Bearer " + rt)
                    .add("info", "refreshToken");

            createNewChannelAndSendRequest(quicChannel, frame1);
        } else if (frame.headers().get("info").toString().equals("refreshTokenExpired")) {
            System.out.println("Refresh token expired.");
            reLogin(ctx);
        } else {
            String answ = UIHandler.YesOrNotQuestion(frame.headers().get("info") + "\n" + "Write Y/N: \n");
            if (answ.equalsIgnoreCase("y")) {
                System.out.println("registration...");
                Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
                frame1.headers().method("GET").path("/secure/api-all")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https")
                        .add("authorization", "Basic " + Base64.getEncoder().encodeToString(UIHandler.getLogdata().getBytes(StandardCharsets.UTF_8)))
                        .add("info", "reg");

                createNewChannelAndSendRequest(quicChannel, frame1);
            } else {
                System.exit(0);
            }
        }
    }
    private void handleAuthorized(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException {
        if (frame.headers().get("refreshtoken") != null) {
            System.out.println("access and refresh tokens have been received, user authorized");
            System.out.println("accessToken: " + frame.headers().get("accesstoken"));
            System.out.println("refreshToken: " + frame.headers().get("refreshtoken"));
            UIHandler.saveTokensInFile(frame.headers().get("accesstoken").toString(), frame.headers().get("refreshtoken").toString());
        } else {
            System.out.println("new access token have been received, user authorized");
            UIHandler.refreshAccessTokenInFile(frame.headers().get("accesstoken").toString());
        }
        ctx.close();
    }

    private void reLogin(ChannelHandlerContext ctx) throws IOException, InterruptedException {
        System.out.println("Trying to log in with user data: " + UIHandler.getLogdata());
        System.out.println("Encoding sensitive data...");

        Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
        frame1.headers().method("GET").path("/secure/api-all")
                .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                .scheme("https").add("authorization", "Basic " + Base64.getEncoder().encodeToString(UIHandler.getLogdata().getBytes(StandardCharsets.UTF_8)));
        createNewChannelAndSendRequest(quicChannel, frame1);
    }

    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws InterruptedException, SocketException, UnknownHostException {
        Http3.newRequestStream(quicChannel, new ClientChannelHandler_old(quicChannel, http3HeadersFrame)).sync().getNow().closeFuture();
    }
}
