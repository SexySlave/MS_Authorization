package ms.netty_old.trash;

public class DefaultServerHandler extends ServerHandlerAbstract {
    @Override
    public void run() {
        System.out.println("DefaulServerHandler got the message:" + ctx);
    }
}
