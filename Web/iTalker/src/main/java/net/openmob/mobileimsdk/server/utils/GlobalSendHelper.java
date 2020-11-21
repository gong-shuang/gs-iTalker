//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openmob.mobileimsdk.server.utils;

import io.netty.channel.Channel;
import net.nettime.mobileimsdk.server.bridge.QoS4ReciveDaemonC2B;
import net.nettime.mobileimsdk.server.netty.MBObserver;
import net.openmob.mobileimsdk.server.ServerCoreHandler;
import net.openmob.mobileimsdk.server.ServerLauncher;
import net.openmob.mobileimsdk.server.processor.BridgeProcessor;
import net.openmob.mobileimsdk.server.processor.OnlineProcessor;
import net.openmob.mobileimsdk.server.protocal.Protocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalSendHelper {
    private static Logger logger = LoggerFactory.getLogger(ServerCoreHandler.class);

    public GlobalSendHelper() {
    }

    public static void sendDataC2C(BridgeProcessor bridgeProcessor, final Channel session, final Protocal pFromClient, final String remoteAddress, final ServerCoreHandler serverCoreHandler) throws Exception {
        OnlineProcessor.getInstance().__printOnline();
        boolean needDelegateACK = false;
        MBObserver resultObserver;
        if (ServerLauncher.bridgeEnabled && !OnlineProcessor.isOnline(pFromClient.getTo())) {
            logger.debug("[IMCORE-netty<C2C>-桥接↑]>> 客户端" + pFromClient.getTo() + "不在线，数据[from:" + pFromClient.getFrom() + ",fp:" + pFromClient.getFp() + "to:" + pFromClient.getTo() + ",content:" + pFromClient.getDataContent() + "] 将通过MQ直发Web服务端（彼时在线则通过web实时发送、否则通过Web端进" + "行离线存储）【第一阶段APP+WEB跨机通信算法】！");
            if (pFromClient.isQoS() && QoS4ReciveDaemonC2B.getInstance().hasRecieved(pFromClient.getFp())) {
                needDelegateACK = true;
            } else {
                boolean toMQ = bridgeProcessor.publish(pFromClient.toGsonString());
                if (toMQ) {
                    logger.debug("[IMCORE-netty<C2C>-桥接↑]>> 客户端" + remoteAddress + "的数据已跨机器送出成功【OK】。(数据[from:" + pFromClient.getFrom() + ",fp:" + pFromClient.getFp() + ",to:" + pFromClient.getTo() + ",content:" + pFromClient.getDataContent() + "]【第一阶段APP+WEB跨机通信算法】)");
                    if (pFromClient.isQoS()) {
                        needDelegateACK = true;
                    }
                } else {
                    logger.debug("[IMCORE-netty<C2C>-桥接↑]>> 客户端" + remoteAddress + "的数据已跨机器送出失败，将作离线处理了【NO】。(数据[from:" + pFromClient.getFrom() + ",fp:" + pFromClient.getFp() + "to:" + pFromClient.getTo() + ",content:" + pFromClient.getDataContent() + "]【第一阶段APP+WEB跨机通信算法】)");
                    boolean offlineProcessedOK = serverCoreHandler.getServerEventListener().onTransBuffer_C2C_RealTimeSendFaild_CallBack(pFromClient);
                    if (pFromClient.isQoS() && offlineProcessedOK) {
                        needDelegateACK = true;
                    } else {
                        logger.warn("[IMCORE-netty<C2C>-桥接↑]>> 客户端" + remoteAddress + "的通用数据传输消息尝试实时发送没有成功，但上层应用层没有成" + "功(或者完全没有)进行离线存储，此消息将被服务端丢弃【第一阶段APP+WEB跨机通信算法】！");
                    }
                }
            }

            if (needDelegateACK) {
                resultObserver = new MBObserver() {
                    public void update(boolean receivedBackSendSucess, Object extraObj) {
                        if (receivedBackSendSucess) {
                            GlobalSendHelper.logger.debug("[IMCORE-netty<C2C>-桥接↑]【QoS_伪应答_C2S】向" + pFromClient.getFrom() + "发送" + pFromClient.getFp() + "的伪应答包成功,伪装from自：" + pFromClient.getTo() + "【第一阶段APP+WEB跨机通信算法】.");
                        }

                    }
                };
                LocalSendHelper.replyDelegateRecievedBack(session, pFromClient, resultObserver);
            }

            QoS4ReciveDaemonC2B.getInstance().addRecieved(pFromClient);
        } else {
            resultObserver = new MBObserver() {
                public void update(boolean sendOK, Object extraObj) {
                    if (sendOK) {
                        serverCoreHandler.getServerEventListener().onTransBuffer_C2C_CallBack(pFromClient);
                    } else {
                        GlobalSendHelper.logger.info("[IMCORE-netty<C2C>]>> 客户端" + remoteAddress + "的通用数据尝试实时发送没有成功，将交给应用层进行离线存储哦...");
                        boolean offlineProcessedOK = serverCoreHandler.getServerEventListener().onTransBuffer_C2C_RealTimeSendFaild_CallBack(pFromClient);
                        if (pFromClient.isQoS() && offlineProcessedOK) {
                            try {
                                MBObserver retObserver = new MBObserver() {
                                    public void update(boolean sucess, Object extraObj) {
                                        if (sucess) {
                                            GlobalSendHelper.logger.debug("[IMCORE-netty<C2C>]【QoS_伪应答_C2S】向" + pFromClient.getFrom() + "发送" + pFromClient.getFp() + "的伪应答包成功,from=" + pFromClient.getTo() + ".");
                                        }

                                    }
                                };
                                LocalSendHelper.replyDelegateRecievedBack(session, pFromClient, retObserver);
                            } catch (Exception var5) {
                                GlobalSendHelper.logger.warn(var5.getMessage(), var5);
                            }
                        } else {
                            GlobalSendHelper.logger.warn("[IMCORE-netty<C2C>]>> 客户端" + remoteAddress + "的通用数据传输消息尝试实时发送没有成功，但上层应用层没有成" + "功(或者完全没有)进行离线存储，此消息将被服务端丢弃！");
                        }
                    }

                }
            };
            LocalSendHelper.sendData(pFromClient, resultObserver);
        }

    }

    public static void sendDataS2C(BridgeProcessor bridgeProcessor, Protocal pFromClient, final MBObserver resultObserver) throws Exception {
        OnlineProcessor.getInstance().__printOnline();
        boolean sucess = false;
        if (ServerLauncher.bridgeEnabled && !OnlineProcessor.isOnline(pFromClient.getTo())) {
            logger.debug("[IMCORE-netty<S2C>-桥接↑]>> 客户端" + pFromClient.getTo() + "不在线，数据[from:" + pFromClient.getFrom() + ",fp:" + pFromClient.getFp() + "to:" + pFromClient.getTo() + ",content:" + pFromClient.getDataContent() + "] 将通过MQ直发Web服务端（彼时在线则通过web实时发送、否则通过Web端进" + "行离线存储）【第一阶段APP+WEB跨机通信算法】！");
            boolean toMQ = bridgeProcessor.publish(pFromClient.toGsonString());
            if (toMQ) {
                logger.debug("[IMCORE-netty<S2C>-桥接↑]>> 服务端的数据已跨机器送出成功【OK】。(数据[from:" + pFromClient.getFrom() + ",fp:" + pFromClient.getFp() + ",to:" + pFromClient.getTo() + ",content:" + pFromClient.getDataContent() + "]【第一阶段APP+WEB跨机通信算法】)");
                sucess = true;
            } else {
                logger.error("[IMCORE-netty<S2C>-桥接↑]>> 服务端的数据已跨机器送出失败，请通知管理员检查MQ中间件是否正常工作【NO】。(数据[from:" + pFromClient.getFrom() + ",fp:" + pFromClient.getFp() + "to:" + pFromClient.getTo() + ",content:" + pFromClient.getDataContent() + "]【第一阶段APP+WEB跨机通信算法】)");
            }

            if (resultObserver != null) {
                resultObserver.update(sucess, (Object)null);
            }

        } else {
            LocalSendHelper.sendData(pFromClient, new MBObserver() {
                public void update(boolean _sendSucess, Object extraObj) {
                    if (_sendSucess) {
                        _sendSucess = true;
                    } else {
                        GlobalSendHelper.logger.warn("[IMCORE-netty]>> 服务端的通用数据传输消息尝试实时发送没有成功，但上层应用层没有成功，请应用层自行决定此条消息的发送【NO】！");
                    }

                    if (resultObserver != null) {
                        resultObserver.update(_sendSucess, (Object)null);
                    }

                }
            });
        }
    }
}
