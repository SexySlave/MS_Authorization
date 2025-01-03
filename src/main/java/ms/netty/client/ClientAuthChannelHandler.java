package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.util.AttributeKey;
import io.netty.util.NetUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class ClientAuthChannelHandler extends Http3RequestStreamInboundHandler {

    private static boolean received401 = false;
    QuicChannel quicChannel;


    public ClientAuthChannelHandler(QuicChannel quicChannel) throws IOException {
        this.quicChannel = quicChannel;
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {

        if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401")) {
            received401 = true;
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

            String rt = UIHandler.getAccessAndRefreshTokens()[1];

            Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
            frame1.headers().method("GET").path("/secure").authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999).scheme("https").add("authorization", "Bearer " + rt).add("info", "refreshToken");

            createNewChannelAndSendRequest(quicChannel, frame1);
        } else if (frame.headers().get("info").toString().equals("refreshTokenExpired")) {
            System.out.println("Refresh token expired.");
            reLogin(ctx);
        } else {
            String answ = UIHandler.YesOrNotQuestion(frame.headers().get("info") + "\n" + "Write Y/N: \n");
            if (answ.equalsIgnoreCase("y")) {
                System.out.println("registration...");
                Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
                frame1.headers().method("GET").path("/secure").authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999).scheme("https").add("authorization", "Basic " + Base64.getEncoder().encodeToString(UIHandler.getLogdata().concat(":").concat(UIHandler.getMacAddress()).getBytes(StandardCharsets.UTF_8))).add("info", "reg");

                createNewChannelAndSendRequest(quicChannel, frame1);
            } else {
                System.exit(0);
            }
        }
    }

    private void handleAuthorized(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException, InterruptedException {
        if (frame.headers().get("refreshtoken") != null) {
            System.out.println("access and refresh tokens have been received, user authorized");
            System.out.println("accessToken: " + frame.headers().get("accesstoken"));
            System.out.println("refreshToken: " + frame.headers().get("refreshtoken"));
            UIHandler.saveTokensInFile(frame.headers().get("accesstoken").toString(), frame.headers().get("refreshtoken").toString());

            if (received401) {

                Http3HeadersFrame headersFrame = (Http3HeadersFrame) quicChannel.attr(AttributeKey.valueOf("HeaderFrame")).get();
                headersFrame.headers().set("authorization", "Bearer " + UIHandler.getAccessAndRefreshTokens()[0]);

                createNewChannelAndSendRequest(quicChannel, headersFrame, (Http3DataFrame) quicChannel.attr(AttributeKey.valueOf("DataFrame")).get());

                quicChannel.attr(AttributeKey.valueOf("HeaderFrame")).set("");
                quicChannel.attr(AttributeKey.valueOf("DataFrame")).set("");

                received401 = false;
            }


        } else {
            System.out.println("new access token have been received, user authorized");
            UIHandler.refreshAccessTokenInFile(frame.headers().get("accesstoken").toString());
        }
    }

    private void reLogin(ChannelHandlerContext ctx) throws IOException, InterruptedException {
        System.out.println("Trying to log in with user data: " + UIHandler.getLogdata());
        System.out.println("Encoding sensitive data...");

        Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
        frame1.headers().method("GET").path("/secure").authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999).scheme("https").add("authorization", "Basic " + Base64.getEncoder().encodeToString(UIHandler.getLogdata().concat(":").concat(UIHandler.getMacAddress()).getBytes(StandardCharsets.UTF_8)));
        createNewChannelAndSendRequest(quicChannel, frame1);
    }

    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3Frame... http3Frame) throws InterruptedException, IOException {
        Http3.newRequestStream(quicChannel, new ClientChannelHandlerDefault(quicChannel, http3Frame)).sync().getNow().closeFuture();
    }
}
