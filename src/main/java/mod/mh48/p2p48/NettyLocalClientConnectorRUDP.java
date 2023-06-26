package mod.mh48.p2p48;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import net.rudp.ReliableSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.UUID;

public class NettyLocalClientConnectorRUDP {

    public ReliableSocket socket;

    public EventLoopGroup workerGroup;

    public LocalAddress serverAddress;

    public RUDP2NettyLocal rudp2NettyLocal;

    public Channel serverChanel;

    public NettyLocalClientConnectorRUDP(InetSocketAddress punchAddr, URI comuri, String id){
        this(punchAddr,comuri,id,new NioEventLoopGroup());
    }

    public NettyLocalClientConnectorRUDP(InetSocketAddress punchAddr, URI comuri, String id,EventLoopGroup pWorkerGroup){
        try {
            socket = Utils.connectToServer(punchAddr,comuri,id);
            rudp2NettyLocal = new RUDP2NettyLocal(socket);
            workerGroup = pWorkerGroup;
            serverAddress = new LocalAddress("P2PC/"+UUID.randomUUID());
            serverChanel = startLocalServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Channel startLocalServer() {
        ServerBootstrap b = new ServerBootstrap();
        b.group(workerGroup,workerGroup);
        b.channel(LocalServerChannel.class);
        b.childHandler(new ChannelInitializer<LocalChannel>() { // (4)
            @Override
            public void initChannel(LocalChannel ch) throws Exception {
                rudp2NettyLocal.channel = ch;
                ch.pipeline().addLast(rudp2NettyLocal);
            }
        });
        try {
            ChannelFuture f = b.bind(serverAddress).sync();
            return f.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalAddress getAddr(){
        return serverAddress;
    }
}
