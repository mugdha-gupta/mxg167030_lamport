import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server3 {
    ServerSocket client_listener;
    Socket socket1;
    Socket socket2;
    int server_id = 3;
    List<Socket> clients;
    LamportFile lamportFile;

    Server3() throws Exception {
        initializeServer3();
        closeServer();
    }

    public void initializeServer3() throws Exception{
        System.out.println("Server 3: I have started!");
        socket1 = getSocket(FileServer.serverOneAddress, FileServer.SERVER_PORT+1);
        socket2 = getSocket(FileServer.serverTwoAddress, FileServer.SERVER_PORT+2);
        HashMap<Integer, ServerConnection> serverConnections = new HashMap<>();
        serverConnections.put(1, new ServerConnection(socket1));
        serverConnections.put(2, new ServerConnection(socket2));
        lamportFile = new LamportFile(1, server_id, serverConnections);

        client_listener = new ServerSocket(FileClient.CLIENT_PORT);
        TimeUnit.SECONDS.sleep(10);
//
//        clients = new LinkedList<>();
//        for(int i = 0 ; i < 5; i++){
//            clients.add(getSocket(client_listener));
//        }

        ExecutorService pool = Executors.newFixedThreadPool(10);
        pool.execute(new ServerListenerRunnable(server_id, socket1, lamportFile));
        pool.execute(new ServerListenerRunnable(server_id, socket2, lamportFile));

        lamportFile.append("str");
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.NANOSECONDS);

    }

    Socket getSocket(String serverAddress, int localPort) throws IOException {

        Socket socket = new Socket();
        socket.setReuseAddress(true);
        InetSocketAddress socket1LocalInet = new InetSocketAddress(InetAddress.getLocalHost(), localPort);
        socket.bind(socket1LocalInet);

        InetSocketAddress socket1RemoteInet = new InetSocketAddress(serverAddress, FileServer.SERVER_PORT);
        socket.connect(socket1RemoteInet);

        Scanner server1Input = new Scanner(socket.getInputStream());
        PrintWriter server1Output = new PrintWriter(socket.getOutputStream(), true);
        server1Output.println("Server 3 is running and connected.");
        System.out.println("Server 3: " + server1Input.nextLine());
        return socket;
    }

    Socket getSocket(ServerSocket listener) throws IOException {
        Socket socket = listener.accept();
        System.out.println("Server 3: connected to " + socket);
        Scanner server3Input = new Scanner(socket.getInputStream());
        PrintWriter server3Output = new PrintWriter(socket.getOutputStream(), true);
        server3Output.println("Server 3 is running and connected");
        System.out.println("Server 3: " + server3Input.nextLine());
        return socket;
    }

    void closeServer() throws IOException {
        for (Socket socket: clients
        ) {
            socket.close();
        }
        socket1.close();
        socket2.close();
    }

}
