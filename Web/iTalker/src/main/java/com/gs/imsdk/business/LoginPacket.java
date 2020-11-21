package com.gs.imsdk.business;

import com.gs.imsdk.data.DeviceInfo;
import com.gs.imsdk.protocol.Protocol;
import com.gs.imsdk.protocol.ProtocolFactory;
import com.gs.imsdk.protocol.UDPHandler;

import io.netty.channel.Channel;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import net.openmob.mobileimsdk.server.processor.OnlineProcessor;
import net.qiujuer.web.italker.push.bean.db.Device;
import net.qiujuer.web.italker.push.factory.DeviceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 主要做下面三件事情。
 * （1）接受来自客户端的设备信息。设备信息包括linux版本，Android版本，设备型号，mac地址，经纬度等。
 * （2）创建一个PushID，连同设备信息一起写入到数据库中。
 * （3）客户端收到PushID后，表示设备已经连接上服务器了。在登陆app时，或者是打开app时，更新pushID。
 */
public class LoginPacket {
    private static LoginPacket loginPacket = null;
    private static Logger logger = LoggerFactory.getLogger(LoginPacket.class);

    private LoginPacket(){
    }

    public static LoginPacket getInstance(){
        if(loginPacket == null){
            loginPacket = new LoginPacket();
        }
        return  loginPacket;
    }

    //接受来自客户端的loginPacket
    public void receive(final Channel session, Protocol pFromClient, final String remoteAddress) throws Exception{
        final DeviceInfo deviceInfo = ProtocolFactory.parseDataContent(pFromClient.getDataContent(), DeviceInfo.class);
        if (deviceInfo != null && deviceInfo.getId() != null) {
            //创建一个PushID，
            String uuid = UUID.randomUUID().toString();
            //取最前面的3个字符
//            String uuid = uuida.substring(0,2);
            logger.info("[IMCORE]>> 客户端" + remoteAddress + "发过来的登陆信息内容是：deviceInfo=" + deviceInfo.getId() + ".pushId=" + uuid);

            // 设置回调，监听是否发送成功
            MBObserver sendResultObserver = new MBObserver() {
                public void update(boolean __sendOK, Object extraObj) {
                    if (__sendOK) {
                        //保存当前设备ID和session到内存中
                        session.attr(OnlineProcessor.USER_ID_IN_SESSION_ATTRIBUTE_ATTR).set(uuid);
                        OnlineProcessor.getInstance().putUser(uuid, session);

                        //将设备信息和PushID存入到数据库中
                        saveAndUpdateDB(deviceInfo, uuid);
                    } else {
                        logger.warn("[IMCORE]>> 发给客户端" + remoteAddress + "的登陆成功信息发送失败了！");
                    }
                }
            };

            //给客户端发送PushID，
            UDPHandler.getInstance().udpSendACK(session, ProtocolFactory.createLoginPacket(pFromClient.getId(), uuid), sendResultObserver);
        } else {
            logger.warn("[IMCORE]>> 收到客户端" + remoteAddress + "登陆信息，但loginInfo或loginInfo.getId()是null，登陆无法继续！");
        }
    }

    public void saveAndUpdateDB(DeviceInfo deviceInfo, String pushId){
        String id = deviceInfo.getId();
        Device device = DeviceFactory.findById(id);
        if(device == null){
            DeviceFactory.createDevice(deviceInfo, pushId);
        }else {
            device.setPushID(pushId);
            DeviceFactory.update(device);
        }
    }
}
