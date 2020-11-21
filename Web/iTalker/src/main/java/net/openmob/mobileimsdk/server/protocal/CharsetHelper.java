//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.protocal;

import java.io.UnsupportedEncodingException;

public class CharsetHelper {
    public static final String ENCODE_CHARSET = "UTF-8";
    public static final String DECODE_CHARSET = "UTF-8";

    public CharsetHelper() {
    }

    public static String getString(byte[] b, int len) {
        try {
            return new String(b, 0, len, "UTF-8");
        } catch (UnsupportedEncodingException var3) {
            return new String(b, 0, len);
        }
    }

    public static String getString(byte[] b, int start, int len) {
        try {
            return new String(b, start, len, "UTF-8");
        } catch (UnsupportedEncodingException var4) {
            return new String(b, start, len);
        }
    }

    public static byte[] getBytes(String str) {
        if (str != null) {
            try {
                return str.getBytes("UTF-8");
            } catch (UnsupportedEncodingException var2) {
                return str.getBytes();
            }
        } else {
            return new byte[0];
        }
    }
}
