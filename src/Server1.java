import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Server1 {
    private static final int SERVER_PORT = 11888;
    static ServerSocket listener;
    static Socket socket2;
    static Socket socket3;

    public static void initializeServer() throws Exception {
        //Server 1 should call accept() twice on a server socket in order to get connected
        //to Server 2 and Server 3

        listener = new ServerSocket(SERVER_PORT);
        TimeUnit.SECONDS.sleep(10);

        socket2 = getSocket(listener);
        socket3 = getSocket(listener);

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
}
