import Message.AckMessage;
import Message.ServerEndMessage;
import Message.StartMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

/*
 * Server class
 * Called from command line to create a server process
 * needs to be given a number as the server id
 */

public class Server {
    static Server server;
    int serverId;
    HashMap<Integer, MyServerSocket> clients;
    HashMap<Integer, MyServerSocket> servers;
    HashMap<Integer, LamportFile> files;
    HashSet<AckMessage> ackMessages;
    CountDownLatch latch;

    //get server id from command line and instantiate server
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length != 1)
            return;
        int id = Integer.parseInt(args[0]);
        server = new Server(id);
    }

    //constructor sets variables and sets up sockets
    Server(int serverId) throws IOException, InterruptedException {
        System.out.println("server " + serverId + " starts at time: " + System.currentTimeMillis());
        server = this;
        this.serverId = serverId;
        clients = new HashMap<>();
        servers = new HashMap<>();
        files = new HashMap<>();
        ackMessages = new HashSet<>();
        latch = new CountDownLatch(2);
        setUpMaps(serverId);

    }

    //sets up sockets to other servers and clients and creates the lamport files
    private void setUpMaps(int serverId) throws IOException, InterruptedException {
        //create server sockets based on which server this is
        switch (serverId){
            case 1 :
                servers.put(2, MyServerSocket.createServerListenerSocket(server, 2));
                servers.put(3, MyServerSocket.createServerListenerSocket(server, 3));
                break;
            case 2 :
                servers.put(1, MyServerSocket.createServerSocket(server, 1, Util.SERVER_LISTENER_PORT+1));
                servers.put(3, MyServerSocket.createServerListenerSocket(server, 3));
                break;
            case 3 :
                servers.put(1, MyServerSocket.createServerSocket(server, 1, Util.SERVER_LISTENER_PORT+1));
                servers.put(2, MyServerSocket.createServerSocket(server, 2, Util.SERVER_LISTENER_PORT+2));
                break;
        }

        //connect to the clients
        clients.put(1, MyServerSocket.createServerClientSocket(server, 1));
        clients.put(2, MyServerSocket.createServerClientSocket(server, 2));
        clients.put(3, MyServerSocket.createServerClientSocket(server, 3));
        clients.put(4, MyServerSocket.createServerClientSocket(server, 4));
        clients.put(5, MyServerSocket.createServerClientSocket(server, 5));

        //initialize the lamport files
        files.put(1, new LamportFile(1, server));
        files.put(2, new LamportFile(2, server));
        files.put(3, new LamportFile(3, server));
        files.put(4, new LamportFile(4, server));

        //Start up serverSocket in new thread to monitor for any incoming messages
        ExecutorService serverPool = Executors.newFixedThreadPool(3);
        for (MyServerSocket socketRunnable: servers.values()
             ) {
            socketRunnable.latch = latch;
            serverPool.execute(socketRunnable);
        }

        //start up client sockets to listen for append messages
        ExecutorService clientPool = Executors.newFixedThreadPool(5);
        for (MyServerSocket socketRunnable: clients.values()
        ) {
            clientPool.execute(socketRunnable);
        }

        //send the clients start message so they all start when everything is ready
        for (MyServerSocket socketRunnable: clients.values()
        ) {
            socketRunnable.sendMessage(new StartMessage());
        }

        //when all the clients have exited, we can move on
        clientPool.shutdown();
        clientPool.awaitTermination(15, TimeUnit.MINUTES);
        //tell the other servers your clients are done
        for(MyServerSocket socket : servers.values()){
            socket.sendMessage(new ServerEndMessage());
        }

        latch.await();
        System.out.println("server " +  serverId  + " gracefully shutdown");
        exit(0);

    }


    public LamportFile getLamportFile (int fileNum) {
        return files.get(fileNum);
    }
}
