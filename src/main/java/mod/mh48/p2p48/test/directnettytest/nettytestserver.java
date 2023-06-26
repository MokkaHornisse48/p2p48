package mod.mh48.p2p48.test.directnettytest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import mod.mh48.p2p48.P2PServerSocket;
import mod.mh48.p2p48.Utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class nettytestserver {
    public static Channel channel;

    public static void main(String[] args){

        try {
            P2PServerSocket server = new P2PServerSocket(new InetSocketAddress("185.213.25.234",12001),new URI("ws://185.213.25.234:27776"),"test", true);
            System.out.println("id:"+server.getId());
            channel = new OioServerSocketChannel(server);

            ServerBootstrap b = new ServerBootstrap(); // (2)

            b.group(new OioEventLoopGroup(),new OioEventLoopGroup());



            b
                    // (3)
                    .childHandler(new ChannelInitializer<Channel>() { // (4)
                        @Override
                        public void initChannel(Channel ch) throws Exception {
                            System.out.println("c"+ch);
                            ch.pipeline().addLast(new testnh());
                        }
                    });


            System.out.println(channel.pipeline());
            System.out.println(channel.pipeline().last());
            System.out.println(channel.isRegistered());
            System.out.println(b.config().group());



            try {
                ReflectionUtils.initmethod.invoke(b,channel);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            ChannelFuture f = b.config().group().register(channel).sync(); // (7)


            // Bind and start to accept incoming connections.

            //
            System.out.println(channel.isRegistered());
            System.out.println(channel.eventLoop());
            System.out.println(channel.pipeline());
            System.out.println(channel.pipeline().last());


            /*
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Bind successful to "+future.channel().localAddress());
                new Thread(clientServer).start();
            });*/


            channel.closeFuture().sync();
            System.out.println("done");
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
            System.out.println( msgo);
            ByteBuf msg = ((ByteBuf) msgo);
            // Discard the received data silently.
            //String s = Utils.readString(msg);
            //System.out.println(s);
            //ByteBuf buf = ctx.alloc().buffer();
            //Utils.writeString(buf,"yay"+s);
            //ctx.channel().writeAndFlush(buf);
            ByteBuf buf = ctx.alloc().buffer();
            Utils.writeString(buf,"test");
            ctx.writeAndFlush(buf);
            ctx.flush();
            msg.release();
        }
    }
}