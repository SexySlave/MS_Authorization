package ms.netty.trash;

import io.netty.channel.ChannelHandlerContext;

public abstract class ServerHandlerAbstract {

    ChannelHandlerContext ctx;


    public void run(){}

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


}
