package mod.mh48.p2p48;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class P2PServerSocket extends ServerSocket {

    private boolean _closed = false;

    private ComTool comTool;

    private InetSocketAddress punchAddr;

    private volatile List<P2PReliableSocket> _clientSocks = new ArrayList<>();

    private volatile BlockingQueue<P2PReliableSocket.DSADDR> _backlog = new LinkedBlockingDeque<>();


    public P2PServerSocket(InetSocketAddress pPunchAddr,URI comuri,String name,boolean isPublic) throws IOException {
        comTool = new ComTool(comuri);
        comTool.login(name,isPublic,this::newconnect);
        punchAddr = pPunchAddr;
    }

    private InetSocketAddress newconnect(InetSocketAddress addr){
        P2PReliableSocket.DSADDR ma;
        try {
            ma = P2PReliableSocket.punchUDP(punchAddr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        _backlog.add(new P2PReliableSocket.DSADDR(ma.socket,addr));
        return ma.addr;
    }

    public String getId(){
        return comTool.cid;
    }

    public Socket accept() throws IOException
    {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        synchronized (_backlog) {
            P2PReliableSocket.DSADDR dsaddr = null;
            try {
                dsaddr = _backlog.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            P2PReliableSocket s = new P2PReliableSocket(dsaddr.socket);
            s.connect(dsaddr.addr);
            return s;
        }
    }

    public synchronized void bind(SocketAddress endpoint) throws IOException {}

    public synchronized void bind(SocketAddress endpoint, int backlog) throws IOException {}

    public synchronized void close() throws IOException {
        if (isClosed()) {
            return;
        }

        for(P2PReliableSocket s:_clientSocks){
            s.close();
        }

        //todo comTool close

        _closed = true;
    }

    public InetAddress getInetAddress() {return null;}

    public int getLocalPort()
    {
        return 0;
    }

    public SocketAddress getLocalSocketAddress()
    {
        return null;
    }

    public boolean isBound()
    {
        return !_closed;
    }

    public boolean isClosed()
    {
        return _closed;
    }

    public void setSoTimeout(int timeout) {}

    public int getSoTimeout()
    {
        return 0;
    }
}
