package ms.netty.client;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import ms.netty_old.client.ClientChannelHandler;
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

public class ClientChannelHandlerDefault extends Http3RequestStreamInboundHandler {


    private final Http3Frame[] http3Frame;
    private final QuicChannel quicChannel;
    private final Logger log = Logger.getLogger(ClientChannelHandler.class);

    private String logData;
    private final String logDataEncoded;
    private final String macAddress;
    private String accessToken;
    private String refreshToken;

    public ClientChannelHandlerDefault(QuicChannel quicChannel, Http3Frame... http3Frame) throws IOException {
        this.quicChannel = quicChannel;
        this.http3Frame = http3Frame;

        // set upping macAddress
        macAddress = getMacAddress();
        // set upping logData
        logData = UIHandler.getLogdata();
        logData = logData.concat(":" + macAddress);
        logDataEncoded = Base64.getEncoder().encodeToString(logData.getBytes(StandardCharsets.UTF_8));
        // set upping accessToken and refreshToken
        accessToken = UIHandler.getAccessAndRefreshTokens()[0];
        refreshToken = UIHandler.getAccessAndRefreshTokens()[1];
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        if (http3Frame != null) {
            for (Http3Frame frame : http3Frame) {
                ctx.write(frame);
            }
            ctx.flush();
        } else {
            Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
            headersFrame.headers().method("GET").path("/secure/api-all")
                    .authority(NetUtil.LOCALHOST4.getHostAddress() + ":" + 9999)
                    .scheme("https");
            Http3DataFrame dataFrame = new DefaultHttp3DataFrame(Unpooled.copiedBuffer("Hello, HTTP/3!".getBytes()));


            System.out.println("Sending message");
            ctx.write(headersFrame);
            ctx.write(dataFrame);
            ctx.flush();
        }


    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {

        // handling authorization status
        if (Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "401") |
                Objects.equals(Objects.requireNonNull(frame.headers().status()).toString(), "202")) {
            ctx.pipeline().addLast(new ClientAuthChannelHandler(quicChannel));
            ctx.fireChannelRead(frame);
        }
        // ur own logic...


        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        System.out.println("Received: " + frame.content().toString(StandardCharsets.UTF_8));
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }

    // function which creates new channel and sends request
    // its main idea of QUICK protocol to send each request in new channel
    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws InterruptedException, IOException {
        log.debug("Creating new quicChannel");
        Http3.newRequestStream(quicChannel, new ClientChannelHandlerDefault(quicChannel, http3HeadersFrame)).sync().getNow().closeFuture();
    }

    public  String getMacAddress() throws UnknownHostException, SocketException {
        byte[] hardwareAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
        String[] hexadecimal = new String[hardwareAddress.length];
        for (int i = 0; i < hardwareAddress.length; i++) {
            hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
        }
        return String.join("-", hexadecimal);
    }

}
