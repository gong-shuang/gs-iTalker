package com.gs.imsdk.protocol;

import com.google.gson.Gson;
import net.openmob.mobileimsdk.server.protocal.CharsetHelper;

import java.util.UUID;

/**
 * 协议层
 * 负责UDP的发送和接受，主要功能有：
 * （1）消息消息重发，默认过10秒后，再重发一次，如果还是不饿能
 * （2）消息的加密和解密
 * （3）判断是否是重复的消息，
 */
public class Protocol {
    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_COMMON = 2;
    public static final int TYPE_LOGOUT = 3;
    public static final int TYPE_PUSH = 4;
    public static final int TYPE_KEEP_ALIVE = 5;
    public static final int TYPE_USER_UN_EXIST = 100;  //用户不存在
    public static final int TYPE_USER_UN_LOGIN = 101;  //用户未登录，就是说，该用户没有pushID

    private String id;  //每个消息都有唯一的ID，
    private int type; //消息类型，包括有登陆，普通，退出，推送
    private String dataContent;  //上层的，业务层的代码
    private boolean replyServer;  //如果是true，则服务器需要给客户端应答，
    private boolean replyClient;  //如果是true，则客户端需要给服务器应答，
    private transient int retryCount;

    public Protocol (String id, int type, String dataContent, boolean replyServer, boolean replyClient){
        this.id = id;
        this.type = type;

        this.dataContent = dataContent;
        this.replyServer = replyServer;
        this.replyClient = replyClient;
    }

    public String toGsonString() {
        return (new Gson()).toJson(this);
    }

    public byte[] toBytes() {
        return CharsetHelper.getBytes(this.toGsonString());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDataContent() {
        return dataContent;
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }

    public boolean isReplyServer() {
        return replyServer;
    }

    public void setReplyServer(boolean replyServer) {
        this.replyServer = replyServer;
    }

    public boolean isReplyClient() {
        return replyClient;
    }

    public void setReplyClient(boolean replyClient) {
        this.replyClient = replyClient;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
