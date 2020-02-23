import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Server2 {
    static int server_id = 2;
    static ServerSocket listener;
    static ServerSocket client_listener;
    static Socket socket1;
    static Socket socket3;
    static int currentTime;

    static List<Socket> clients;
    public static void initializeServer2() throws Exception{
        System.out.println("Server 2: I have started!");
        socket1 = getSocket(FileServer.serverOneAddress, FileServer.SERVER_PORT+1);

        listener = new ServerSocket(FileServer.SERVER_PORT);
        TimeUnit.SECONDS.sleep(10);
        socket3 = getSocket(listener);

        client_listener = new ServerSocket(FileClient.CLIENT_PORT);
        TimeUnit.SECONDS.sleep(10);

        clients = new LinkedList<>();
        for(int i = 0 ; i < 5; i++){
            clients.add(getSocket(client_listener));
        }
        currentTime = 0;

    }

    static Socket getSocket(String serverAddress, int localPort) throws IOException {

        Socket socket = new Socket();
        socket.setReuseAddress(true);
        InetSocketAddress socket1LocalInet = new InetSocketAddress(InetAddress.getLocalHost(), localPort);
        socket.bind(socket1LocalInet);

        InetSocketAddress socket1RemoteInet = new InetSocketAddress(serverAddress, FileServer.SERVER_PORT);
        socket.connect(socket1RemoteInet);

        Scanner server1Input = new Scanner(socket.getInputStream());
        PrintWriter server1Output = new PrintWriter(socket.getOutputStream(), true);
        server1Output.println("Server 2 is running and connected.");
        System.out.println("Server 2: " + server1Input.nextLine());
        return socket;
    }

    static Socket getSocket(ServerSocket listener) throws IOException {
        Socket socket = listener.accept();
        System.out.println("Server 2: connected to " + socket);
        Scanner server3Input = new Scanner(socket.getInputStream());
        PrintWriter server3Output = new PrintWriter(socket.getOutputStream(), true);
        server3Output.println("Server 2 is running and connected");
        System.out.println("Server 2: " + server3Input.nextLine());
        return socket;
    }

    static void closeServer() throws IOException {
        socket1.close();
        socket3.close();
        for (Socket socket: clients
        ) {
            socket.close();
        }
        listener.close();
    }
}
