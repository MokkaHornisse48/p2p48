package mod.mh48.p2p48;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.local.LocalChannel;
import net.rudp.ReliableSocket;
import net.rudp.ReliableSocketListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RUDP2NettyLocal extends ChannelInboundHandlerAdapter implements ReliableSocketListener {

    public ReliableSocket socket;
    public LocalChannel channel;

    public RUDP2NettyLocal(ReliableSocket pSocket){
        socket = pSocket;
        socket.addListener(this);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgo) {
        ByteBuf msg = ((ByteBuf) msgo);
        byte[] b = new byte[msg.readableBytes()];
        msg.readBytes(b);
        msg.release();
        try {
            OutputStream os = socket.getOutputStream();
            os.write(b);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);//todo error
        }
    }

    @Override
    public void packetSent() {}

    @Override
    public void packetRetransmitted() {}

    @Override
    public void packetReceivedInOrder() {
        recieve();
    }

    @Override
    public void packetReceivedOutOfOrder() {}

    public void recieve(){
        if (channel != null) {
            try {
                InputStream is = socket.getInputStream();
                int s = socket.dataAvailable();
                if (s > 0) {
                    //System.out.println("Recieved " + s + "bytes");
                    byte[] b = new byte[s];
                    is.read(b);
                    ByteBuf buf = channel.alloc().buffer(s);
                    buf.writeBytes(b);
                    channel.writeAndFlush(buf);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);//todo error
            }
        }
    }
}
