package mod.mh48.p2p48.test.directnettytest;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioSocketChannel;
import mod.mh48.p2p48.P2PReliableSocket;
import mod.mh48.p2p48.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class nettytestclient {
    public static void main(String[] args){


        try {
            System.out.println("id:");
            Scanner my_scan = new Scanner(System.in);
            String id = my_scan.nextLine();
            P2PReliableSocket s = Utils.connectToServer(new InetSocketAddress("185.213.25.234", 12001), new URI("ws://185.213.25.234:27776"), id);
            Channel channel = new OioSocketChannel(s);

            Bootstrap b = new Bootstrap();
            System.out.println("1");
            b.group(new OioEventLoopGroup());


            System.out.println("2");
            ChannelFuture f = b.config().group().register(channel);
            channel.pipeline().addLast(new testnh());
            System.out.println("3");
            System.out.println("Connected successful to ("+channel.remoteAddress()+") with ("+channel.localAddress()+")");
            ByteBuf buf = f.channel().alloc().buffer();
            System.out.println(f);
            Utils.writeString(buf,"test");
            f = f.channel().writeAndFlush(buf);
            f.sync();
            System.out.println("4");
            f.channel().closeFuture().sync();

        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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
            Utils.writeString(buf,"lol"+s);
            ctx.channel().writeAndFlush(buf);
        }
    }
}
