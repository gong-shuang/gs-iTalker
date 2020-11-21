//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.qos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import net.openmob.mobileimsdk.server.ServerLauncher;
import net.openmob.mobileimsdk.server.protocal.Protocal;
import net.openmob.mobileimsdk.server.utils.LocalSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QoS4SendDaemonRoot {
    private static Logger logger = LoggerFactory.getLogger(QoS4SendDaemonRoot.class);
    private boolean DEBUG = false;
    private ServerLauncher serverLauncher = null;
    private ConcurrentMap<String, Protocal> sentMessages = new ConcurrentHashMap();
    private ConcurrentMap<String, Long> sendMessagesTimestamp = new ConcurrentHashMap();
    private int CHECH_INTERVAL = 5000;
    private int MESSAGES_JUST$NOW_TIME = 2000;
    private int QOS_TRY_COUNT = 1;
    private boolean _excuting = false;
    private Timer timer = null;
    private String debugTag = "";

    public QoS4SendDaemonRoot(int CHECH_INTERVAL, int MESSAGES_JUST$NOW_TIME, int QOS_TRY_COUNT, boolean DEBUG, String debugTag) {
        if (CHECH_INTERVAL > 0) {
            this.CHECH_INTERVAL = CHECH_INTERVAL;
        }

        if (MESSAGES_JUST$NOW_TIME > 0) {
            this.MESSAGES_JUST$NOW_TIME = MESSAGES_JUST$NOW_TIME;
        }

        if (QOS_TRY_COUNT >= 0) {
            this.QOS_TRY_COUNT = QOS_TRY_COUNT;
        }

        this.DEBUG = DEBUG;
        this.debugTag = debugTag;
    }

    private void doTaskOnece() {
        if (!this._excuting) {
            ArrayList<Protocal> lostMessages = new ArrayList();
            this._excuting = true;

            try {
                if (this.DEBUG && this.sentMessages.size() > 0) {
                    logger.debug("【IMCORE-netty" + this.debugTag + "】【QoS发送方】=========== 消息发送质量保证线程运行中, 当前需要处理的列表长度为" + this.sentMessages.size() + "...");
                }

                Iterator entryIt = this.sentMessages.entrySet().iterator();

                label52:
                while(true) {
                    while(true) {
                        if (!entryIt.hasNext()) {
                            break label52;
                        }

                        Entry<String, Protocal> entry = (Entry)entryIt.next();
                        String key = (String)entry.getKey();
                        final Protocal p = (Protocal)entry.getValue();
                        if (p != null && p.isQoS()) {
                            if (p.getRetryCount() >= this.QOS_TRY_COUNT) {
                                if (this.DEBUG) {
                                    logger.debug("【IMCORE-netty" + this.debugTag + "】【QoS发送方】指纹为" + p.getFp() + "的消息包重传次数已达" + p.getRetryCount() + "(最多" + this.QOS_TRY_COUNT + "次)上限，将判定为丢包！");
                                }

                                lostMessages.add((Protocal)p.clone());
                                this.remove(p.getFp());
                            } else {
                                long delta = System.currentTimeMillis() - (Long)this.sendMessagesTimestamp.get(key);
                                if (delta <= (long)this.MESSAGES_JUST$NOW_TIME) {
                                    if (this.DEBUG) {
                                        logger.warn("【IMCORE-netty" + this.debugTag + "】【QoS发送方】指纹为" + key + "的包距\"刚刚\"发出才" + delta + "ms(<=" + this.MESSAGES_JUST$NOW_TIME + "ms将被认定是\"刚刚\"), 本次不需要重传哦.");
                                    }
                                } else {
                                    MBObserver sendResultObserver = new MBObserver() {
                                        public void update(boolean sendOK, Object extraObj) {
                                            if (sendOK) {
                                                if (QoS4SendDaemonRoot.this.DEBUG) {
                                                    QoS4SendDaemonRoot.logger.debug("【IMCORE-netty" + QoS4SendDaemonRoot.this.debugTag + "】【QoS发送方】指纹为" + p.getFp() + "的消息包已成功进行重传，此次之后重传次数已达" + p.getRetryCount() + "(最多" + QoS4SendDaemonRoot.this.QOS_TRY_COUNT + "次).");
                                                }
                                            } else if (QoS4SendDaemonRoot.this.DEBUG) {
                                                QoS4SendDaemonRoot.logger.warn("【IMCORE-netty" + QoS4SendDaemonRoot.this.debugTag + "】【QoS发送方】指纹为" + p.getFp() + "的消息包重传失败，它的重传次数之前已累计为" + p.getRetryCount() + "(最多" + QoS4SendDaemonRoot.this.QOS_TRY_COUNT + "次).");
                                            }

                                        }
                                    };
                                    LocalSendHelper.sendData(p, sendResultObserver);
                                    p.increaseRetryCount();
                                }
                            }
                        } else {
                            this.remove(key);
                        }
                    }
                }
            } catch (Exception var9) {
                if (this.DEBUG) {
                    logger.warn("【IMCORE-netty" + this.debugTag + "】【QoS发送方】消息发送质量保证线程运行时发生异常," + var9.getMessage(), var9);
                }
            }

            if (lostMessages != null && lostMessages.size() > 0) {
                this.notifyMessageLost(lostMessages);
            }

            this._excuting = false;
        }

    }

    protected void notifyMessageLost(ArrayList<Protocal> lostMessages) {
        if (this.serverLauncher != null && this.serverLauncher.getServerMessageQoSEventListener() != null) {
            this.serverLauncher.getServerMessageQoSEventListener().messagesLost(lostMessages);
        }

    }

    public QoS4SendDaemonRoot startup(boolean immediately) {
        this.stop();
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                QoS4SendDaemonRoot.this.doTaskOnece();
            }
        }, (long)(immediately ? 0 : this.CHECH_INTERVAL), (long)this.CHECH_INTERVAL);
        logger.debug("【IMCORE-netty" + this.debugTag + "】【QoS发送方】=========== 消息发送质量保证线程已成功启动");
        return this;
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

    public boolean exist(String fingerPrint) {
        return this.sentMessages.get(fingerPrint) != null;
    }

    public void put(Protocal p) {
        if (p == null) {
            if (this.DEBUG) {
                logger.warn(this.debugTag + "Invalid arg p==null.");
            }

        } else if (p.getFp() == null) {
            if (this.DEBUG) {
                logger.warn(this.debugTag + "Invalid arg p.getFp() == null.");
            }

        } else if (!p.isQoS()) {
            if (this.DEBUG) {
                logger.warn(this.debugTag + "This protocal is not QoS pkg, ignore it!");
            }

        } else {
            if (this.sentMessages.get(p.getFp()) != null && this.DEBUG) {
                logger.warn("【IMCORE-netty" + this.debugTag + "】【QoS发送方】指纹为" + p.getFp() + "的消息已经放入了发送质量保证队列，该消息为何会重复？（生成的指纹码重复？还是重复put？）");
            }

            this.sentMessages.put(p.getFp(), p);
            this.sendMessagesTimestamp.put(p.getFp(), System.currentTimeMillis());
        }
    }

    public void remove(String fingerPrint) {
        try {
            this.sendMessagesTimestamp.remove(fingerPrint);
            Object result = this.sentMessages.remove(fingerPrint);
            if (this.DEBUG) {
                logger.warn("【IMCORE-netty" + this.debugTag + "】【QoS发送方】指纹为" + fingerPrint + "的消息已成功从发送质量保证队列中移除(可能是收到接收方的应答也可能是达到了重传的次数上限)，重试次数=" + (result != null ? ((Protocal)result).getRetryCount() : "none呵呵."));
            }
        } catch (Exception var3) {
            if (this.DEBUG) {
                logger.warn("【IMCORE-netty" + this.debugTag + "】【QoS发送方】remove(fingerPrint)时出错了：", var3);
            }
        }

    }

    public int size() {
        return this.sentMessages.size();
    }

    public void setServerLauncher(ServerLauncher serverLauncher) {
        this.serverLauncher = serverLauncher;
    }

    public QoS4SendDaemonRoot setDebugable(boolean debugable) {
        this.DEBUG = debugable;
        return this;
    }

    public boolean isDebugable() {
        return this.DEBUG;
    }
}
