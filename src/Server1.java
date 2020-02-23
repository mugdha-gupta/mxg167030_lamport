import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server1 {
    private static final int SERVER_PORT = 11888;
    static int serve_id = 1;
    static ServerSocket server_listener;
    static ServerSocket client_listener;
    static Socket socket2;
    static Socket socket3;
    static int currentTime;

    static List<Socket> clients;

    public static void initializeServer() throws Exception {
        //Server 1 should call accept() twice on a server socket in order to get connected
        //to Server 2 and Server 3

        server_listener = new ServerSocket(SERVER_PORT);
        TimeUnit.SECONDS.sleep(10);

        socket2 = getSocket(server_listener);
        socket3 = getSocket(server_listener);

        client_listener = new ServerSocket(FileClient.CLIENT_PORT);
        clients = new LinkedList<>();
        for(int i = 0 ; i < 5; i++){
            clients.add(getSocket(client_listener));
        }


        currentTime = 0;

    }

    static Socket getSocket(ServerSocket listener) throws InterruptedException, IOException {

        Socket socket = listener.accept();

        System.out.println("Server 1: connected to " + socket);
        Scanner serverInput = new Scanner(socket.getInputStream());
        PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
        serverOutput.println("Server 1 is running and connected");
        System.out.println("Server 1: " + serverInput.nextLine());

        return socket;
    }

    static void closeServer() throws IOException {
        socket2.close();
        socket3.close();
        for (Socket socket: clients
             ) {
            socket.close();
        }
        server_listener.close();
    }
}
