package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LogLevel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
//FileReader fileReader = new FileReader("tokens.txt");



public class ClientChannelHandler extends Http3RequestStreamInboundHandler {

    UIHandler handler = new UIHandler();
    Http3HeadersFrame http3HeadersFrame;

    QuicChannel quicChannel;
    Logger log = Logger.getLogger(ClientChannelHandler.class);

    String logData = "lolipopssssss:qwerty12223";
    String logDataEncoded = Base64.getEncoder().encodeToString(logData.getBytes(StandardCharsets.UTF_8));


    public ClientChannelHandler (QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) {
        this.quicChannel=quicChannel;
        this.http3HeadersFrame=http3HeadersFrame;
    }


    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        if (http3HeadersFrame!=null){
            ctx.writeAndFlush(http3HeadersFrame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
        } else {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens")); // token location.
                                                                                                                        // it can be everything (file, ur local DB, etc.)
            String at = reader.readLine();
            String rt = reader.readLine();
            reader.close();

            if (at!=null && rt!=null){
                at = at.split(" ")[1];
                rt = rt.split(" ")[1];
            }

            System.out.println("Connecting to server from IP: " +  NetUtil.LOCALHOST4.getHostAddress());
            if (at==null | rt==null ) {
                System.out.println("Trying to log in with user data: " + logData);
                System.out.println("Encoding sensitive data...");

                Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
                frame.headers().method("GET").path("/")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https").add("authorization", "Basic "+ logDataEncoded);
                ctx.writeAndFlush(frame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);

            } else {
                System.out.println("Tokens are detected. Trying to connect... ");

                Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
                frame.headers().method("GET").path("/")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https").add("authorization", "Bearer "+ at);
                ctx.writeAndFlush(frame).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);

            }
        }
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException, InterruptedException {
        if (frame.headers().status() == null){
            System.out.println("null status");
            try {
                handler.saveTokensInFile(frame.headers().get("accesstoken").toString(), frame.headers().get("refreshtoken").toString());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401")){
            if (frame.headers().get("info").toString().equals("accessTokenExpired")){
                System.out.println("access token expired, sending refresh token...");

                BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
                String at = reader.readLine();
                String rt = reader.readLine();
                reader.close();
                System.out.println(rt);
                rt = rt.split(" ")[1];

                Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
                frame1.headers().method("GET").path("/")
                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                        .scheme("https")
                        .add("authorization", "Bearer "+ rt)
                        .add("info", "refreshToken");

                createNewChannelAndSendRequest(quicChannel, frame1);
            } else if(frame.headers().get("info").toString().equals("refreshTokenExpired")){
                System.out.println("Refresh token expired. Clean file with tokens and try again!");
            } else {
                String answ = handler.YesOrNotQuestion(frame.headers().get("info") + "\n" + "Write Y/N: \n");
                if (answ.equalsIgnoreCase("y")) {
                    System.out.println("registration...");
                    Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
                    frame1.headers().method("GET").path("/")
                            .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                            .scheme("https");
                    frame1.headers().add("authorization", "Basic " + logDataEncoded).add("info", "reg");

                    createNewChannelAndSendRequest(quicChannel, frame1);
                } else {
                    System.exit(0);
                }
            }
        }  else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "202")) {
            if (frame.headers().get("refreshtoken")!=null){
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
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) {
        System.err.print(frame.content().toString(CharsetUtil.US_ASCII));
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) {
        System.out.println("ctxclosed");
        ctx.close();
    }

    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws InterruptedException {
        log.debug("Creating new quicChannel");
        Http3.newRequestStream(quicChannel, new ClientChannelHandler(quicChannel, http3HeadersFrame)).sync().getNow().closeFuture();;
    }

    public static class UIHandler{
        public void makeToast(String s){
            System.out.println(s);
        }
        public String YesOrNotQuestion(String q){
            Scanner in = new Scanner(System.in);
            System.out.print(q);
            String e = in.next();
            return e;
        }
        public String requestLogData(){
            Scanner in = new Scanner(System.in);
            System.out.print("Please enter ur login and password"+"\nlogin: ");
            String l = in.next();
            System.out.println("\npassword: ");
            String p = in.next();
            return l+":"+p.replace(":","_"); // replacing illegal char
        }

        public void saveTokensInFile(String at, String rt) throws IOException {
            PrintWriter writer = new PrintWriter("src/main/java/ms/netty/client/tokens", StandardCharsets.UTF_8);
            writer.println("accessToken: "+ at);
            writer.println("refreshToken: " + rt);
            writer.close();

        }

        public void refreshAccessTokenInFile(String at) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
            String s = reader.readLine();
            String rt = reader.readLine();
            reader.close();

            saveTokensInFile(at, rt.split(" ")[1]);
        }
    }

}
