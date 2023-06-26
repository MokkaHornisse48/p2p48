package mod.mh48.p2p48;

import io.netty.buffer.ByteBuf;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Utils {
    public static byte[] recivepacket(DatagramSocket socket,int buffer) throws IOException {
        DatagramPacket dp = new DatagramPacket(new byte[buffer],buffer);
        socket.receive(dp);
        byte[] data = new byte[dp.getLength()];
        System.arraycopy(dp.getData(), dp.getOffset(), data, 0, dp.getLength());
        return data;
    }

    public static InetSocketAddress ADDRfromString(String data){
        JSONObject c = new JSONObject(data);
        return ADDRfromJSON(c);
    }

    public static InetSocketAddress ADDRfromJSON(JSONObject c){
        return new InetSocketAddress(c.getString("ip"), c.getInt("port"));
    }

    public static JSONObject ADDRtoJson(InetSocketAddress addr){
        JSONObject c = new JSONObject();
        c.put("ip",addr.getAddress().getHostAddress());
        c.put("port",addr.getPort());
        return c;
    }

    public static P2PReliableSocket connectToServer(InetSocketAddress punchAddr,URI comuri,String id) throws IOException {
        P2PReliableSocket.DSADDR ma = P2PReliableSocket.punchUDP(punchAddr);
        System.out.println("Punched udp with:"+ma.addr);
        ComTool ct = new ComTool(comuri);
        InetSocketAddress addr = ct.connect(ma.addr, id);
        System.out.println("Got remote addr:"+addr);
        ct.close();
        P2PReliableSocket s = new P2PReliableSocket(ma.socket);
        s.connect(addr);
        while(!s.isConnected()){}
        return s;
    }

    public static String readString(ByteBuf buffer){//test netty
        int len = buffer.readInt();
        byte[] abyte = new byte[len];
        buffer.readBytes(abyte);
        return  new String(abyte);
    }

    public static void writeString(ByteBuf buffer,String str){
        byte[] abyte = str.getBytes();
        buffer.writeInt(abyte.length);
        buffer.writeBytes(abyte);
    }
}
