//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.qos;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.openmob.mobileimsdk.server.protocal.Protocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QoS4ReciveDaemonRoot {
    private static Logger logger = LoggerFactory.getLogger(QoS4ReciveDaemonRoot.class);
    private boolean DEBUG = false;
    private int CHECH_INTERVAL = 300000;
    private int MESSAGES_VALID_TIME = 600000;
    private ConcurrentMap<String, Long> recievedMessages = new ConcurrentHashMap();
    private Timer timer = null;
    private Runnable runnable = null;
    private boolean _excuting = false;
    private String debugTag = "";

    public QoS4ReciveDaemonRoot(int CHECH_INTERVAL, int MESSAGES_VALID_TIME, boolean DEBUG, String debugTag) {
        if (CHECH_INTERVAL > 0) {
            this.CHECH_INTERVAL = CHECH_INTERVAL;
        }

        if (MESSAGES_VALID_TIME > 0) {
            this.MESSAGES_VALID_TIME = MESSAGES_VALID_TIME;
        }

        this.DEBUG = DEBUG;
        this.debugTag = debugTag;
    }

    private void doTaskOnece() {
        if (!this._excuting) {
            this._excuting = true;
            if (this.DEBUG) {
                logger.debug("【IMCORE" + this.debugTag + "】【QoS接收方】++++++++++ START 暂存处理线程正在运行中，当前长度" + this.recievedMessages.size() + ".");
            }

            Iterator entryIt = this.recievedMessages.entrySet().iterator();

            while(entryIt.hasNext()) {
                Entry<String, Long> entry = (Entry)entryIt.next();
                String key = (String)entry.getKey();
                long value = (Long)entry.getValue();
                long delta = System.currentTimeMillis() - value;
                if (delta >= (long)this.MESSAGES_VALID_TIME) {
                    if (this.DEBUG) {
                        logger.debug("【IMCORE" + this.debugTag + "】【QoS接收方】指纹为" + key + "的包已生存" + delta + "ms(最大允许" + this.MESSAGES_VALID_TIME + "ms), 马上将删除之.");
                    }

                    this.recievedMessages.remove(key);
                }
            }
        }

        if (this.DEBUG) {
            logger.debug("【IMCORE" + this.debugTag + "】【QoS接收方】++++++++++ END 暂存处理线程正在运行中，当前长度" + this.recievedMessages.size() + ".");
        }

        this._excuting = false;
    }

    public void startup() {
        this.stop();
        if (this.recievedMessages != null && this.recievedMessages.size() > 0) {
            Iterator var2 = this.recievedMessages.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                this.putImpl(key);
            }
        }

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                QoS4ReciveDaemonRoot.this.doTaskOnece();
            }
        }, (long)this.CHECH_INTERVAL, (long)this.CHECH_INTERVAL);
    }

    public void stop() {
        if (this.timer != null) {
            try {
                this.timer.cancel();
            } finally {
                this.timer = null;
            }
        }

    }

    public boolean isRunning() {
        return this.timer != null;
    }

    public void addRecieved(Protocal p) {
        if (p != null && p.isQoS()) {
            this.addRecieved(p.getFp());
        }

    }

    public void addRecieved(String fingerPrintOfProtocal) {
        if (fingerPrintOfProtocal == null) {
            logger.debug("【IMCORE" + this.debugTag + "】无效的 fingerPrintOfProtocal==null!");
        } else {
            if (this.recievedMessages.containsKey(fingerPrintOfProtocal)) {
                logger.debug("【IMCORE" + this.debugTag + "】【QoS接收方】指纹为" + fingerPrintOfProtocal + "的消息已经存在于接收列表中，该消息重复了（原理可能是对方因未收到应答包而错误重传导致），更新收到时间戳哦.");
            }

            this.putImpl(fingerPrintOfProtocal);
        }
    }

    private void putImpl(String fingerPrintOfProtocal) {
        if (fingerPrintOfProtocal != null) {
            this.recievedMessages.put(fingerPrintOfProtocal, System.currentTimeMillis());
        }

    }

    public boolean hasRecieved(String fingerPrintOfProtocal) {
        return this.recievedMessages.containsKey(fingerPrintOfProtocal);
    }

    public int size() {
        return this.recievedMessages.size();
    }

    public QoS4ReciveDaemonRoot setDebugable(boolean debugable) {
        this.DEBUG = debugable;
        return this;
    }

    public boolean isDebugable() {
        return this.DEBUG;
    }
}
