import Message.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServerSocket implements Runnable {
    //can receive any type of message
    int remoteServerId;
    int clientId;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket socket;
    Server localServer;
    CountDownLatch latch;

    private MyServerSocket(Server s, int remotePort) throws IOException {
        localServer = s;
        socket = Util.getSocketAsServer(remotePort);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    private MyServerSocket(Server s, int remoteServerId, int localPort) throws IOException {
        localServer = s;
        socket = Util.getSocketAsClient(remoteServerId, localPort, Util.SERVER_LISTENER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    static MyServerSocket createServerClientSocket(Server s, int clientId) throws IOException {
        MyServerSocket myServerSocket = new MyServerSocket(s, Util.CLIENT_LISTENER_PORT);
        myServerSocket.clientId = clientId;
        return myServerSocket;
    }

    static MyServerSocket createServerListenerSocket(Server s, int remoteServerId) throws IOException {
        MyServerSocket myServerSocket = new MyServerSocket(s, Util.SERVER_LISTENER_PORT);
        myServerSocket.remoteServerId = remoteServerId;
        return myServerSocket;
    }

    static MyServerSocket createServerSocket(Server s, int remoteServerId, int localPort) throws IOException {
        MyServerSocket myServerSocket = new MyServerSocket(s, remoteServerId, localPort);
        myServerSocket.remoteServerId = remoteServerId;
        return myServerSocket;
    }

    synchronized void sendMessage(RequestMessage message) throws IOException {
        out.writeObject(message);
    }

    synchronized void sendMessage(StartMessage message) throws IOException {
        out.writeObject(message);
    }

    synchronized void sendMessage(ServerEndMessage message) throws IOException {
        out.writeObject(message);
    }


    synchronized void sendMessage(AckMessage message) throws IOException {
        out.writeObject(message);
    }

    synchronized void sendMessage(ReplyMessage message) throws IOException {
        out.writeObject(message);
    }

    synchronized void sendMessage(ServerAppendMessage message) throws IOException {
        out.writeObject(message);
    }

    synchronized void sendMessage(SuccessMessage message) throws IOException {
        out.writeObject(message);
    }

    synchronized void sendMessage(ReleaseMessage message) throws IOException {
        out.writeObject(message);
    }

    @Override
    public void run() {

        ExecutorService pool = Executors.newFixedThreadPool(15);
        Object m;
        while(true){
            try {
                m = in.readObject();
                Thread.sleep(1000);
                if(m == null)
                    continue;
                if(m instanceof EndMessage){
                    clean();
                    return;
                }
                if(m instanceof ServerEndMessage){
                    latch.countDown();
                }
                pool.execute(new HandleMessageRunnable(m, localServer));

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                continue;
            }
        }
    }

    void clean() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

}
