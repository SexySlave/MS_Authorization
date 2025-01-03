package ms.netty.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.incubator.codec.http3.*;
import ms.netty.server.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class is responsible for routing requests to the appropriate handler.</p>
 * **/

public class RouteHandler extends Http3RequestStreamInboundHandler {
    private final Map<String, Http3RequestStreamInboundHandler[]> routes = new HashMap<>();

    Http3RequestStreamInboundHandler[] streamInboundHandler;

    public RouteHandler() {
        // Определяем маршруты
        routes.put(SecureHandler.class.getAnnotation(Route.class).route(), new Http3RequestStreamInboundHandler[]{new SecureHandler()});
        routes.put(ApiAllHandler.class.getAnnotation(Route.class).route(), new Http3RequestStreamInboundHandler[]{new AuthHandler(), new ApiAllHandler()});
        routes.put(ApiHandler.class.getAnnotation(Route.class).route(), new Http3RequestStreamInboundHandler[]{new ApiHandler()});
        routes.put(MainHandler.class.getAnnotation(Route.class).route(), new Http3RequestStreamInboundHandler[]{new MainHandler()});
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3HeadersFrame frame) throws Exception {
        String route = String.valueOf(frame.headers().path());
        System.out.println("route: " + route);
        // Ищем обработчик для указанного пути
        streamInboundHandler = routes.get(route);
        if (streamInboundHandler != null) {
            for (Http3RequestStreamInboundHandler http3RequestStreamInboundHandler : streamInboundHandler) {
                ctx.pipeline().addLast(http3RequestStreamInboundHandler);
            }
            ctx.fireChannelRead(frame);
        } else {
            sendNotFound(ctx);
        }
    }

    @Override
    protected void channelRead(ChannelHandlerContext ctx, Http3DataFrame frame) throws Exception {
        ctx.fireChannelRead(frame);
    }

    @Override
    protected void channelInputClosed(ChannelHandlerContext ctx) throws Exception {
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        Http3HeadersFrame frame = new DefaultHttp3HeadersFrame();
        frame.headers().status("404");
        frame.headers().add("info", "route not found");
        ctx.writeAndFlush(frame);
    }
}


