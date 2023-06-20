package mod.mh48.p2p48.test.simpletest;

import mod.mh48.p2p48.ComTool;
import mod.mh48.p2p48.P2PReliableSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class testclient {
    public static void main(String[] args) throws IOException, URISyntaxException {
        P2PReliableSocket.DSADDR ma = P2PReliableSocket.punchUDP(new InetSocketAddress("185.213.25.234", 12001));

        ComTool ct = new ComTool(new URI("ws://185.213.25.234:27776"));
        System.out.println("id:");
        Scanner my_scan = new Scanner(System.in);
        String id = my_scan.nextLine();
        InetSocketAddress addr = ct.connect(ma.addr, id);
        P2PReliableSocket s = new P2PReliableSocket(ma.socket);
        s.connect(addr);
        while(!s.isConnected()){}
        System.out.println("is connected (: ");

        OutputStream bo = s.getOutputStream();
        InputStream ai = s.getInputStream();
        //s.setSoTimeout(1);
        bo.write("Hallo".getBytes());
        bo.flush();
        System.out.println("cool");
        byte[] bs = new byte[10];
        ai.read(bs);
        System.out.println(new String(bs));
    }
}
