package net.nettime.mobileimsdk.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
//import io.netty.channel.AbstractChannel.AbstractUnsafe;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.RecyclableArrayList;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MBUDPChannel extends AbstractChannel {
    protected final ChannelMetadata metadata = new ChannelMetadata(false);
    protected final DefaultChannelConfig config = new DefaultChannelConfig(this);
    private final ConcurrentLinkedQueue<ByteBuf> buffers = new ConcurrentLinkedQueue();
    protected final MBUDPServerChannel serverchannel;
    protected final InetSocketAddress remote;
    private volatile boolean open = true;
    private boolean reading = false;

    protected MBUDPChannel(MBUDPServerChannel serverchannel, InetSocketAddress remote) {
        super(serverchannel);
        this.serverchannel = serverchannel;
        this.remote = remote;
    }

    public ChannelMetadata metadata() {
        return this.metadata;
    }

    public ChannelConfig config() {
        return this.config;
    }

    public boolean isActive() {
        return this.open;
    }

    public boolean isOpen() {
        return this.isActive();
    }

    protected void doClose() throws Exception {
        this.open = false;
        this.serverchannel.removeChannel(this);
    }

    protected void doDisconnect() throws Exception {
        this.doClose();
    }

    protected void addBuffer(ByteBuf buffer) {
        this.buffers.add(buffer);
    }

    protected void doBeginRead() throws Exception {
        if (!this.reading) {
            this.reading = true;

            try {
                ByteBuf buffer = null;

                while((buffer = (ByteBuf)this.buffers.poll()) != null) {
                    this.pipeline().fireChannelRead(buffer);
                }

                this.pipeline().fireChannelReadComplete();
            } finally {
                this.reading = false;
            }
        }
    }

    protected void doWrite(ChannelOutboundBuffer buffer) throws Exception {
        final RecyclableArrayList list = RecyclableArrayList.newInstance();
        boolean freeList = true;

        try {
            ByteBuf buf = null;

            while(true) {
                if ((buf = (ByteBuf)buffer.current()) == null) {
                    freeList = false;
                    break;
                }

                list.add(buf.retain());
                buffer.remove();
            }
        } finally {
            if (freeList) {
                Iterator var7 = list.iterator();

                while(var7.hasNext()) {
                    Object obj = var7.next();
                    ReferenceCountUtil.safeRelease(obj);
                }

                list.recycle();
            }

        }

        this.serverchannel.eventLoop().execute(new Runnable() {
            public void run() {
                try {
                    Iterator var2 = list.iterator();

                    while(var2.hasNext()) {
                        Object buf = var2.next();
                        MBUDPChannel.this.serverchannel.unsafe().write(new DatagramPacket((ByteBuf)buf, MBUDPChannel.this.remote), MBUDPChannel.this.voidPromise());
                    }

                    MBUDPChannel.this.serverchannel.unsafe().flush();
                } finally {
                    list.recycle();
                }
            }
        });
    }

    protected boolean isCompatible(EventLoop eventloop) {
        return eventloop instanceof DefaultEventLoop;
    }

    protected AbstractUnsafe newUnsafe() {
        return new MBUDPChannel.UdpChannelUnsafe();
    }

    protected SocketAddress localAddress0() {
        return this.serverchannel.localAddress0();
    }

    protected SocketAddress remoteAddress0() {
        return this.remote;
    }

    protected void doBind(SocketAddress addr) throws Exception {
        throw new UnsupportedOperationException();
    }

    private class UdpChannelUnsafe extends AbstractUnsafe {
        private UdpChannelUnsafe() {
            super();
        }

        public void connect(SocketAddress addr1, SocketAddress addr2, ChannelPromise pr) {
            throw new UnsupportedOperationException();
        }
    }
}
