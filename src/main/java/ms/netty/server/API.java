package ms.netty.server;

/**
    * Example of server API
    * U must use it only trough APIProvider class
    **/



public class API {

    @RequiresAuthentication
    protected static void secureOperation1() {
        System.out.println("Executing secure operation1...");
    }

    @RequiresAuthentication
    protected static void secureOperation2() {
        System.out.println("Executing secure operation2...");
    }

    @RequiresAuthentication
    protected static void secureOperation3() {
        System.out.println("Executing secure operation3...");
    }

    protected static void nonSecureOperation1(){
        System.out.println("Executing non secure operation1...");
    }

}
