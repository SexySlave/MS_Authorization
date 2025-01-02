package ms.netty.server;

import io.netty.incubator.codec.http3.Http3RequestStreamInboundHandler;

import java.lang.reflect.Method;

/**
 * <p>This class is a provider of API methods.</p>
 * <p>It`s a facade for invoking API methods which check all requirements before invoking (@RequiresAuthorization etc.).</p>
 * **/

public class APIProvider extends API {

    public static <T extends Http3RequestStreamInboundHandler> void invokeSecureOperation1(T handler) throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("secureOperation1");
        preInvokeCheck(method, handler);
        secureOperation1();
    }

    public static <T extends Http3RequestStreamInboundHandler> void invokeSecureOperation2(T handler) throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("secureOperation2");
        preInvokeCheck(method, handler);
        secureOperation2();
    }

    public static <T extends Http3RequestStreamInboundHandler> void invokeSecureOperation3(T handler) throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("secureOperation3");
        preInvokeCheck(method, handler);
        secureOperation3();
    }

    public static <T extends Http3RequestStreamInboundHandler> void invokeNonSecureOperation1(T handler) throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("nonSecureOperation1");
        preInvokeCheck(method, handler);
        nonSecureOperation1();
    }

    private static <T extends Http3RequestStreamInboundHandler> void preInvokeCheck(Method method, T handler) {
        // Проверяем, помечен ли метод аннотацией @RequiresAuthorization
        if (method.isAnnotationPresent(RequiresAuthorization.class)) {
            if (!isUserAuthorized(handler)) {
                throw new SecurityException("User is not authorized!");
            }
        }
    }

    private static <T extends Http3RequestStreamInboundHandler> boolean isUserAuthorized(T handler) {

        if (handler.getClass().isAnnotationPresent(Route.class)) {
            return handler.getClass().getAnnotation(Route.class).route().contains("/secure/");
        }
        return false;
    }
}
