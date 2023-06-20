package mod.mh48.p2p48;

import net.rudp.ReliableSocket;

import java.io.IOException;
import java.net.*;
import java.util.AbstractMap;
import java.util.Map;

public class P2PReliableSocket extends ReliableSocket {

    public P2PReliableSocket(DatagramSocket sock) {
        super(sock);
    }

    public static DSADDR punchUDP(SocketAddress punchHelper) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.send(new DatagramPacket("MachMal".getBytes(),7,punchHelper));
        byte[] d = Utils.recivepacket(socket,100);
        //P2PReliableSocket p2pReliableSocket = new P2PReliableSocket(socket);
        //p2pReliableSocket.externaddr = Utils.ADDRfromString(new String(d));
        return new DSADDR(socket,Utils.ADDRfromString(new String(d)));
    }

    public static class DSADDR{
        public final InetSocketAddress addr;
        public final DatagramSocket socket;

        public DSADDR(DatagramSocket socket,InetSocketAddress addr) {
            this.addr = addr;
            this.socket = socket;
        }
    }

}
