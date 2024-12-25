package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

public class ClientChannelHandler extends Http3RequestStreamInboundHandler {

    private final UIHandler handler = new UIHandler();
    private final Http3HeadersFrame http3HeadersFrame;
    private final QuicChannel quicChannel;
    private final Logger log = Logger.getLogger(ClientChannelHandler.class);

    private String logData = "lolipopssss77121ww22214433231233221112221133444337ss:qwerty12223";
    private final String logDataEncoded;
    private final String macAddress;

    public ClientChannelHandler(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws UnknownHostException, SocketException {
        this.quicChannel = quicChannel;
        this.http3HeadersFrame = http3HeadersFrame;

        byte[] hardwareAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
        String[] hexadecimal = new String[hardwareAddress.length];
        for (int i = 0; i < hardwareAddress.length; i++) {
            hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
        }
        macAddress = String.join("-", hexadecimal);
        logData = logData.concat(":" + macAddress);
        logDataEncoded = Base64.getEncoder().encodeToString(logData.getBytes(StandardCharsets.UTF_8));
        System.out.println(logData);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        if (http3HeadersFrame != null) {
            ctx.writeAndFlush(http3HeadersFrame);
        } else {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
            String at = reader.readLine();
            String rt = reader.readLine();
            reader.close();

            if (at != null && rt != null) {
                at = at.split(" ")[1];
                rt = rt.split(" ")[1];
            }

            System.out.println("Connecting to server from IP: " + NetUtil.LOCALHOST4.getHostAddress());
            if (at == null || rt == null) {
                System.out.println("Trying to log in with user data: " + logData);
                System.out.println("Encoding sensitive data...");

                Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
                frame.headers().method("GET").path("/")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https").add("authorization", "Basic " + logDataEncoded);
                ctx.writeAndFlush(frame);
            } else {
                System.out.println("Tokens are detected. Trying to connect...");

                Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
                frame.headers().method("GET").path("/")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https").add("authorization", "Bearer " + at)
                        .add("info", "refreshToken");
                ctx.writeAndFlush(frame);
            }
        }
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException, InterruptedException {
        if (frame.headers().status() == null) {
            System.out.println("null status");
            try {
                handler.saveTokensInFile(frame.headers().get("accesstoken").toString(), frame.headers().get("refreshtoken").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401")) {
            handleUnauthorized(ctx, frame);
        } else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "202")) {
            handleAuthorized(ctx, frame);
        }
        ReferenceCountUtil.release(frame);
    }

    private void handleUnauthorized(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException, InterruptedException {
        if (frame.headers().get("info").toString().equals("accessTokenExpired")) {
            System.out.println("access token expired, sending refresh token...");

            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
            String rt = reader.readLine().split(" ")[1];
            reader.close();

            Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
            frame1.headers().method("GET").path("/")
                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                    .scheme("https")
                    .add("authorization", "Bearer " + rt)
                    .add("info", "refreshToken");

            createNewChannelAndSendRequest(quicChannel, frame1);
        } else if (frame.headers().get("info").toString().equals("refreshTokenExpired")) {
            System.out.println("Refresh token expired.");
            reLogin(ctx);
        } else {
            String answ = handler.YesOrNotQuestion(frame.headers().get("info") + "\n" + "Write Y/N: \n");
            if (answ.equalsIgnoreCase("y")) {
                System.out.println("registration...");
                Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
                frame1.headers().method("GET").path("/")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https")
                        .add("authorization", "Basic " + logDataEncoded)
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
            handler.saveTokensInFile(frame.headers().get("accesstoken").toString(), frame.headers().get("refreshtoken").toString());
        } else {
            System.out.println("new access token have been received, user authorized");
            handler.refreshAccessTokenInFile(frame.headers().get("accesstoken").toString());
        }
        ctx.close();
    }

    private void reLogin(ChannelHandlerContext ctx) throws IOException, InterruptedException {
        System.out.println("Trying to log in with user data: " + logData);
        System.out.println("Encoding sensitive data...");

        Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
        frame1.headers().method("GET").path("/")
                .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                .scheme("https").add("authorization", "Basic " + logDataEncoded);
        createNewChannelAndSendRequest(quicChannel, frame1);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) {
        System.err.print(frame.content().toString(CharsetUtil.US_ASCII));
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) {
        System.out.println("ctxclosed");
    }

    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws InterruptedException, SocketException, UnknownHostException {
        log.debug("Creating new quicChannel");
        Http3.newRequestStream(quicChannel, new ClientChannelHandler(quicChannel, http3HeadersFrame)).sync().getNow().closeFuture();
    }

    public static class UIHandler {
        public void makeToast(String s) {
            System.out.println(s);
        }

        public String YesOrNotQuestion(String q) {
            Scanner in = new Scanner(System.in);
            System.out.print(q);
            return in.next();
        }

        public String requestLogData() {
            Scanner in = new Scanner(System.in);
            System.out.print("Please enter ur login and password" + "\nlogin: ");
            String l = in.next();
            System.out.println("\npassword: ");
            String p = in.next();
            return l + ":" + p.replace(":", "_"); // replacing illegal char
        }

        public void saveTokensInFile(String at, String rt) throws IOException {
            try (PrintWriter writer = new PrintWriter("src/main/java/ms/netty/client/tokens", StandardCharsets.UTF_8)) {
                writer.println("accessToken: " + at);
                writer.println("refreshToken: " + rt);
            }
        }

        public void refreshAccessTokenInFile(String at) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"))) {
                String rt = reader.readLine().split(" ")[1];
                saveTokensInFile(at, rt);
            }
        }
    }
}