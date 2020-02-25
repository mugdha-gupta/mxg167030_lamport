import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        return myServerSocket;
    }

    synchronized void sendMessage(Message message) throws IOException {
        out.writeObject(message);
    }

    @Override
    public void run() {

        ExecutorService pool = Executors.newFixedThreadPool(20);
        Message m;
        while(true){
            try {
                if (!(in.available() <= 0))
                    continue;
                m = (Message) in.readObject();
                if(m == null)
                    continue;
                pool.execute(new HandleMessageRunnable(m, localServer));

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

}
