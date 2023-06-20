package mod.mh48.p2p48.test;

import mod.mh48.p2p48.ComTool;
import mod.mh48.p2p48.P2PReliableSocket;
import mod.mh48.p2p48.P2PServerSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class testhost {

    public static void main(String[] args)  throws IOException, URISyntaxException {
        P2PServerSocket server = new P2PServerSocket(new InetSocketAddress("185.213.25.234",12001),new URI("ws://185.213.25.234:27776"),"test", true);
        System.out.println("id:"+server.getId());
        Socket client = server.accept();
        InputStream clientInputStream = client.getInputStream();
        OutputStream clientOutputStream= client.getOutputStream();
        byte[] bs = new byte[10];
        clientInputStream.read(bs);
        System.out.println(new String(bs));
        clientOutputStream.write("Hallo".getBytes());
        clientOutputStream.flush();
    }
}
