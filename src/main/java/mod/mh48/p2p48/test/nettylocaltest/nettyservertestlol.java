package mod.mh48.p2p48.test.nettylocaltest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import mod.mh48.p2p48.NettyLocalServerConnectorRUDP;
import mod.mh48.p2p48.Utils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;


public class nettyservertestlol {
    public static void main(String[] args){
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(new NioEventLoopGroup(),new NioEventLoopGroup())
                    .channel(LocalServerChannel.class) // (3)
                    .childHandler(new ChannelInitializer<LocalChannel>() { // (4)
                        @Override
                        public void initChannel(LocalChannel ch) throws Exception {
                            System.out.println("Connected netty:"+ch);
                            ch.pipeline().addLast(new testnh());
                        }
                    });

            // Bind and start to accept incoming connections.
            LocalAddress address = new LocalAddress("P2PS");
            NettyLocalServerConnectorRUDP nettyLocalServerConnectorRUDP = new NettyLocalServerConnectorRUDP(new InetSocketAddress("185.213.25.234",12001),new URI("ws://185.213.25.234:27776"),"test", true,address);
            System.out.println("id:"+nettyLocalServerConnectorRUDP.getId());
            ChannelFuture f = b.bind(address).sync(); // (7)
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Bind successful to "+future.channel().localAddress());
            });

            f.channel().closeFuture().sync();
            System.out.println("done");
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static class testnh extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msgo) { // (2)
            //System.out.println( msgo);
            ByteBuf msg = ((ByteBuf) msgo);
            // Discard the received data silently.
            String s = Utils.readString(msg);
            System.out.println(s);
            msg.release();
            ByteBuf buf = ctx.alloc().buffer();
            Utils.writeString(buf,s);
            ctx.channel().writeAndFlush(buf);
        }
    }
}
