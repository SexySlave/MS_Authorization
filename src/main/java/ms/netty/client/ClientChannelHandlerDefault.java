package ms.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.Http3;
import io.netty.incubator.codec.http3.Http3DataFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;
import io.netty.incubator.codec.quic.QuicChannel;
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

public class ClientChannelHandlerDefault extends Http3RequestStreamInboundHandler {

    private final ClientChannelHandler_old.UIHandler handler = new ClientChannelHandler_old.UIHandler();
    private final Http3HeadersFrame http3HeadersFrame;
    private final QuicChannel quicChannel;
    private final Logger log = Logger.getLogger(ClientChannelHandler_old.class);

    private String logData;
    private final String logDataEncoded;
    private final String macAddress;
    private String accessToken;
    private String refreshToken;

    public ClientChannelHandlerDefault(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws IOException {
        this.quicChannel = quicChannel;
        this.http3HeadersFrame = http3HeadersFrame;

        // set upping macAddress
        macAddress = UIHandler.getMacAddress();
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

        if (http3HeadersFrame != null) {
            ctx.writeAndFlush(http3HeadersFrame);
        } else {
            // if we have tokens in file, we try to authorize with them
            if (accessToken == null || refreshToken == null) {
                // ur own logic...

            } else {
                // authorization via user data
                // ur own logic...

            }

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
        ReferenceCountUtil.release(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {

    }

    // function which creates new channel and sends request
    // its main idea of QUICK protocol to send each request in new channel
    public void createNewChannelAndSendRequest(QuicChannel quicChannel, Http3HeadersFrame http3HeadersFrame) throws InterruptedException, SocketException, UnknownHostException {
        log.debug("Creating new quicChannel");
        Http3.newRequestStream(quicChannel, new ClientChannelHandler_old(quicChannel, http3HeadersFrame)).sync().getNow().closeFuture();
    }

}
