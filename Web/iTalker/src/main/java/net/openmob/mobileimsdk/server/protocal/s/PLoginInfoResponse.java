package net.openmob.mobileimsdk.server.protocal.s;


public class PLoginInfoResponse {
    private int code = 0;

    public PLoginInfoResponse(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}