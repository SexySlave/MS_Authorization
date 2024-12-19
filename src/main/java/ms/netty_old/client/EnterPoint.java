package ms.netty_old.client;

import ms.netty_old.debug.tclass;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EnterPoint {
    public static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();


    static int a = 4;
public static  void  main(String... args){
    System.out.println(Thread .currentThread().getName());
    run();

}


    public static void run(){
    Handler h = new Handler();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                tclass t = new tclass(h, queue);
                t.run();
                System.out.println("ended");

            }
        });
        t.setName("newThread");
        t.start();

    }

    public static class Handler{
        public void makeToast(String s){
            System.out.println(s);
        }
        public String YesOrNotQuestion(String q){
            Scanner in = new Scanner(System.in);
            System.out.print(q + Thread.currentThread().getName());
            String e = in.next();
            return e;
        }
    }


}
