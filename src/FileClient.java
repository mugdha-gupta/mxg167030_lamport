import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileClient {
    static Socket server1;
    static Socket server2;
    static Socket server3;
    final static int CLIENT_PORT = 11999;
    private static int clientNum;

    public static void main(String[] args) throws IOException, InterruptedException {
        server1 = getSocket(FileServer.serverOneAddress, CLIENT_PORT+1);
        server2 = getSocket(FileServer.serverTwoAddress, CLIENT_PORT+2);
        server3 = getSocket(FileServer.serverThreeAddress, CLIENT_PORT+3);
        HashMap<Integer, ServerConnection> servers = new HashMap<>();
        servers.put(1, new ServerConnection(server1));
        servers.put(2, new ServerConnection(server2));
        servers.put(3, new ServerConnection(server3));


        ExecutorService pool = Executors.newFixedThreadPool(10);

        clientNum = Integer.parseInt(args[0]);

        pool.execute(new ClientRunnable(clientNum, servers));


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
