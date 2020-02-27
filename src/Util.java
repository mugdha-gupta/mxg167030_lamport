
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * Util class
 * This class holds commonly used information and methods
 */
public class Util {
    static String[] serverAdresses = {"dc32.utdallas.edu", "dc33.utdallas.edu", "dc34.utdallas.edu"};

    //Ports that the Server listens on to connect to other servers and clients
     static final int SERVER_LISTENER_PORT = 13000;
     static final int CLIENT_LISTENER_PORT = 12345;

     //returns a socket to a client connection
    static Socket getSocketAsClient(int serverId, int localPort, int remotePort) throws IOException {
        Socket socket = new Socket();
        socket.setReuseAddress(true);
        InetSocketAddress localInsa = new InetSocketAddress(InetAddress.getLocalHost(), localPort);
        socket.bind(localInsa);

        InetSocketAddress remoteInsa = new InetSocketAddress(serverAdresses[serverId-1], remotePort);
        socket.connect(remoteInsa);
        return socket;
    }

    //returns a socket to a server
    static Socket getSocketAsServer(int remotePort) throws IOException {
        ServerSocket listener = new ServerSocket(remotePort);
        Socket socket = listener.accept();
        listener.close();
        return socket;
    }

}
