//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.nettime.mobileimsdk.server.netty;

import com.gs.imsdk.protocol.UDPHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.openmob.mobileimsdk.server.ServerCoreHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//接受客户端的包
public class MBUDPClientInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static Logger logger = LoggerFactory.getLogger(MBUDPClientInboundHandler.class);
    private ServerCoreHandler serverCoreHandler = null;

    public MBUDPClientInboundHandler(ServerCoreHandler serverCoreHandler) {
        this.serverCoreHandler = serverCoreHandler;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        try {
            this.serverCoreHandler.exceptionCaught(ctx.channel(), e);
        } catch (Exception var4) {
            logger.warn(var4.getMessage(), e);
        }

    }

    //有新的连接
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.serverCoreHandler.sessionCreated(ctx.channel());
    }

   //断开，这个和用户退出登录不一样。
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.serverCoreHandler.sessionClosed(ctx.channel());
    }

    //接受数据包的接口。
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf bytebuf) throws Exception {
//        this.serverCoreHandler.messageReceived(ctx.channel(), bytebuf);
        UDPHandler.getInstance().udpReceive(ctx.channel(), bytebuf);
    }
}
