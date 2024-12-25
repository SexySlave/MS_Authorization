package ms.netty.server;

import java.lang.reflect.Method;

public class APIProvider extends API{


    public static void invoke(Object obj, String methodName, Object... args) throws Exception {
        // Получаем метод по имени
        Method method = obj.getClass().getMethod(methodName);

        // Проверяем, помечен ли метод аннотацией @RequiresAuthentication
        if (method.isAnnotationPresent(RequiresAuthentication.class)) {
            // Проверяем аутентификацию
            if (!isUserAuthenticated()) {
                throw new SecurityException("User is not authenticated!");
            }
        }

        // Вызываем метод
        method.invoke(obj, args);
    }


    public static void invokeSecureOperation1() throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("secureOperation1");
        preInvokeCheck(method);
        secureOperation1();
    }
    public static void invokeSecureOperation2() throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("secureOperation2");
        preInvokeCheck(method);
        secureOperation2();
    }
    public static void invokeSecureOperation3() throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("secureOperation3");
        preInvokeCheck(method);
        secureOperation3();
    }
    public static void invokeNonSecureOperation1() throws NoSuchMethodException {
        Method method = API.class.getDeclaredMethod("nonSecureOperation1");
        preInvokeCheck(method);
        nonSecureOperation1();
    }

    private static void preInvokeCheck(Method method){
        // Проверяем, помечен ли метод аннотацией @RequiresAuthentication
        if (method.isAnnotationPresent(RequiresAuthentication.class)) {
            // Проверяем аутентификацию
            if (!isUserAuthenticated()) {
                throw new SecurityException("User is not authenticated!");
            }
        }
    }

    /**
     * Пример проверки аутентификации.
     *
     * @return true, если пользователь аутентифицирован, иначе false.
     */
    private static boolean isUserAuthenticated() {
        // Реализуйте свою логику аутентификации
        return true; // Здесь возвращаем true для примера
    }
}
