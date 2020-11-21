//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.nettime.mobileimsdk.server.netty;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.ServerSocketChannelConfig;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public class MBUDPServerChannelConfig extends DefaultChannelConfig implements ServerSocketChannelConfig {
    private final DatagramChannel datagramChannel;

    public MBUDPServerChannelConfig(Channel channel, DatagramChannel datagramChannel) {
        super(channel);
        this.datagramChannel = datagramChannel;
        this.setRecvByteBufAllocator(new FixedRecvByteBufAllocator(2048));
    }

    public int getBacklog() {
        return 1;
    }

    public ServerSocketChannelConfig setBacklog(int backlog) {
        return this;
    }

    public ServerSocketChannelConfig setConnectTimeoutMillis(int timeout) {
        return this;
    }

    public ServerSocketChannelConfig setPerformancePreferences(int arg0, int arg1, int arg2) {
        return this;
    }

    public ServerSocketChannelConfig setAllocator(ByteBufAllocator alloc) {
        super.setAllocator(alloc);
        return this;
    }

    public ServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator alloc) {
        super.setRecvByteBufAllocator(alloc);
        return this;
    }

    public ServerSocketChannelConfig setAutoRead(boolean autoread) {
        super.setAutoRead(true);
        return this;
    }

    /** @deprecated */
    @Deprecated
    public ServerSocketChannelConfig setMaxMessagesPerRead(int n) {
        super.setMaxMessagesPerRead(n);
        return this;
    }

    public ServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator est) {
        super.setMessageSizeEstimator(est);
        return this;
    }

    public ServerSocketChannelConfig setWriteSpinCount(int spincount) {
        super.setWriteSpinCount(spincount);
        return this;
    }

    public ServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        return (ServerSocketChannelConfig)super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    }

    public ServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        return (ServerSocketChannelConfig)super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    }

    public ServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        return (ServerSocketChannelConfig)super.setWriteBufferWaterMark(writeBufferWaterMark);
    }

    public int getReceiveBufferSize() {
        try {
            return this.datagramChannel.socket().getReceiveBufferSize();
        } catch (SocketException var2) {
            throw new ChannelException(var2);
        }
    }

    public ServerSocketChannelConfig setReceiveBufferSize(int size) {
        try {
            this.datagramChannel.socket().setReceiveBufferSize(size);
            return this;
        } catch (SocketException var3) {
            throw new ChannelException(var3);
        }
    }

    public boolean isReuseAddress() {
        try {
            return this.datagramChannel.socket().getReuseAddress();
        } catch (SocketException var2) {
            throw new ChannelException(var2);
        }
    }

    public ServerSocketChannelConfig setReuseAddress(boolean reuseaddr) {
        try {
            this.datagramChannel.socket().setReuseAddress(true);
            return this;
        } catch (SocketException var3) {
            throw new ChannelException(var3);
        }
    }
}
