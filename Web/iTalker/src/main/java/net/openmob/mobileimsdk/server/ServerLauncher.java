//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import net.nettime.mobileimsdk.server.bridge.QoS4ReciveDaemonC2B;
import net.nettime.mobileimsdk.server.bridge.QoS4SendDaemonB2C;
import net.nettime.mobileimsdk.server.netty.MBUDPClientInboundHandler;
import net.nettime.mobileimsdk.server.netty.MBUDPServerChannel;
import net.openmob.mobileimsdk.server.event.MessageQoSEventListenerS2C;
import net.openmob.mobileimsdk.server.event.ServerEventListener;
import net.openmob.mobileimsdk.server.qos.QoS4ReciveDaemonC2S;
import net.openmob.mobileimsdk.server.qos.QoS4SendDaemonS2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerLauncher {
    private static Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
    /** @deprecated */
    public static boolean debug = true;
    public static String appKey = null;
    public static int PORT = 7901;
    public static int SESION_RECYCLER_EXPIRE = 10;
    public static boolean bridgeEnabled = false;  //桥接，与Web端的消息互通的桥接器
    private boolean running = false;
    protected ServerCoreHandler serverCoreHandler = null;  //处理接受到的包
    private final EventLoopGroup __bossGroup4Netty = new NioEventLoopGroup();
    private final EventLoopGroup __workerGroup4Netty = new DefaultEventLoopGroup();
    private Channel __serverChannel4Netty = null;

    public ServerLauncher() throws IOException {
    }

    public boolean isRunning() {
        return this.running;
    }

    public void startup() throws Exception {
        if (!this.running) {
            this.serverCoreHandler = this.initServerCoreHandler();
            this.initListeners();
            ServerBootstrap bootstrap = this.initServerBootstrap4Netty();
            QoS4ReciveDaemonC2S.getInstance().startup();
            QoS4SendDaemonS2C.getInstance().startup(true).setServerLauncher(this);
            if (bridgeEnabled) {
                QoS4ReciveDaemonC2B.getInstance().startup();
                QoS4SendDaemonB2C.getInstance().startup(true).setServerLauncher(this);
                this.serverCoreHandler.lazyStartupBridgeProcessor();
                logger.info("[IMCORE-netty] 配置项：已开启与MobileIMSDK Web的互通.");
            } else {
                logger.info("[IMCORE-netty] 配置项：未开启与MobileIMSDK Web的互通.");
            }

            ChannelFuture cf = bootstrap.bind("0.0.0.0", PORT).syncUninterruptibly();
            this.__serverChannel4Netty = cf.channel();
            this.running = true;
            logger.info("[IMCORE-netty] 基于MobileIMSDK的UDP服务正在端口" + PORT + "上监听中...");
            this.__serverChannel4Netty.closeFuture().await();
        } else {
            logger.warn("[IMCORE-netty] 基于MobileIMSDK的UDP服务正在运行中，本次startup()失败，请先调用shutdown()后再试！");
        }

    }

    public void shutdown() {
        if (this.__serverChannel4Netty != null) {
            this.__serverChannel4Netty.close();
        }

        this.__bossGroup4Netty.shutdownGracefully();
        this.__workerGroup4Netty.shutdownGracefully();
        QoS4ReciveDaemonC2S.getInstance().stop();
        QoS4SendDaemonS2C.getInstance().stop();
        if (bridgeEnabled) {
            QoS4ReciveDaemonC2B.getInstance().stop();
            QoS4SendDaemonB2C.getInstance().stop();
        }

        this.running = false;
    }

    protected ServerCoreHandler initServerCoreHandler() {
        return new ServerCoreHandler();
    }

    protected abstract void initListeners();

    //Netty 中 Bootstrap 类是客户端程序的启动引导类，ServerBootstrap 是服务端启动引导类。
    protected ServerBootstrap initServerBootstrap4Netty() {
        return ((ServerBootstrap)(new ServerBootstrap())
                .group(this.__bossGroup4Netty, this.__workerGroup4Netty)
                .channel(MBUDPServerChannel.class))
                .childHandler(this.initChildChannelHandler4Netty());
    }

    protected ChannelHandler initChildChannelHandler4Netty() {
        return new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new ChannelHandler[]{new ReadTimeoutHandler(ServerLauncher.SESION_RECYCLER_EXPIRE)})
                        .addLast(new ChannelHandler[]{new MBUDPClientInboundHandler(ServerLauncher.this.serverCoreHandler)});
            }
        };
    }

    public ServerEventListener getServerEventListener() {
        return this.serverCoreHandler.getServerEventListener();
    }

    public void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverCoreHandler.setServerEventListener(serverEventListener);
    }

    public MessageQoSEventListenerS2C getServerMessageQoSEventListener() {
        return this.serverCoreHandler.getServerMessageQoSEventListener();
    }

    public void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener) {
        this.serverCoreHandler.setServerMessageQoSEventListener(serverMessageQoSEventListener);
    }

    public ServerCoreHandler getServerCoreHandler() {
        return this.serverCoreHandler;
    }
}
