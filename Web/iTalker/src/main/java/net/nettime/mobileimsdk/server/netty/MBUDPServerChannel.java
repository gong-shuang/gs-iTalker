//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.nettime.mobileimsdk.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.RecvByteBufAllocator.Handle;
import io.netty.channel.nio.AbstractNioMessageChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannelConfig;
import io.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class MBUDPServerChannel extends AbstractNioMessageChannel implements ServerSocketChannel {
    private final ChannelMetadata METADATA;
    private final MBUDPServerChannelConfig config;
    protected final LinkedHashMap<InetSocketAddress, MBUDPChannel> channels;

    public MBUDPServerChannel() throws IOException {
        this(SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET));
    }

    protected MBUDPServerChannel(DatagramChannel datagramChannel) {
        super((Channel)null, datagramChannel, 1);
        this.METADATA = new ChannelMetadata(true);
        this.channels = new LinkedHashMap();
        this.config = new MBUDPServerChannelConfig(this, datagramChannel);
    }

    public InetSocketAddress localAddress() {
        return (InetSocketAddress)super.localAddress();
    }

    protected SocketAddress localAddress0() {
        return this.javaChannel().socket().getLocalSocketAddress();
    }

    public InetSocketAddress remoteAddress() {
        return null;
    }

    protected SocketAddress remoteAddress0() {
        return null;
    }

    public ChannelMetadata metadata() {
        return this.METADATA;
    }

    public ServerSocketChannelConfig config() {
        return this.config;
    }

    public boolean isActive() {
        return this.javaChannel().isOpen() && this.javaChannel().socket().isBound();
    }

    protected DatagramChannel javaChannel() {
        return (DatagramChannel)super.javaChannel();
    }

    protected void doBind(SocketAddress localAddress) throws Exception {
        this.javaChannel().socket().bind(localAddress);
    }

    protected void doClose() throws Exception {
        Iterator var2 = this.channels.values().iterator();

        while(var2.hasNext()) {
            MBUDPChannel channel = (MBUDPChannel)var2.next();
            channel.close();
        }

        this.javaChannel().close();
    }

    public void removeChannel(final Channel channel) {
        this.eventLoop().submit(new Runnable() {
            public void run() {
                InetSocketAddress remote = (InetSocketAddress)channel.remoteAddress();
                if (MBUDPServerChannel.this.channels.get(remote) == channel) {
                    MBUDPServerChannel.this.channels.remove(remote);
                }

            }
        });
    }

    protected int doReadMessages(List<Object> list) throws Exception {
        DatagramChannel javaChannel = this.javaChannel();
        Handle allocatorHandle = this.unsafe().recvBufAllocHandle();
        ByteBuf buffer = allocatorHandle.allocate(this.config.getAllocator());
        allocatorHandle.attemptedBytesRead(buffer.writableBytes());
        boolean freeBuffer = true;

        try {
            ByteBuffer nioBuffer = buffer.internalNioBuffer(buffer.writerIndex(), buffer.writableBytes());
            int nioPos = nioBuffer.position();
            InetSocketAddress inetSocketAddress = (InetSocketAddress)javaChannel.receive(nioBuffer);
            if (inetSocketAddress == null) {
                return 0;
            }

            allocatorHandle.lastBytesRead(nioBuffer.position() - nioPos);
            buffer.writerIndex(buffer.writerIndex() + allocatorHandle.lastBytesRead());
            MBUDPChannel udpchannel = (MBUDPChannel)this.channels.get(inetSocketAddress);
            if (udpchannel != null && udpchannel.isOpen()) {
                udpchannel.addBuffer(buffer);
                freeBuffer = false;
                if (udpchannel.isRegistered()) {
                    udpchannel.read();
                }

                return 0;
            }

            udpchannel = new MBUDPChannel(this, inetSocketAddress);
            this.channels.put(inetSocketAddress, udpchannel);
            list.add(udpchannel);
            udpchannel.addBuffer(buffer);
            freeBuffer = false;
        } catch (Throwable var13) {
            PlatformDependent.throwException(var13);
            return -1;
        } finally {
            if (freeBuffer) {
                buffer.release();
            }

        }

        return 1;
    }

    protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer buffer) throws Exception {
        DatagramPacket dpacket = (DatagramPacket)msg;
        InetSocketAddress recipient = (InetSocketAddress)dpacket.recipient();
        ByteBuf byteBuf = (ByteBuf)dpacket.content();
        int readableBytes = byteBuf.readableBytes();
        if (readableBytes == 0) {
            return true;
        } else {
            ByteBuffer internalNioBuffer = byteBuf.internalNioBuffer(byteBuf.readerIndex(), readableBytes);
            return this.javaChannel().send(internalNioBuffer, recipient) > 0;
        }
    }

    protected boolean doConnect(SocketAddress addr1, SocketAddress addr2) throws Exception {
        throw new UnsupportedOperationException();
    }

    protected void doFinishConnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    protected void doDisconnect() throws Exception {
        throw new UnsupportedOperationException();
    }
}
