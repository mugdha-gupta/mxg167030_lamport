
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Util {
    static String[] serverAdresses = {"dc01.utdallas.edu", "dc02.utdallas.edu", "dc03.utdallas.edu"};

     static final int SERVER_LISTENER_PORT = 13000;
     static final int CLIENT_LISTENER_PORT = 12345;

     //returns a socket for the client
    static Socket getSocketAsClient(int serverId, int localPort, int remotePort) throws IOException {
        Socket socket = new Socket();
        socket.setReuseAddress(true);
        InetSocketAddress localInsa = new InetSocketAddress(InetAddress.getLocalHost(), localPort);
        socket.bind(localInsa);

        InetSocketAddress remoteInsa = new InetSocketAddress(serverAdresses[serverId-1], remotePort);
        socket.connect(remoteInsa);
        return socket;
    }

    //returns a socket for the server
    static Socket getSocketAsServer(int remotePort) throws IOException {
        ServerSocket listener = new ServerSocket(remotePort);
        Socket socket = listener.accept();
        listener.close();
        return socket;
    }

}
