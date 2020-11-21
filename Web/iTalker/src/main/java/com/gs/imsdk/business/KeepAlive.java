package com.gs.imsdk.business;

import com.gs.imsdk.protocol.Protocol;
import com.gs.imsdk.protocol.ProtocolFactory;
import com.gs.imsdk.protocol.UDPHandler;
import io.netty.channel.Channel;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepAlive {
    private static KeepAlive keepAlive = null;
    private static Logger logger = LoggerFactory.getLogger(LoginPacket.class);

    private KeepAlive(){
    }

    public static KeepAlive getInstance(){
        if(keepAlive == null){
            keepAlive = new KeepAlive();
        }
        return keepAlive;
    }

    //接受来自客户端的包
    public void receive(final Channel session, Protocol pFromClient, final String remoteAddress){
        final String sequence = pFromClient.getDataContent();
        logger.info("[IMCORE]>> 客户端" + remoteAddress + "发过来的keepAlive信息内容是：sequence=" + sequence );

        // 设置回调，监听是否发送成功
        MBObserver sendResultObserver = new MBObserver() {
            public void update(boolean __sendOK, Object extraObj) {
                if (__sendOK) {
                    //
                    logger.warn("[IMCORE]>> 发给客户端" + remoteAddress + "的keepAlive信息发送成功！");

                } else {
                    logger.warn("[IMCORE]>> 发给客户端" + remoteAddress + "的keepAlive信息发送失败了！");
                }
            }
        };

        //给客户端发送
        UDPHandler.getInstance().udpSendACK(session, ProtocolFactory.createKeepAlivePacket(pFromClient.getId(), sequence), sendResultObserver);
    }
}
