import Message.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * MyServerSocket class
 * Runnable that holds all information required to send messages and receive from the server
 * after it receives a message, it creates a new thread to handle it
 */
public class MyServerSocket implements Runnable {
    //can receive any type of message (from server or client)
    int remoteServerId;
    int clientId;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket socket;
    Server localServer;
    CountDownLatch latch;

    //private constructors so other classes need to create in many different ways for different uses
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

    //this is a socket that listens for client connections
    static MyServerSocket createServerClientSocket(Server s, int clientId) throws IOException {
        MyServerSocket myServerSocket = new MyServerSocket(s, Util.CLIENT_LISTENER_PORT);
        myServerSocket.clientId = clientId;
        return myServerSocket;
    }

    //this listens for other server connections
    static MyServerSocket createServerListenerSocket(Server s, int remoteServerId) throws IOException {
        MyServerSocket myServerSocket = new MyServerSocket(s, Util.SERVER_LISTENER_PORT);
        myServerSocket.remoteServerId = remoteServerId;
        return myServerSocket;
    }

    //this allows servers to connect to other servers
    static MyServerSocket createServerSocket(Server s, int remoteServerId, int localPort) throws IOException {
        MyServerSocket myServerSocket = new MyServerSocket(s, remoteServerId, localPort);
        myServerSocket.remoteServerId = remoteServerId;
        return myServerSocket;
    }

    //many different types of messages may be sent
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

    //we will listen for incoming messages when this runnable is executed
    @Override
    public void run() {

        //we don't want to create too many threads so restict the thread pool for message handling
        ExecutorService pool = Executors.newFixedThreadPool(10);
        Object m;
        while(true){
            try {
                m = in.readObject();
                if(m == null)
                    continue;
                //if we get an end message from the client, we can close this socket
                if(m instanceof EndMessage){
                    clean();
                    return;
                }
                //if we get a server end message, we can countdown the latch to allow the server
                //thread to proceed
                if(m instanceof ServerEndMessage){
                    latch.countDown();
                }
                //otherwise handle the message in another thread
                pool.execute(new HandleMessageRunnable(m, localServer));

            } catch (IOException | ClassNotFoundException e) {
                continue;
            }
        }
    }

    //close streams
    void clean() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

}
