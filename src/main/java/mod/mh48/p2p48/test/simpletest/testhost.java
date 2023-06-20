package mod.mh48.p2p48.test.simpletest;

import mod.mh48.p2p48.ComTool;
import mod.mh48.p2p48.P2PReliableSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class testhost{

    public static void main(String[] args)  throws IOException, URISyntaxException {
        ComTool ct = new ComTool(new URI("ws://185.213.25.234:27776"));
        if(ct.login("test", true, inetSocketAddress ->
        {

            P2PReliableSocket.DSADDR ma;
            try {
                 ma = P2PReliableSocket.punchUDP(new InetSocketAddress("185.213.25.234",12001));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            new Thread((() -> {
                try {
                    P2PReliableSocket s = new P2PReliableSocket(ma.socket);
                    s.connect(inetSocketAddress);
                    System.out.println(inetSocketAddress);
                    System.out.println(ma.addr);
                    while(!s.isConnected()){}
                    System.out.println(1);
                    InputStream ai = s.getInputStream();
                    OutputStream bo= s.getOutputStream();
                    byte[] bs = new byte[10];
                    ai.read(bs);
                    System.out.println(new String(bs));
                    System.out.println(2);
                    bo.write("Hallo".getBytes());
                    bo.flush();
                    //s.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })).start();
            return ma.addr;
        }));
        System.out.println("id:"+ct.cid);
    }
}
