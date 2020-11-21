//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.utils;

import com.gs.imsdk.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.net.SocketAddress;
import net.openmob.mobileimsdk.server.ServerCoreHandler;
import net.openmob.mobileimsdk.server.ServerLauncher;
import net.openmob.mobileimsdk.server.processor.OnlineProcessor;
import net.openmob.mobileimsdk.server.protocal.Protocal;
import net.openmob.mobileimsdk.server.protocal.ProtocalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerToolKits {
    private static Logger logger = LoggerFactory.getLogger(ServerCoreHandler.class);

    public ServerToolKits() {
    }

    public static void setSenseMode(ServerToolKits.SenseMode mode) {
        int expire = 0;
        switch(mode.ordinal()) {
            case 0:
                expire = 10;
                break;
            case 1:
                expire = 21;
                break;
            case 2:
                expire = 62;
                break;
            case 3:
                expire = 122;
                break;
            case 4:
                expire = 242;
        }

        if (expire > 0) {
            ServerLauncher.SESION_RECYCLER_EXPIRE = expire;
        }

    }

    public static String clientInfoToString(Channel session) {
        SocketAddress remoteAddress = session.remoteAddress();
        String s1 = remoteAddress.toString();
        StringBuilder sb = (new StringBuilder()).append("{uid:").append(OnlineProcessor.getUserIdFromSession(session)).append("}").append(s1);
        return sb.toString();
    }

    public static String fromIOBuffer_JSON(ByteBuf buffer) throws Exception {
        byte[] req = new byte[buffer.readableBytes()];
        buffer.readBytes(req);
        String jsonStr = new String(req, "UTF-8");
        return jsonStr;
    }

    public static Protocal fromIOBuffer(ByteBuf buffer) throws Exception {
        return (Protocal)ProtocalFactory.parse(fromIOBuffer_JSON(buffer), Protocal.class);
    }

    //gs add
    public static Protocol fromIOBufferNew(ByteBuf buffer) throws Exception {
        return (Protocol)ProtocalFactory.parse(fromIOBuffer_JSON(buffer), Protocol.class);
    }

    public static enum SenseMode {
        MODE_3S,
        MODE_10S,
        MODE_30S,
        MODE_60S,
        MODE_120S;

        private SenseMode() {
        }
    }
}
