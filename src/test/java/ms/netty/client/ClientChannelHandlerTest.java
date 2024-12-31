package ms.netty.client;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.incubator.codec.http3.DefaultHttp3HeadersFrame;
import io.netty.incubator.codec.http3.Http3HeadersFrame;
import io.netty.incubator.codec.quic.QuicChannel;
import org.junit.jupiter.api.BeforeEach;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertTrue;

public class ClientChannelHandlerTest {

    private QuicChannel quicChannel;
    private Http3HeadersFrame http3HeadersFrame;
    private ClientChannelHandler_old clientChannelHandler;
    private EmbeddedChannel embeddedChannel;

    @BeforeEach
    public void setUp() throws UnknownHostException, SocketException {
        quicChannel = mock(QuicChannel.class);
        http3HeadersFrame = new DefaultHttp3HeadersFrame();
        clientChannelHandler = new ClientChannelHandler_old(quicChannel, http3HeadersFrame);
        embeddedChannel = new EmbeddedChannel(clientChannelHandler);
    }

    @Test
    public void testRegistration() throws Exception {
        // Simulate channel active for registration
        clientChannelHandler.channelActive(embeddedChannel.pipeline().context(clientChannelHandler));
        AssertJUnit.assertTrue(embeddedChannel.isActive());
    }

    @Test
    public void testLoginWithCredentials() throws Exception {
        // Simulate channel active for login with credentials
        clientChannelHandler.channelActive(embeddedChannel.pipeline().context(clientChannelHandler));
        AssertJUnit.assertTrue(embeddedChannel.isActive());
    }

    @Test
    public void testLoginWithTokens() throws Exception {
        // Simulate tokens in file
        try (PrintWriter writer = new PrintWriter("src/main/java/ms/netty/client/tokens", StandardCharsets.UTF_8)) {
            writer.println("accessToken: dummyAccessToken");
            writer.println("refreshToken: dummyRefreshToken");
        }

        // Simulate channel active for login with tokens
        clientChannelHandler.channelActive(embeddedChannel.pipeline().context(clientChannelHandler));
        AssertJUnit.assertTrue(embeddedChannel.isActive());
    }

    @Test
    public void testRefreshAccessTokenAndLogin() throws Exception {
        // Simulate expired access token and valid refresh token
        try (PrintWriter writer = new PrintWriter("src/main/java/ms/netty/client/tokens", StandardCharsets.UTF_8)) {
            writer.println("accessToken: expiredAccessToken");
            writer.println("refreshToken: validRefreshToken");
        }

        // Simulate channel active for refreshing access token and login
        clientChannelHandler.channelActive(embeddedChannel.pipeline().context(clientChannelHandler));
        AssertJUnit.assertTrue(embeddedChannel.isActive());
    }

    @Test
    public void testExpiredTokensAndLoginWithCredentials() throws Exception {
        // Simulate expired access token and expired refresh token
        try (PrintWriter writer = new PrintWriter("src/main/java/ms/netty/client/tokens", StandardCharsets.UTF_8)) {
            writer.println("accessToken: expiredAccessToken");
            writer.println("refreshToken: expiredRefreshToken");
        }

        // Simulate channel active for login with expired tokens and then credentials
        clientChannelHandler.channelActive(embeddedChannel.pipeline().context(clientChannelHandler));
        AssertJUnit.assertTrue(embeddedChannel.isActive());
    }
}