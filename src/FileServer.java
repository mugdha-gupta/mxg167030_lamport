import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Reference for server client relationship: https://cs.lmu.edu/~ray/notes/javanetexamples/#capitalize
 */
public class FileServer {
    static int NUM_CLIENTS = 5;
    static int SERVER_PORT = 11888;
    static int CLIENT_PORT = 11999;

    static String serverOneAddress = "dc01.utdallas.edu";
    static String serverTwoAddress = "dc02.utdallas.edu";
    static String serverThreeAddress = "dc03.utdallas.edu";

    public static void main(String[] args) throws Exception {

        if(args.length != 1){
            System.err.println("Please pass which server number this should be");
            return;
        }

        int serverNum = Integer.parseInt(args[0]);

        switch (serverNum){
            case 1:
                runServer1();
                break;
            case 2:
                runServer2();
                break;
            case 3:
                runServer3();
                break;
            default:
                System.err.println("Server number is invalid");
        }

        try (ServerSocket listener = new ServerSocket()) {
            System.out.println("The capitalization server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(NUM_CLIENTS);
            while (true) {
                pool.execute(new FileAppenderThread(listener.accept()));
            }
        }
    }

    private static void runServer1() throws Exception {
        //Server 1 should call accept() twice on a server socket in order to get connected
        //to Server 2 and Server 3
        ServerSocket listener = new ServerSocket(SERVER_PORT);

        Socket socket2 = listener.accept();
        System.out.println("Server 1: connected to " + socket2);

        Socket socket3 = listener.accept();
        System.out.println("Server 1: connected to " + socket3);

        Scanner server2Input = new Scanner(socket2.getInputStream());
        Scanner server3Input = new Scanner(socket3.getInputStream());

        PrintWriter server2Output = new PrintWriter(socket2.getOutputStream(), true);
        PrintWriter server3Output = new PrintWriter(socket3.getOutputStream(), true);

        server2Output.println("Server 1 is running and connected");
        server3Output.println("Server 1 is running and connected");

        System.out.println("Server 1: " + server2Input.nextLine());
        socket2.close();

        System.out.println("Server 1: " + server3Input.nextLine());
        socket3.close();
    }

    private static void runServer2() throws Exception {
        //Server 2 should call accept() once on a server socket in order to get connected
        //to Server 3
        //Server 2 should also initiate a connection with server one on a Socket
        System.out.println("Server 2: I have started!");

        Socket socket1 = new Socket(serverOneAddress, SERVER_PORT);
        Scanner server1Input = new Scanner(socket1.getInputStream());
        PrintWriter server1Output = new PrintWriter(socket1.getOutputStream(), true);
        server1Output.println("Server 2 is running and connected.");
        System.out.println("Server 2: " + server1Input.nextLine());

        ServerSocket listener = new ServerSocket(SERVER_PORT);
        Socket socket3 = listener.accept();
        System.out.println("Server 2: connected to " + socket3);
        Scanner server3Input = new Scanner(socket3.getInputStream());
        PrintWriter server3Output = new PrintWriter(socket3.getOutputStream(), true);
        server3Output.println("Server 2 is running and connected");
        System.out.println("Server 2: " + server3Input.nextLine());
        socket3.close();


    }

    private static void runServer3() throws Exception{
        //Server 3 should initiate a connection with server one and two on a Socket
        System.out.println("Server 3: I have started!");

        Socket socket1 = new Socket(serverOneAddress, SERVER_PORT);
        Socket socket2 = new Socket(serverTwoAddress, SERVER_PORT);

        Scanner server1Input = new Scanner(socket1.getInputStream());
        PrintWriter server1Output = new PrintWriter(socket1.getOutputStream(), true);

        Scanner server2Input = new Scanner(socket2.getInputStream());
        PrintWriter server2Output = new PrintWriter(socket2.getOutputStream(), true);

        server1Output.println("Server 3 is running and connected.");
        server2Output.println("Server 3 is running and connected.");

        System.out.println("Server 3: " + server1Input.nextLine());
        System.out.println("Server 3: " + server2Input.nextLine());

    }

}
