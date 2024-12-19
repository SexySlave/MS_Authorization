package ms.netty_old.debug;

import ms.netty_old.client.EnterPoint;

import java.util.concurrent.BlockingQueue;

public class tclass {
    EnterPoint.Handler h;
    BlockingQueue<Runnable> q;

    public tclass(EnterPoint.Handler h, BlockingQueue<Runnable> queue){
        this.h = h;
        this.q = queue;

    }

    public void run(){
        q.add(new Runnable() {
            @Override
            public void run() {
                String s = h.YesOrNotQuestion("Give me smth");
                System.out.println(s + Thread.currentThread().getName());
            }
        });



    }
}
