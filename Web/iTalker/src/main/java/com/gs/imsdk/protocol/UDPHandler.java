package com.gs.imsdk.protocol;

import com.gs.imsdk.business.CommonPacket;
import com.gs.imsdk.business.KeepAlive;
import com.gs.imsdk.business.LoginPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import net.openmob.mobileimsdk.server.utils.ServerToolKits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

public class UDPHandler {
    private static UDPHandler udpHandler = null;
    private static Logger logger = LoggerFactory.getLogger(UDPHandler.class);

    public ConcurrentHashMap<String, Protocol> sentUdp = new ConcurrentHashMap();  //已经发送的udp，用来确定是否需要重发
    private SendThread sendThread;

    private UDPHandler(){
        init();
    }

    private void init(){
        sendThread = new SendThread("UDP-Sender");
        sendThread.start();
    }

    public static UDPHandler getInstance(){
        if(udpHandler == null){
            udpHandler = new UDPHandler();
        }
        return  udpHandler;
    }

    public void udpReceive(Channel session, ByteBuf bytebuf) throws Exception {
        Protocol pFromClient = ServerToolKits.fromIOBufferNew(bytebuf);

        //删除需要重复发送的包
        if(sentUdp.containsKey(pFromClient.getId())) {
            //第一次收到，移除
            sentUdp.remove(pFromClient.getId());
        }

        String remoteAddress = ServerToolKits.clientInfoToString(session);
        switch(pFromClient.getType()) {
            case Protocol.TYPE_LOGIN:
                LoginPacket.getInstance().receive(session, pFromClient, remoteAddress);
                break;
            case Protocol.TYPE_COMMON:
                //如果没有登录，返回
                CommonPacket.getInstance().receive(session, pFromClient, remoteAddress);
                break;
            case Protocol.TYPE_KEEP_ALIVE:
                //如果没有登录，返回
                KeepAlive.getInstance().receive(session, pFromClient, remoteAddress);
                break;
            default:
                logger.warn("[IMCORE-netty]【注意】收到的客户端" + remoteAddress + "消息类型：" + pFromClient.getType() + "，但目前该类型服务端不支持解析和处理！");
        }
    }

    //发送确认包，对应发送确认包，不需要应答
    public void udpSendACK(final Channel session, final Protocol p, final MBObserver resultObserver){
        if (session == null) {
            logger.info("[IMCORE-netty]toSession==null >> Protocol.id=" + p.getId() + "的消息：str=" + p.getDataContent() + "因接收方的id已不在线，此次实时发送没有继续(此消息应考虑作离线处理哦).");
        } else if (session.isActive()) {
            if (p != null) {
                final byte[] res = p.toBytes();
                ByteBuf to = Unpooled.copiedBuffer(res);
                ChannelFuture cf = session.writeAndFlush(to);
                cf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            //发送成功
                        } else {
                            logger.warn("[IMCORE-netty]给客户端：" + ServerToolKits.clientInfoToString(session) + "的数据->" + p.toGsonString() + ",发送失败！[" + res.length + "](此消息应考虑作离线处理哦).");
                        }

                        //发送成功
                        if (resultObserver != null) {
                            resultObserver.update(future.isSuccess(), (Object)null);
                        }

                    }
                });
                return;
            }

            logger.warn("[IMCORE-netty] Protocol.id=" + p.getId() + "的消息：str=" + p.getDataContent() + "没有继续(此消息应考虑作离线处理哦).");
        }

        //发送失败
        if (resultObserver != null) {
            resultObserver.update(false, (Object)null);
        }
    }

    //主动给客户端发送，需要客户端应答
    public void udpSend(final Channel session, final Protocol p, final MBObserver resultObserver){
        //需要应答，如果没有收到应答，需要继续发送；

        //1、先丢到队列中；
        if(p.isReplyClient()){
            //先判断是否存在
            if(sentUdp.containsKey(p.getId())) {
                //第一次收到，移除
                sentUdp.put(p.getId(),p);
            }
        }else{
            //重复收到，返回
            logger.warn("[IMCORE-netty]发送的包不需要应答");
            return;
        }

        //2、发送
        if (session == null) {
            logger.info("[IMCORE-netty]toSession==null >> Protocol.id=" + p.getId() + "的消息：str=" + p.getDataContent() + "因接收方的id已不在线，此次实时发送没有继续(此消息应考虑作离线处理哦).");
        } else if (session.isActive()) {
            if (p != null) {
                final byte[] res = p.toBytes();
                ByteBuf to = Unpooled.copiedBuffer(res);
                ChannelFuture cf = session.writeAndFlush(to);
                cf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            //发送成功
                        } else {
                            logger.warn("[IMCORE-netty]给客户端：" + ServerToolKits.clientInfoToString(session) + "的数据->" + p.toGsonString() + ",发送失败！[" + res.length + "](此消息应考虑作离线处理哦).");
                        }

                        //发送成功
                        if (resultObserver != null) {
                            resultObserver.update(future.isSuccess(), (Object)null);
                        }

                    }
                });
                return;
            }

            logger.warn("[IMCORE-netty] Protocol.id=" + p.getId() + "的消息：str=" + p.getDataContent() + "没有继续(此消息应考虑作离线处理哦).");
        }

        //发送失败
        if (resultObserver != null) {
            resultObserver.update(false, (Object)null);
        }
    }


    class SendThread extends Thread{

        public SendThread(String title) {
            super(title);
        }

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(Protocol p : sentUdp.values()){
                    logger.warn("[IMCORE-netty] Protocol.id=" + p.getId() + "的消息：str=" + p.getDataContent() + "xxxxxxxxxxxxxxxxx.");
                }
            }
        }
    }

}
