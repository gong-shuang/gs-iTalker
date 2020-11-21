package net.openmob.mobileimsdk.server.event;


import io.netty.channel.Channel;
import net.openmob.mobileimsdk.server.protocal.Protocal;

public interface ServerEventListener {
    int onVerifyUserCallBack(String var1, String var2, String var3, Channel var4);

    void onUserLoginAction_CallBack(String var1, String var2, Channel var3);

    void onUserLogoutAction_CallBack(String var1, Object var2, Channel var3);

    boolean onTransBuffer_C2S_CallBack(Protocal var1, Channel var2);

    void onTransBuffer_C2C_CallBack(Protocal var1);

    boolean onTransBuffer_C2C_RealTimeSendFaild_CallBack(Protocal var1);
}