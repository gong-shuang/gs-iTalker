package com.gs.imsdk.protocol;

import com.google.gson.Gson;
import com.gs.imsdk.data.CommonInfo;
import com.gs.imsdk.data.DeviceInfo;
import net.openmob.mobileimsdk.server.protocal.CharsetHelper;


public class ProtocolFactory {
    private static String create(Object c) {
        return (new Gson()).toJson(c);
    }

    public static <T> T parse(byte[] fullProtocalJASOnBytes, int len, Class<T> clazz) {
        return parse(CharsetHelper.getString(fullProtocalJASOnBytes, len), clazz);
    }

    public static <T> T parse(String dataContentOfProtocal, Class<T> clazz) {
        return (new Gson()).fromJson(dataContentOfProtocal, clazz);
    }

    public static <T> T parseDataContent(String dataContentOfProtocal, Class<T> clazz) {
        return (T)parse(dataContentOfProtocal, clazz);
    }


    public static DeviceInfo parseLoginInfo(String dataContentOfProtocal) {
        return (DeviceInfo)parse(dataContentOfProtocal, DeviceInfo.class);
    }

    //确认登录包
    public static Protocol createLoginPacket(String id, String pushID) {
        return new Protocol(id, Protocol.TYPE_LOGIN, pushID, true, false);
    }

    //确认心跳包
    public static Protocol createKeepAlivePacket(String id, String sequence) {
        return new Protocol(id, Protocol.TYPE_KEEP_ALIVE, sequence, true, false);
    }

    //普通消息--用户不需要应答
    public static Protocol createCommonPacket(CommonInfo commonInfo, boolean reply) {
        return new Protocol(commonInfo.getId(), Protocol.TYPE_COMMON, create(commonInfo),  reply, false);
    }

    //普通消息--用户需要应答
    public static Protocol createCommonPacketReply(CommonInfo commonInfo, boolean reply) {
        return new Protocol(commonInfo.getId(), Protocol.TYPE_COMMON, create(commonInfo), false, reply);
    }

    //用户不存在
    public static Protocol createUserUnExistPacket(String id, String userId) {
        return new Protocol(id, Protocol.TYPE_USER_UN_EXIST, userId, true, false);
    }

    //用户没有登陆
    public static Protocol createUserUnLoginPacket(String id, String userId) {
        return new Protocol(id, Protocol.TYPE_USER_UN_LOGIN, userId, true, false);
    }

}
