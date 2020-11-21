//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.protocal;

import com.google.gson.Gson;
import net.openmob.mobileimsdk.server.protocal.c.PKeepAlive;
import net.openmob.mobileimsdk.server.protocal.c.PLoginInfo;
import net.openmob.mobileimsdk.server.protocal.s.PErrorResponse;
import net.openmob.mobileimsdk.server.protocal.s.PKeepAliveResponse;
import net.openmob.mobileimsdk.server.protocal.s.PLoginInfoResponse;

public class ProtocalFactory {
    public ProtocalFactory() {
    }

    private static String create(Object c) {
        return (new Gson()).toJson(c);
    }

    public static <T> T parse(byte[] fullProtocalJASOnBytes, int len, Class<T> clazz) {
        return parse(CharsetHelper.getString(fullProtocalJASOnBytes, len), clazz);
    }

    public static <T> T parse(String dataContentOfProtocal, Class<T> clazz) {
        return (new Gson()).fromJson(dataContentOfProtocal, clazz);
    }

    public static Protocal parse(byte[] fullProtocalJASOnBytes, int len) {
        return (Protocal)parse(fullProtocalJASOnBytes, len, Protocal.class);
    }

    public static Protocal createPKeepAliveResponse(String to_user_id) {
        return new Protocal(51, create(new PKeepAliveResponse()), "0", to_user_id);
    }

    public static PKeepAliveResponse parsePKeepAliveResponse(String dataContentOfProtocal) {
        return (PKeepAliveResponse)parse(dataContentOfProtocal, PKeepAliveResponse.class);
    }

    public static Protocal createPKeepAlive(String from_user_id) {
        return new Protocal(1, create(new PKeepAlive()), from_user_id, "0");
    }

    public static PKeepAlive parsePKeepAlive(String dataContentOfProtocal) {
        return (PKeepAlive)parse(dataContentOfProtocal, PKeepAlive.class);
    }

    public static Protocal createPErrorResponse(int errorCode, String errorMsg, String user_id) {
        return new Protocal(52, create(new PErrorResponse(errorCode, errorMsg)), "0", user_id);
    }

    public static PErrorResponse parsePErrorResponse(String dataContentOfProtocal) {
        return (PErrorResponse)parse(dataContentOfProtocal, PErrorResponse.class);
    }

    public static Protocal createPLoginoutInfo(String user_id) {
        return new Protocal(3, (String)null, user_id, "0");
    }

    public static Protocal createPLoginInfo(String userId, String token, String extra) {
        return new Protocal(0, create(new PLoginInfo(userId, token, extra)), userId, "0");
    }

    public static PLoginInfo parsePLoginInfo(String dataContentOfProtocal) {
        return (PLoginInfo)parse(dataContentOfProtocal, PLoginInfo.class);
    }

    public static Protocal createPLoginInfoResponse(int code, String user_id) {
        return new Protocal(50, create(new PLoginInfoResponse(code)), "0", user_id, true, Protocal.genFingerPrint());
    }

    public static PLoginInfoResponse parsePLoginInfoResponse(String dataContentOfProtocal) {
        return (PLoginInfoResponse)parse(dataContentOfProtocal, PLoginInfoResponse.class);
    }

    public static Protocal createCommonData(String dataContent, String from_user_id, String to_user_id, boolean QoS, String fingerPrint) {
        return createCommonData(dataContent, from_user_id, to_user_id, QoS, fingerPrint, -1);
    }

    public static Protocal createCommonData(String dataContent, String from_user_id, String to_user_id, boolean QoS, String fingerPrint, int typeu) {
        return new Protocal(2, dataContent, from_user_id, to_user_id, QoS, fingerPrint, typeu);
    }

    public static Protocal createRecivedBack(String from_user_id, String to_user_id, String recievedMessageFingerPrint) {
        return createRecivedBack(from_user_id, to_user_id, recievedMessageFingerPrint, false);
    }

    public static Protocal createRecivedBack(String from_user_id, String to_user_id, String recievedMessageFingerPrint, boolean bridge) {
        Protocal p = new Protocal(4, recievedMessageFingerPrint, from_user_id, to_user_id);
        p.setBridge(bridge);
        return p;
    }
}
