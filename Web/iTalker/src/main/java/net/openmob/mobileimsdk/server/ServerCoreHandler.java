//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import net.openmob.mobileimsdk.server.event.MessageQoSEventListenerS2C;
import net.openmob.mobileimsdk.server.event.ServerEventListener;
import net.openmob.mobileimsdk.server.processor.BridgeProcessor;
import net.openmob.mobileimsdk.server.processor.LogicProcessor;
import net.openmob.mobileimsdk.server.processor.OnlineProcessor;
import net.openmob.mobileimsdk.server.protocal.Protocal;
import net.openmob.mobileimsdk.server.utils.LocalSendHelper;
import net.openmob.mobileimsdk.server.utils.ServerToolKits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerCoreHandler {
    public static final int MESSAGE_TYPE_LOGIN = 0;
    public static final int MESSAGE_TYPE_KEEP_ALIVE = 1;
    public static final int MESSAGE_TYPE_COMMON = 2;  //通用消息
    public static final int MESSAGE_TYPE_EXIT = 3;
    public static final int MESSAGE_TYPE_ACK = 4;
    public static final int MESSAGE_TYPE_UNKNOW = 5;   //暂时不知道什么类型
    private static Logger logger = LoggerFactory.getLogger(ServerCoreHandler.class);
    protected ServerEventListener serverEventListener = null;
    protected MessageQoSEventListenerS2C serverMessageQoSEventListener = null;
    protected LogicProcessor logicProcessor = null;  //逻辑处理
    protected BridgeProcessor bridgeProcessor = null;

    public ServerCoreHandler() {
        this.logicProcessor = this.createLogicProcessor();
        if (ServerLauncher.bridgeEnabled) {
            this.bridgeProcessor = this.createBridgeProcessor();
        }

    }

    protected LogicProcessor createLogicProcessor() {
        return new LogicProcessor(this);
    }

    protected BridgeProcessor createBridgeProcessor() {
        BridgeProcessor bp = new BridgeProcessor() {
            protected void realtimeC2CSuccessCallback(Protocal p) {
                ServerCoreHandler.this.serverEventListener.onTransBuffer_C2C_CallBack(p);
            }

            protected boolean offlineC2CProcessCallback(Protocal p) {
                return ServerCoreHandler.this.serverEventListener.onTransBuffer_C2C_RealTimeSendFaild_CallBack(p);
            }
        };
        return bp;
    }

    public void lazyStartupBridgeProcessor() {
        if (ServerLauncher.bridgeEnabled && this.bridgeProcessor != null) {
            this.bridgeProcessor.start();
        }

    }

    public void exceptionCaught(Channel session, Throwable cause) throws Exception {
        logger.debug("[IMCORE-netty]此客户端的Channel抛出了exceptionCaught，原因是：" + cause.getMessage() + "，可以提前close掉了哦！", cause);
        session.close();
    }

    public void messageReceived(Channel session, ByteBuf bytebuf) throws Exception {
        Protocal pFromClient = ServerToolKits.fromIOBuffer(bytebuf);
        String remoteAddress = ServerToolKits.clientInfoToString(session);
        switch(pFromClient.getType()) {
            case MESSAGE_TYPE_LOGIN:
                this.logicProcessor.processLogin(session, pFromClient, remoteAddress);
                break;
            case MESSAGE_TYPE_KEEP_ALIVE:
                if (!OnlineProcessor.isLogined(session)) {
                    LocalSendHelper.replyDataForUnlogined(session, pFromClient, (MBObserver)null);
                    return;
                }

                this.logicProcessor.processKeepAlive(session, pFromClient, remoteAddress);
                break;
            case MESSAGE_TYPE_COMMON:
                logger.info("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的通用数据发送请求.");
                if (this.serverEventListener != null) {
                    if (!OnlineProcessor.isLogined(session)) {
                        LocalSendHelper.replyDataForUnlogined(session, pFromClient, (MBObserver)null);
                        return;
                    }

                    if ("0".equals(pFromClient.getTo())) {
                        this.logicProcessor.processC2SMessage(session, pFromClient, remoteAddress);
                    } else {
                        this.logicProcessor.processC2CMessage(this.bridgeProcessor, session, pFromClient, remoteAddress);
                    }
                } else {
                    logger.warn("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的通用数据传输消息，但回调对象是null，回调无法继续.");
                }
                break;
            case MESSAGE_TYPE_EXIT:
                logger.info("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的退出登陆请求.");
                session.close();
                break;
            case MESSAGE_TYPE_ACK:
                logger.info("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的ACK应答包发送请求.");
                if (!OnlineProcessor.isLogined(session)) {
                    LocalSendHelper.replyDataForUnlogined(session, pFromClient, (MBObserver)null);
                    return;
                }

                this.logicProcessor.processACK(pFromClient, remoteAddress);
                break;
            case MESSAGE_TYPE_UNKNOW:
                pFromClient.setType(53);
                LocalSendHelper.sendData(session, pFromClient, (MBObserver)null);
                break;
            default:
                logger.warn("[IMCORE-netty]【注意】收到的客户端" + remoteAddress + "消息类型：" + pFromClient.getType() + "，但目前该类型服务端不支持解析和处理！");
        }

    }

    public void sessionClosed(Channel session) throws Exception {
        String user_id = OnlineProcessor.getUserIdFromSession(session);
        if (user_id != null) {
            Channel sessionInOnlinelist = OnlineProcessor.getInstance().getOnlineSession(user_id);
            logger.info("[IMCORE-netty]" + ServerToolKits.clientInfoToString(session) + "的会话已关闭(user_id=" + user_id + ")了...");
            logger.info(".......... 【0】[当前正在被关闭的session] session.hashCode=" + session.hashCode() + ", session.ip+port=" + session.remoteAddress());
            if (sessionInOnlinelist != null) {
                logger.info(".......... 【1】[处于在线列表中的session] session.hashCode=" + sessionInOnlinelist.hashCode() + ", session.ip+port=" + sessionInOnlinelist.remoteAddress());
            }

            if (sessionInOnlinelist != null && session != null && session == sessionInOnlinelist) {
                OnlineProcessor.getInstance().removeUser(user_id);
                if (this.serverEventListener != null) {
                    this.serverEventListener.onUserLogoutAction_CallBack(user_id, (Object)null, session);
                } else {
                    logger.debug("[IMCORE-netty]>> 会话" + ServerToolKits.clientInfoToString(session) + "被系统close了，但回调对象是null，没有进行回调通知.");
                }
            } else {
                logger.warn("[IMCORE-netty]【2】【注意】会话" + ServerToolKits.clientInfoToString(session) + "不在在线列表中，意味着它是被客户端弃用的，本次忽略这条关闭事件即可！");
            }
        } else {
            logger.warn("[IMCORE-netty]【注意】会话" + ServerToolKits.clientInfoToString(session) + "被系统close了，但它里面没有存放user_id，它很可能是没有成功合法认证而被提前关闭，从而正常释放资源。");
        }

    }

    public void sessionCreated(Channel session) throws Exception {
        logger.info("[IMCORE-netty]与" + ServerToolKits.clientInfoToString(session) + "的会话建立(channelActive)了...");
    }

    public ServerEventListener getServerEventListener() {
        return this.serverEventListener;
    }

    void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverEventListener = serverEventListener;
    }

    public MessageQoSEventListenerS2C getServerMessageQoSEventListener() {
        return this.serverMessageQoSEventListener;
    }

    void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener) {
        this.serverMessageQoSEventListener = serverMessageQoSEventListener;
    }

    public BridgeProcessor getBridgeProcessor() {
        return this.bridgeProcessor;
    }
}
