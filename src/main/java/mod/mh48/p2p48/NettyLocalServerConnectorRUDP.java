package mod.mh48.p2p48;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import net.rudp.ReliableSocket;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class NettyLocalServerConnectorRUDP {

    private ComTool comTool;

    private InetSocketAddress punchAddr;

    private volatile List<RUDP2NettyLocal> _clientSocks = new ArrayList<>();

    public EventLoopGroup workerGroup;

    public LocalAddress serverAddress;

    public NettyLocalServerConnectorRUDP(InetSocketAddress pPunchAddr, URI comuri, String name, boolean isPublic,LocalAddress pServerAddress){
        this(pPunchAddr,comuri,name,isPublic,pServerAddress,new NioEventLoopGroup());

    }

    public NettyLocalServerConnectorRUDP(InetSocketAddress pPunchAddr, URI comuri, String name, boolean isPublic,LocalAddress pServerAddress,EventLoopGroup pWorkerGroup){
        serverAddress = pServerAddress;
        comTool = new ComTool(comuri);
        comTool.login(name,isPublic,this::newconnect);
        punchAddr = pPunchAddr;
        workerGroup = pWorkerGroup;
    }

    private synchronized InetSocketAddress newconnect(InetSocketAddress addr) {
        System.out.println("connection from:" + addr);
        P2PReliableSocket.DSADDR ma;
        try {
            ma = P2PReliableSocket.punchUDP(punchAddr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("punched udp with:" + ma.addr);
        new Thread(() -> {
            P2PReliableSocket s = new P2PReliableSocket(ma.socket);
            try {
                s.connect(addr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Connected (:");
            _clientSocks.add(makeRUDP2NettyLocal(s));
        }).start();
        return ma.addr;
    }

    private synchronized RUDP2NettyLocal makeRUDP2NettyLocal(ReliableSocket socket){
        Bootstrap b = new Bootstrap();
        RUDP2NettyLocal rudp2NettyLocal = new RUDP2NettyLocal(socket);
        b.channel(LocalChannel.class);
        b.group(workerGroup);
        b.handler(new ChannelInitializer<LocalChannel>() {

            @Override
            public void initChannel(LocalChannel ch)
                    throws Exception {
                rudp2NettyLocal.channel = ch;
                ch.pipeline().addLast(rudp2NettyLocal);
            }
        });
        try {
            ChannelFuture f = b.connect(serverAddress).sync();
            System.out.println("generated rudp2NettyLocal:"+rudp2NettyLocal);
            return rudp2NettyLocal;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getId(){
        return comTool.cid;
    }


    public synchronized void close() throws IOException {
        for(RUDP2NettyLocal s:_clientSocks){
            s.socket.close();
            s.channel.close();
        }

        //todo comTool close
    }
}
