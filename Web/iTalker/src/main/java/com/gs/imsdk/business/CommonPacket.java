package com.gs.imsdk.business;

import com.gs.imsdk.data.CommonInfo;
import com.gs.imsdk.data.DeviceInfo;
import com.gs.imsdk.protocol.Protocol;
import com.gs.imsdk.protocol.ProtocolFactory;
import com.gs.imsdk.protocol.UDPHandler;
import io.netty.channel.Channel;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import net.openmob.mobileimsdk.server.processor.OnlineProcessor;
import net.qiujuer.web.italker.push.bean.api.base.PushModel;
import net.qiujuer.web.italker.push.bean.api.message.MessageCreateModel;
import net.qiujuer.web.italker.push.bean.db.*;
import net.qiujuer.web.italker.push.factory.DeviceFactory;
import net.qiujuer.web.italker.push.factory.MessageFactory;
import net.qiujuer.web.italker.push.factory.UdpMessageFactory;
import net.qiujuer.web.italker.push.factory.UserFactory;
import net.qiujuer.web.italker.push.utils.Hib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 *  普通消息的包，
 *  包括：文字信息，文件，图片，语言消息，视频等。
 *  假如A发送给B消息，则流程是：
 *  （1）A发送给服务器，服务器应答消息
 *  （2）服务器主动发送给B，B应答消息，
 *  （3）服务器主动发送给A，告诉A消息已经被接受了，
 *  （4）A收到服务器发送的消息已经被接受了，需应答服务器，服务器收到后，将消息的状态设置完成。
 */
public class CommonPacket {
    private static CommonPacket keepAlive = null;
    private static Logger logger = LoggerFactory.getLogger(LoginPacket.class);
    //先存在内存中，后期移到数据库中
    public ConcurrentHashMap<String, CommonInfo> receiveMessage = new ConcurrentHashMap();

    private CommonPacket(){
    }

    public static CommonPacket getInstance(){
        if(keepAlive == null){
            keepAlive = new CommonPacket();
        }
        return keepAlive;
    }

    /**
     * 主动发送的包，对于刚上线的用户，需要主动发送（其他用户发送给这个用户的消息）
     * 这里的上线就是输入了用户名和密码后，进入到APP的主界面。
     */
    public void send(){

    }

    public void notifySender(CommonInfo commonInfo){
        String sender = commonInfo.getSenderId();
        User user = UserFactory.findById(sender);
        if(user == null ){
            //用户未登录
            logger.warn("[IMCORE]>> 发送者不存在！，sender：" + sender );
            return;
        }
        String senderPushID = user.getPushId();
        if(senderPushID == null || senderPushID.length()==0){
            //接收者未登录（未绑定pushid）
            logger.warn("[IMCORE]>> 发送者未绑定PushId！，senderPushID：" + senderPushID );
            return;
        }

        //判断是否在线
        if(!OnlineProcessor.isOnline(senderPushID)){
            logger.warn("[IMCORE]>> 发送者不在线！，内容：" + commonInfo.getContent() );
            return;
        }

        // 设置回调，监听是否发送成功
        MBObserver sendResultObserver = new MBObserver() {
            public void update(boolean __sendOK, Object extraObj) {
                if (__sendOK) {
                    //接受成功，接受者 接受到信息，更新数据库
                    commonInfo.setState(CommonInfo.STATE_DONE);

                    logger.warn("[IMCORE]>> 发给客户端" + sender + "的common信息发送成功了！");

                } else {
                    logger.warn("[IMCORE]>> 发给客户端" + sender + "的common信息发送失败了！");
                }
            }
        };

        //发送
        UDPHandler.getInstance().udpSend(OnlineProcessor.getInstance().getOnlineSession(senderPushID),
                ProtocolFactory.createCommonPacketReply(commonInfo, true), null);
    }

    public void notifyReceiver(CommonInfo commonInfo){
        String receiver = commonInfo.getReceiverId();
        User user = UserFactory.findById(receiver);
        if(user == null ){
            //用户未登录
            logger.warn("[IMCORE]>> 接收者不存在！，receiver：" + receiver );
            return;
        }
        String receiverPushID = user.getPushId();
        if(receiverPushID == null || receiverPushID.length()==0){
            //接收者未登录（未绑定pushid）
            logger.warn("[IMCORE]>> 接受者未绑定PushId！，receiverPushID：" + receiverPushID );
            return;
        }

        //判断是否在线
        if(!OnlineProcessor.isOnline(receiverPushID)){
            logger.warn("[IMCORE]>> 接受者不在线！，内容：" + commonInfo.getContent() );
            return;
        }

        // 设置回调，监听是否发送成功
        MBObserver sendResultObserver = new MBObserver() {
            public void update(boolean __sendOK, Object extraObj) {
                if (__sendOK) {
                    //接受成功，接受者 接受到信息，更新数据库
                    commonInfo.setState(CommonInfo.STATE_RECEIVED);

                    logger.warn("[IMCORE]>> 发给客户端" + receiver + "的common信息发送成功了！");

                } else {
                    logger.warn("[IMCORE]>> 发给客户端" + receiver + "的common信息发送失败了！");
                }
            }
        };

        UDPHandler.getInstance().udpSend(OnlineProcessor.getInstance().getOnlineSession(receiverPushID),
                ProtocolFactory.createCommonPacketReply(commonInfo, true), sendResultObserver);
    }

    //接受来自客户端的包
    public void receive(final Channel session, Protocol pFromClient, final String remoteAddress){
        final CommonInfo commonInfo = ProtocolFactory.parseDataContent(pFromClient.getDataContent(), CommonInfo.class);
        if(commonInfo.getState()==CommonInfo.STATE_SENDING){
            //表示：说明是新接受的包
            if(queryMessage(commonInfo.getId())){
                logger.warn("[IMCORE]>> 消息不应该存在！state：" + commonInfo.getState() + "，内容：" + commonInfo.getContent() );
                return;
            }

            //判断发送方的用户ID是否存在
            String sender = commonInfo.getSenderId();
            User user = UserFactory.findById(sender);
            if(user == null ){
                //用户未登录
                UDPHandler.getInstance().udpSendACK(session, ProtocolFactory.createUserUnExistPacket(commonInfo.getId(), sender), null);
                return;
            }
            String senderPushID = user.getPushId();
            if(senderPushID == null || senderPushID.length()==0){
                //发送方未登录（未绑定pushid）
                UDPHandler.getInstance().udpSendACK(session, ProtocolFactory.createUserUnLoginPacket(commonInfo.getId(), sender), null);
                return;
            }

            //判断接受方的用户ID是否存在
            String receiver = commonInfo.getReceiverId();
            User userRece = UserFactory.findById(receiver);
            if(userRece == null ){
                //用户未登录
                UDPHandler.getInstance().udpSendACK(session, ProtocolFactory.createUserUnExistPacket(commonInfo.getId(), receiver), null);
                return;
            }

            //告知发送方，服务器已经接受，属于确认包
            commonInfo.setState(CommonInfo.STATE_SENT);
            UDPHandler.getInstance().udpSendACK(session, ProtocolFactory.createCommonPacket(commonInfo, true), null);

            //更新状态
            commonInfo.setState(CommonInfo.STATE_RECEIVING);

            //存入到数据库中
            saveMessage(commonInfo);

            //通知接受方，有新的消息已经到来了
            notifyReceiver(commonInfo);
        }else if(commonInfo.getState()==CommonInfo.STATE_RECEIVED){
            //表示：接受方已经接受到了消息
            if(!queryMessage(commonInfo.getId())){
                logger.warn("[IMCORE]>> 消息不存在！state：" + commonInfo.getState() + "，内容：" + commonInfo.getContent() );
                return;
            }

            //更新数据库
            updateMessageState(commonInfo.getId(), CommonInfo.STATE_SENT_RECEIVED);
            commonInfo.setState(CommonInfo.STATE_SENT_RECEIVED);

            //通知发送方，接受方已经接受到了消息
            notifySender(commonInfo);
        }else if(commonInfo.getState()==CommonInfo.STATE_DONE){
            //表示：发送方已经知道消息被接受了
            if(!queryMessage(commonInfo.getId())){
                logger.warn("[IMCORE]>> 消息不存在！state：" + commonInfo.getState() + "，内容：" + commonInfo.getContent() );
                return;
            }

            //更新数据库
            updateMessageState(commonInfo.getId(), CommonInfo.STATE_DONE);

        }else{
            logger.warn("[IMCORE]>> 接受的消息错误！state：" + commonInfo.getState() + "，内容：" + commonInfo.getContent() );
        }
    }

    boolean queryMessage(String id){
        return receiveMessage.containsKey(id);
    }

    //保持到内存中。。
    void  saveMessage(CommonInfo commonInfo ){
        receiveMessage.put(commonInfo.getId(),commonInfo);

        saveAndUpdateDB(commonInfo);
    }

    //保持到内存中
    void  updateMessageState(String id, int state){
        CommonInfo commonInfo = receiveMessage.get(id);
        if(commonInfo != null){
            commonInfo.setState(state);

            saveAndUpdateDB(commonInfo);
        }
    }

    //保持到数据
    public void saveAndUpdateDB(CommonInfo commonInfo ){

        String id = commonInfo.getId();
        UdpMessage message = UdpMessageFactory.findById(id);
        if(message == null){
            UdpMessageFactory.createDevice(commonInfo);
        }else {
            UdpMessageFactory.update(new UdpMessage(commonInfo));
        }
    }
}
