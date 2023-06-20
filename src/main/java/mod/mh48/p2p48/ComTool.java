package mod.mh48.p2p48;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class ComTool extends WebSocketClient{

    public Mode mode = Mode.Other;

    public static final int protocolVersion = 3;

    public int remotePV;

    public String cid;

    public String secret;

    public InetSocketAddress addr;

    public CountDownLatch pinglatch;

    public CountDownLatch loginlatch;

    Function<InetSocketAddress, InetSocketAddress> connecthandler;

    public ComTool(URI addr){
        super(addr);
    }

    public boolean start(){
        if(remotePV==protocolVersion && isOpen())return true;
        try {
            pinglatch = new CountDownLatch(1);
            if(connectBlocking()) {
                send("ping", new JSONObject());
                pinglatch.await();
                return remotePV == protocolVersion;
            }
        } catch (InterruptedException e) {}
        return false;
    }

    public boolean login(String name,boolean isPublic,Function<InetSocketAddress, InetSocketAddress> ch) {
        mode = Mode.Host;
        if(start()){
            loginlatch = new CountDownLatch(1);
            JSONObject data = new JSONObject();
            data.put("name",name);
            data.put("isPublic",isPublic);
            send("login",data);
            try {
                loginlatch.await();
                connecthandler = ch;
                return true;
            } catch (InterruptedException e) {}
        }
        return false;
    }

    public boolean relogin(){
        if(loginlatch!=null&&start()){
            loginlatch = new CountDownLatch(1);
            JSONObject data = new JSONObject();
            data.put("cid",cid);
            data.put("secret",secret);
            send("relogin",data);
            try {
                loginlatch.await();
                return true;
            } catch (InterruptedException e) {}
        }
        return false;
    }

    public InetSocketAddress connect(InetSocketAddress paddr,String id){
        mode = Mode.Client;
        if(start()){
            loginlatch = new CountDownLatch(1);
            JSONObject data = new JSONObject();
            data.put("addr",Utils.ADDRtoJson(paddr));
            data.put("cid",id);
            send("connect",data);
            try {
                loginlatch.await();
                return addr;
            } catch (InterruptedException e) {}
        }
        return null;
    }

    public void send(String id,JSONObject data){
        data.put("id",id);
        send(data.toString());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        JSONObject msg = new JSONObject(message);
        String id = msg.getString("id");
        switch (id) {
            case "pong":
                remotePV = msg.getInt("pv");
                pinglatch.countDown();
                break;
            case "login":
                cid = msg.getString("cid");
                secret = msg.getString("secret");
                loginlatch.countDown();
                break;
            case "connect":
                if(mode == Mode.Client){
                    //System.out.println(msg);
                    addr = Utils.ADDRfromJSON(msg.getJSONObject("addr"));
                    loginlatch.countDown();
                }
                if(mode == Mode.Host){
                    InetSocketAddress naddr = connecthandler.apply(Utils.ADDRfromJSON(msg.getJSONObject("addr")));
                    msg.put("addr",Utils.ADDRtoJson(naddr));
                    //System.out.println(msg);
                    send("connect",msg);
                }
                break;
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public enum Mode{
        Host,Client,Other
    }
}
