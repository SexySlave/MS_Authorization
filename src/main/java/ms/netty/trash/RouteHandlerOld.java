package ms.netty.trash;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RouteHandlerOld extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Map<String, BiConsumer<ChannelHandlerContext, FullHttpRequest>> routes = new HashMap<>();

    public RouteHandlerOld() {
        // Определяем маршруты
        routes.put("/api", this::handleGetUsers);
       // routes.put("/users/create", this::handleCreateUser);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        String path = uri.split("\\?")[0]; // Убираем параметры запроса

        // Ищем обработчик для указанного пути
        BiConsumer<ChannelHandlerContext, FullHttpRequest> handler = routes.get(path);
        if (handler != null) {
            handler.accept(ctx, request);
        } else {
            sendNotFound(ctx);
        }
    }

    private void handleGetUsers(ChannelHandlerContext ctx, FullHttpRequest request) {
        // Логика обработки запроса GET /users
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                ctx.alloc().buffer().writeBytes("[{\"id\":1,\"name\":\"Alice\"}]".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        ctx.writeAndFlush(response);
    }

    private void handleCreateUser(ChannelHandlerContext ctx, FullHttpRequest request) {
        // Логика обработки запроса POST /users/create
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.CREATED
        );
        ctx.writeAndFlush(response);
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND
        );
        ctx.writeAndFlush(response);
    }
}
