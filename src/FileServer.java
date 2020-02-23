import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Reference for server client relationship: https://cs.lmu.edu/~ray/notes/javanetexamples/#capitalize
 */
public class FileServer {
    static int SERVER_PORT = 11888;

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
                Server1.initializeServer();
                Server1.closeServer();
                break;
            case 2:
                Server2.initializeServer2();
                Server2.closeServer();
                break;
            case 3:
                Server3.initializeServer3();
                Server3.closeServer();
                break;
            default:
                System.err.println("Server number is invalid");
        }

//        try (ServerSocket listener = new ServerSocket()) {
//            System.out.println("The capitalization server is running...");
//            ExecutorService pool = Executors.newFixedThreadPool(NUM_CLIENTS);
//            while (true) {
//                pool.execute(new FileAppenderThread(listener.accept()));
//            }
//        }
    }



    private static void runServer3() throws Exception{
//        out = new ObjectOutputStream(socket.getOutputStream());
//        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));


    }

}
