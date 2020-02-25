import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Util {
    static String[] serverAdresses = {"dc01.utdallas.edu", "dc02.utdallas.edu", "dc03.utdallas.edu"};

     static final int SERVER_LISTENER_PORT = 11888;
     static final int CLIENT_LISTENER_PORT = 11999;

    static Socket getSocketAsClient(int serverId, int localPort, int remotePort) throws IOException {
        Socket socket = new Socket();
        socket.setReuseAddress(true);
        InetSocketAddress localInsa = new InetSocketAddress(InetAddress.getLocalHost(), localPort);
        socket.bind(localInsa);

        InetSocketAddress remoteInsa = new InetSocketAddress(serverAdresses[serverId+1], remotePort);
        socket.connect(remoteInsa);
        return socket;
    }

    static Socket getSocketAsServer(int remotePort) throws IOException {
        ServerSocket listener = new ServerSocket(remotePort);
        Socket socket = listener.accept();
        return socket;
    }

}
