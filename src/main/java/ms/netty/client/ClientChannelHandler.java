package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;
//FileReader fileReader = new FileReader("tokens.txt");

public class ClientChannelHandler extends Http3RequestStreamInboundHandler {

    UIHandler handler = new UIHandler();

    String logData = "lolipop:qwerty12223";
    String logDataEncoded = Base64.getEncoder().encodeToString(logData.getBytes(StandardCharsets.UTF_8));

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
        String at = reader.readLine();
        String rt = reader.readLine();
        reader.close();

        System.out.println(at +"\n"+ rt);
        if (at!=null && rt!=null){
            at = at.split(" ")[1];
            rt = rt.split(" ")[1];
        }



        if (at==null | rt==null ) {
            Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
            System.out.println(NetUtil.LOCALHOST4.getHostAddress());
            System.out.println(logData);
            frame.headers().method("GET").path("/")
                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                    .scheme("https").add("authorization", "Basic "+ logDataEncoded);
            ctx.writeAndFlush(frame);
            System.out.println(frame.headers());
        } else {
            Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
            System.out.println(NetUtil.LOCALHOST4.getHostAddress());
            frame.headers().method("GET").path("/")
                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                    .scheme("https").add("authorization", "Bearer "+ at);
            ctx.writeAndFlush(frame);
            System.out.println(frame.headers());
        }









    }


    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws IOException {
        System.out.println("Got header");
        if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401")){
            System.out.println("---1");
            if (frame.headers().get("info").toString().equals("tokenExpired")){
                System.out.println("---2");
                System.out.println(frame.headers().get("info").toString());

                BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
                String at = reader.readLine();
                String rt = reader.readLine();
                reader.close();
                System.out.println(rt);
                rt = rt.split(" ")[1];

                Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
//                frame1.headers().method("GET").path("/")
//                        .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
//                        .scheme("https")
//                        .add("authorization", "Bearer "+ rt);
                frame1.headers()
                        .add("authorization", "Bearer "+ rt);
                System.out.println(frame1.headers());
                ctx.writeAndFlush(frame1);




            } else {
                System.out.println("---3");
                String answ = handler.YesOrNotQuestion(frame.headers().get("info") + "\n" + "Write Y/N: \n");
                if (answ.equalsIgnoreCase("y")) {
                    System.out.println("---4");
                    System.out.println("Registration");
                    Http3HeadersFrame frame1 = new DefaultHttp3HeadersFrame();
                    frame1.headers().add("authorization", "Basic " + logDataEncoded).add("info", "reg");

                    ctx.writeAndFlush(frame1);
                    System.out.println(frame1.headers());

                }//else{ReferenceCountUtil.release(frame);}
            }
        } else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "200")) {
            System.out.println("---5");
            System.out.println("accessToken: " + frame.headers().get("accesstoken"));
            System.out.println("refreshToken: " + frame.headers().get("refreshtoken"));
            handler.saveTokensInFile(frame.headers().get("accesstoken").toString(), frame.headers().get("refreshtoken").toString());

        } else if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "202")) {
            System.out.println("---6");
            System.out.println("Token accessed");
            handler.refreshAccessTokenInFile(frame.headers().get("accesstoken").toString());
        }
        //else{ReferenceCountUtil.release(frame);}
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

            saveTokensInFile(at, rt);
        }
    }

}
