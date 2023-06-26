package mod.mh48.p2p48.test.nettylocaltest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import mod.mh48.p2p48.NettyLocalClientConnectorRUDP;
import mod.mh48.p2p48.Utils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.time.LocalTime;

public class nettyclienttestlol {

    public static void main(String[] args) {
        System.out.println("id:");
        Scanner my_scan = new Scanner(System.in);
        String pid = my_scan.nextLine();
        try {
            NettyLocalClientConnectorRUDP p2pConnection = new NettyLocalClientConnectorRUDP(new InetSocketAddress("185.213.25.234", 12001), new URI("ws://185.213.25.234:27776"), pid);
            LocalAddress address = p2pConnection.getAddr();
            Bootstrap b = new Bootstrap();
            System.out.println("1");
            b.group(new NioEventLoopGroup())
                    .channel(LocalChannel.class)
                    .handler(new ChannelInitializer<LocalChannel>() { // (4)
                        @Override
                        public void initChannel(LocalChannel ch) throws Exception {
                            ch.pipeline().addLast(new testnh());
                        }
                    });
            System.out.println("2");
            ChannelFuture f = b.connect(address).sync();
            System.out.println("3");
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Connected successful to (" + future.channel().remoteAddress() + ") with (" + future.channel().localAddress() + ")");
                ByteBuf buf = f.channel().alloc().buffer();
                buf.writeDouble(LocalTime.now().toNanoOfDay());
                f.channel().writeAndFlush(buf);
            });
            System.out.println("4");
            Channel channel = f.channel();
            while (true){
                ByteBuf buf = f.channel().alloc().buffer();
                buf.writeDouble(LocalTime.now().toNanoOfDay());
                f.channel().writeAndFlush(buf);
            }
            //f.channel().closeFuture().sync();

        } catch (
                InterruptedException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public static class testnh extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msgo) {
            ByteBuf msg = ((ByteBuf) msgo);
            double t = msg.readDouble();
            System.out.println((LocalTime.now().toNanoOfDay()-t)*1000000000);
        }
    }
}
