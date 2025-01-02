package ms.netty.server;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Annotation for endpoint handlers.</p>
 * */

@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String route() default "";
}
