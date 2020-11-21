//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.nettime.mobileimsdk.server.bridge;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import java.io.IOException;
import java.util.Map;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQProvider {
    private static Logger logger = LoggerFactory.getLogger(MQProvider.class);
    public static final String DEFAULT_ENCODE_CHARSET = "UTF-8";
    public static final String DEFAULT_DECODE_CHARSET = "UTF-8";
    protected ConnectionFactory _factory;
    protected Connection _connection;
    protected Channel _pubChannel;
    protected final Timer timerForStartAgain;
    protected boolean startRunning;
    protected final Timer timerForRetryWorker;
    protected boolean retryWorkerRunning;
    protected ConcurrentLinkedQueue<String[]> publishTrayAgainCache;
    protected boolean publishTrayAgainEnable;
    protected Observer consumerObserver;
    protected String encodeCharset;
    protected String decodeCharset;
    protected String mqURI;
    protected String publishToQueue;
    protected String consumFromQueue;
    protected String TAG;

    public MQProvider(String mqURI, String publishToQueue, String consumFromQueue, String TAG, boolean publishTrayAgainEnable) {
        this(mqURI, publishToQueue, consumFromQueue, (String)null, (String)null, TAG, publishTrayAgainEnable);
    }

    public MQProvider(String mqURI, String publishToQueue, String consumFromQueue, String encodeCharset, String decodeCharset, String TAG, boolean publishTrayAgainEnable) {
        this._factory = null;
        this._connection = null;
        this._pubChannel = null;
        this.timerForStartAgain = new Timer();
        this.startRunning = false;
        this.timerForRetryWorker = new Timer();
        this.retryWorkerRunning = false;
        this.publishTrayAgainCache = new ConcurrentLinkedQueue();
        this.publishTrayAgainEnable = false;
        this.consumerObserver = null;
        this.encodeCharset = null;
        this.decodeCharset = null;
        this.mqURI = null;
        this.publishToQueue = null;
        this.consumFromQueue = null;
        this.TAG = null;
        this.mqURI = mqURI;
        this.publishToQueue = publishToQueue;
        this.consumFromQueue = consumFromQueue;
        this.encodeCharset = encodeCharset;
        this.decodeCharset = decodeCharset;
        this.TAG = TAG;
        if (this.mqURI == null) {
            throw new IllegalArgumentException("[" + TAG + "]无效的参数mqURI ！");
        } else if (this.publishToQueue == null && this.consumFromQueue == null) {
            throw new IllegalArgumentException("[" + TAG + "]无效的参数，publishToQueue和" + "consumFromQueue至少应设置其一！");
        } else {
            if (this.encodeCharset == null || this.encodeCharset.trim().length() == 0) {
                this.encodeCharset = "UTF-8";
            }

            if (this.decodeCharset == null || this.decodeCharset.trim().length() == 0) {
                this.decodeCharset = "UTF-8";
            }

            this.init();
        }
    }

    protected boolean init() {
        String uri = this.mqURI;
        this._factory = new ConnectionFactory();

        try {
            this._factory.setUri(uri);
        } catch (Exception var3) {
            logger.error("[" + this.TAG + "] - 【严重】factory.setUri()时出错，Uri格式不对哦，uri=" + uri, var3);
            return false;
        }

        this._factory.setAutomaticRecoveryEnabled(true);
        this._factory.setTopologyRecoveryEnabled(false);
        this._factory.setNetworkRecoveryInterval(5000);
        this._factory.setRequestedHeartbeat(30);
        this._factory.setConnectionTimeout(30000);
        return true;
    }

    protected Connection tryGetConnection() {
        if (this._connection == null) {
            try {
                this._connection = this._factory.newConnection();
                this._connection.addShutdownListener(new ShutdownListener() {
                    public void shutdownCompleted(ShutdownSignalException cause) {
                        MQProvider.logger.warn("[" + MQProvider.this.TAG + "] - 连接已经关闭了。。。。【NO】");
                    }
                });
                ((Recoverable)this._connection).addRecoveryListener(new RecoveryListener() {
                    public void handleRecovery(Recoverable arg0) {
                        MQProvider.logger.info("[" + MQProvider.this.TAG + "] - 连接已成功自动恢复了！【OK】");
                        MQProvider.this.start();
                    }
                });
            } catch (Exception var2) {
                logger.error("[" + this.TAG + "] - 【NO】getConnection()时出错了，原因是：" + var2.getMessage(), var2);
                this._connection = null;
                return null;
            }
        }

        return this._connection;
    }

    public void start() {
        if (!this.startRunning) {
            try {
                if (this._factory != null) {
                    Connection conn = this.tryGetConnection();
                    if (conn != null) {
                        this.whenConnected(conn);
                    } else {
                        logger.error("[" + this.TAG + "-↑] - [start()中]【严重】connction还没有准备好" + "，conn.createChannel()失败，start()没有继续！(原因：connction==null)【5秒后重新尝试start】");
                        this.timerForStartAgain.schedule(new TimerTask() {
                            public void run() {
                                MQProvider.this.start();
                            }
                        }, 5000L);
                    }
                } else {
                    logger.error("[" + this.TAG + "-↑] - [start()中]【严重】factory还没有准备好，start()失败！(原因：factory==null)");
                }
            } finally {
                this.startRunning = false;
            }

        }
    }

    protected void whenConnected(Connection conn) {
        this.startPublisher(conn);
        this.startWorker(conn);
    }

    protected void startPublisher(Connection conn) {
        if (conn != null) {
            if (this._pubChannel != null && this._pubChannel.isOpen()) {
                try {
                    this._pubChannel.close();
                } catch (Exception var8) {
                    logger.warn("[" + this.TAG + "-↑] - [startPublisher()中]pubChannel.close()时发生错误。", var8);
                }
            }

            try {
                this._pubChannel = conn.createChannel();
                logger.info("[" + this.TAG + "-↑] - [startPublisher()中] 的channel成功创建了，" + "马上开始循环publish消息，当前数组队列长度：N/A！【OK】");
                String queue = this.publishToQueue;
                boolean durable = true;
                boolean exclusive = false;
                boolean autoDelete = false;
                DeclareOk qOK = this._pubChannel.queueDeclare(queue, durable, exclusive, autoDelete, (Map)null);
                logger.info("[" + this.TAG + "-↑] - [startPublisher中] Queue[当前队列消息数：" + qOK.getMessageCount() + ",消费者：" + qOK.getConsumerCount() + "]已成功建立，Publisher初始化成功，" + "消息将可publish过去且不怕丢失了。【OK】(当前暂存数组长度:N/A)");
                if (this.publishTrayAgainEnable) {
                    while(this.publishTrayAgainCache.size() > 0) {
                        String[] m = (String[])this.publishTrayAgainCache.poll();
                        if (m == null || m.length <= 0) {
                            logger.debug("[" + this.TAG + "-↑] - [startPublisher()中] [___]在channel成功创建后，" + "当前之前失败暂存的数据队列已为空，publish没有继续！[当前数组队列长度：" + this.publishTrayAgainCache.size() + "]！【OK】");
                            break;
                        }

                        logger.debug("[" + this.TAG + "-↑] - [startPublisher()中] [...]在channel成功创建后，正在publish之前失败暂存的消息 m[0]=" + m[0] + "、m[1]=" + m[1] + ",、m[2]=" + m[2] + "，[当前数组队列长度：" + this.publishTrayAgainCache.size() + "]！【OK】");
                        this.publish(m[0], m[1], m[2]);
                    }
                }
            } catch (Exception var9) {
                logger.error("[" + this.TAG + "-↑] - [startPublisher()中] conn.createChannel()或pubChannel.queueDeclare()" + "出错了，本次startPublisher没有继续！", var9);
            }
        } else {
            logger.error("[" + this.TAG + "-↑] - [startPublisher()中]【严重】connction还没有准备好" + "，conn.createChannel()失败！(原因：connction==null)");
        }

    }

    public boolean publish(String message) {
        return this.publish("", this.publishToQueue, message);
    }

    protected boolean publish(String exchangeName, String routingKey, String message) {
        boolean ok = false;

        try {
            this._pubChannel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(this.encodeCharset));
            logger.info("[" + this.TAG + "-↑] - [startPublisher()中] publish()成功了 ！(数据:" + exchangeName + "," + routingKey + "," + message + ")");
            ok = true;
        } catch (Exception var6) {
            if (this.publishTrayAgainEnable) {
                this.publishTrayAgainCache.add(new String[]{exchangeName, routingKey, message});
            }

            logger.error("[" + this.TAG + "-↑] - [startPublisher()中] publish()时Exception了，" + "原因：" + var6.getMessage() + "【数据[" + exchangeName + "," + routingKey + "," + message + "]已重新放回数组首位" + "，当前数组长度：N/A】", var6);
        }

        return ok;
    }

    protected void startWorker(Connection conn) {
        if (!this.retryWorkerRunning) {
            try {
                if (conn == null) {
                    throw new Exception("[" + this.TAG + "-↓] - 【严重】connction还没有准备好，conn.createChannel()失败！(原因：connction==null)");
                }

                final Channel resumeChannel = conn.createChannel();
                String queueName = this.consumFromQueue;
                DefaultConsumer dc = new DefaultConsumer(resumeChannel) {
                    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                        String routingKey = envelope.getRoutingKey();
                        String contentType = properties.getContentType();
                        long deliveryTag = envelope.getDeliveryTag();
                        MQProvider.logger.info("[" + MQProvider.this.TAG + "-↓] - [startWorker()中] 收到一条新消息(routingKey=" + routingKey + ",contentType=" + contentType + ",consumerTag=" + consumerTag + ",deliveryTag=" + deliveryTag + ")，马上开始处理。。。。");
                        boolean workOK = MQProvider.this.work(body);
                        if (workOK) {
                            resumeChannel.basicAck(deliveryTag, false);
                        } else {
                            resumeChannel.basicReject(deliveryTag, true);
                        }

                    }
                };
                boolean autoAck = false;
                resumeChannel.basicConsume(queueName, autoAck, dc);
                logger.info("[" + this.TAG + "-↓] - [startWorker()中] Worker已经成功开启并运行中...【OK】");
            } catch (Exception var9) {
                logger.error("[" + this.TAG + "-↓] - [startWorker()中] conn.createChannel()或Consumer操作时" + "出错了，本次startWorker没有继续【暂停5秒后重试startWorker()】！", var9);
                this.timerForRetryWorker.schedule(new TimerTask() {
                    public void run() {
                        MQProvider.this.startWorker(MQProvider.this._connection);
                    }
                }, 5000L);
            } finally {
                this.retryWorkerRunning = false;
            }

        }
    }

    protected boolean work(byte[] contentBody) {
        try {
            String msg = new String(contentBody, this.decodeCharset);
            logger.info("[" + this.TAG + "-↓] - [startWorker()中] Got msg：" + msg);
            return true;
        } catch (Exception var3) {
            logger.warn("[" + this.TAG + "-↓] - [startWorker()中] work()出现错误，错误将被记录：" + var3.getMessage(), var3);
            return true;
        }
    }
}
