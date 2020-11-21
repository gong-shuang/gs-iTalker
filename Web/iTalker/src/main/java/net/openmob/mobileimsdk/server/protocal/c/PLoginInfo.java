//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.protocal.c;

//用户的登录信息
public class PLoginInfo {
    private String loginUserId;
    private String loginToken;
    private String extra;

    public PLoginInfo(String loginUserId, String loginToken) {
        this(loginUserId, loginToken, (String)null);
    }

    public PLoginInfo(String loginUserId, String loginToken, String extra) {
        this.loginUserId = null;
        this.loginToken = null;
        this.extra = null;
        this.loginUserId = loginUserId;
        this.loginToken = loginToken;
        this.extra = extra;
    }

    public String getLoginUserId() {
        return this.loginUserId;
    }

    public void setLoginUserId(String loginUserId) {
        this.loginUserId = loginUserId;
    }

    public String getLoginToken() {
        return this.loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
