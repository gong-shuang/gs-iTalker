//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.processor;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineProcessor {
    public static final String USER_ID_IN_SESSION_ATTRIBUTE = "__user_id__";
    public static final AttributeKey<String> USER_ID_IN_SESSION_ATTRIBUTE_ATTR = AttributeKey.newInstance("__user_id__");
    public static boolean DEBUG = false;
    private static Logger logger = LoggerFactory.getLogger(OnlineProcessor.class);
    private static OnlineProcessor instance = null;
    private ConcurrentMap<String, Channel> onlineSessions = new ConcurrentHashMap();   //存放userid 和 session 的hashmap。

    public static OnlineProcessor getInstance() {
        if (instance == null) {
            instance = new OnlineProcessor();
        }

        return instance;
    }

    private OnlineProcessor() {
    }

    public void putUser(String user_id, Channel session) {
        if (this.onlineSessions.containsKey(user_id)) {
            logger.debug("[IMCORE-netty]【注意】用户id=" + user_id + "已经在在线列表中了，session也是同一个吗？" + (((Channel)this.onlineSessions.get(user_id)).hashCode() == session.hashCode()));
        }

        this.onlineSessions.put(user_id, session);
        this.__printOnline();
    }

    public void __printOnline() {
        logger.debug("【@】当前在线用户共(" + this.onlineSessions.size() + ")人------------------->");
        if (DEBUG) {
            Iterator var2 = this.onlineSessions.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                logger.debug("      > user_id=" + key + ",session=" + ((Channel)this.onlineSessions.get(key)).remoteAddress());
            }
        }

    }

    public boolean removeUser(String user_id) {
        synchronized(this.onlineSessions) {
            if (!this.onlineSessions.containsKey(user_id)) {
                logger.warn("[IMCORE-netty]！用户id=" + user_id + "不存在在线列表中，本次removeUser没有继续.");
                this.__printOnline();
                return false;
            } else {
                return this.onlineSessions.remove(user_id) != null;
            }
        }
    }

    public Channel getOnlineSession(String user_id) {
        if (user_id == null) {
            logger.warn("[IMCORE-netty][CAUTION] getOnlineSession时，作为key的user_id== null.");
            return null;
        } else {
            return (Channel)this.onlineSessions.get(user_id);
        }
    }

    public ConcurrentMap<String, Channel> getOnlineSessions() {
        return this.onlineSessions;
    }

    public static boolean isLogined(Channel session) {
        return session != null && getUserIdFromSession(session) != null;
    }

    public static String getUserIdFromSession(Channel session) {
        Object attr = null;
        if (session != null) {
            attr = session.attr(USER_ID_IN_SESSION_ATTRIBUTE_ATTR).get();
            if (attr != null) {
                return (String)attr;
            }
        }

        return null;
    }

    public static boolean isOnline(String userId) {
        return getInstance().getOnlineSession(userId) != null;
    }
}
