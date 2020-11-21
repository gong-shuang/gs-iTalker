package com.gs.imsdk.data;

import java.util.UUID;

public class CommonInfo {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_PICTURE = 2;
    public static final int TYPE_FILE = 3;

    public static final int STATE_SENDING = 1;
    public static final int STATE_SENT = 2;   // 发送方已发送成功
    public static final int STATE_RECEIVING = 3;   //告知接受方，有新的消息
    public static final int STATE_RECEIVED = 4;  // 接受方已经接受消息
    public static final int STATE_SENT_RECEIVED = 5;  // 告知发送方，接收方已经接受
    public static final int STATE_DONE = 6; //消息完成，即发送方已经知道这个消息被接受了。


    private String id;  // 唯一标识
    private int type;
    private String content;
    private String attach;  //当消息是文件，视频，语言时，这个字段表示文件的时长。
    private String groupId;
    private String receiverId;
    private String senderId;
    private int state;
    private String createAt;
    private String updateAt;

    public CommonInfo(int type, String content, String attach, String groupId, String receiverId, String senderId, int state) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
        this.attach = attach;
        this.groupId = groupId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.state = state;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}

