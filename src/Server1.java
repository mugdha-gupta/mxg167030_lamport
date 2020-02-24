import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server1{
    private static final int SERVER_PORT = 11888;
    int server_id = 1;
    ServerSocket server_listener;
    ServerSocket client_listener;
    Socket socket2;
    Socket socket3;
    List<Socket> clients;
    LamportFile lamportFile;

    Server1() throws Exception {
        initializeServer();
        closeServer();
    }


    public void initializeServer() throws Exception {
        //Server 1 should call accept() twice on a server socket in order to get connected
        //to Server 2 and Server 3

        server_listener = new ServerSocket(SERVER_PORT);
        TimeUnit.SECONDS.sleep(10);

        socket2 = getSocket(server_listener);
        socket3 = getSocket(server_listener);
        HashMap<Integer, ServerConnection> serverConnections = new HashMap<>();
        serverConnections.put(2, new ServerConnection(socket2));
        serverConnections.put(3, new ServerConnection(socket3));

        lamportFile = new LamportFile(1, server_id, serverConnections);

        client_listener = new ServerSocket(FileClient.CLIENT_PORT);
        TimeUnit.SECONDS.sleep(10);

//        clients = new LinkedList<>();
//        for(int i = 0 ; i < 5; i++){
//            clients.add(getSocket(client_listener));
//        }


        ExecutorService pool = Executors.newFixedThreadPool(10);
        pool.execute(new ServerListenerRunnable(server_id, socket2, lamportFile));
        pool.execute(new ServerListenerRunnable(server_id, socket3, lamportFile));

        lamportFile.append("message");
        for (Socket socket:clients
             ) {
//            pool.execute(new ClientRunnable(server_id, socket));
        }
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    Socket getSocket(ServerSocket listener) throws InterruptedException, IOException {

        Socket socket = listener.accept();

        System.out.println("Server 1: connected to " + socket);
        Scanner serverInput = new Scanner(socket.getInputStream());
        PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
        serverOutput.println("Server 1 is running and connected");
        System.out.println("Server 1: " + serverInput.nextLine());

        return socket;
    }

    void closeServer() throws IOException {
        socket2.close();
        socket3.close();
        for (Socket socket: clients
             ) {
            socket.close();
        }
        server_listener.close();
    }

}
