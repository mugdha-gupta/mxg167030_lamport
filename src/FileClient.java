import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class FileClient {
    static Socket server1;
    static Socket server2;
    static Socket server3;
    final static int CLIENT_PORT = 11999;

    public static void main(String[] args) throws IOException {
        server1 = getSocket(FileServer.serverOneAddress, CLIENT_PORT+1);
        server2 = getSocket(FileServer.serverTwoAddress, CLIENT_PORT+2);
        server3 = getSocket(FileServer.serverThreeAddress, CLIENT_PORT+3);
    }

    static Socket getSocket(String serverAddress, int localPort) throws IOException {

        Socket socket = new Socket();
        socket.setReuseAddress(true);
        InetSocketAddress socket1LocalInet = new InetSocketAddress(InetAddress.getLocalHost(), localPort);
        socket.bind(socket1LocalInet);

        InetSocketAddress socket1RemoteInet = new InetSocketAddress(serverAddress, CLIENT_PORT);
        socket.connect(socket1RemoteInet);

        Scanner server1Input = new Scanner(socket.getInputStream());
        PrintWriter server1Output = new PrintWriter(socket.getOutputStream(), true);
        server1Output.println("Client is running and connected.");
        System.out.println("Client: " + server1Input.nextLine());
        return socket;
    }
}
