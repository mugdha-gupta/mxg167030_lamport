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

public class Server {
    static Server server;
    int serverId;
    HashMap<Integer, MyServerSocket> clients;
    HashMap<Integer, MyServerSocket> servers;
    HashMap<Integer, LamportFile> files;
    HashSet<AckMessage> ackMessages;
    CountDownLatch latch;

    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length != 1)
            return;
        int id = Integer.parseInt(args[0]);
        server = new Server(id);
    }

    Server(int serverId) throws IOException, InterruptedException {
        server = this;
        this.serverId = serverId;
        clients = new HashMap<>();
        servers = new HashMap<>();
        files = new HashMap<>();
        ackMessages = new HashSet<>();
        latch = new CountDownLatch(2);
        setUpMaps(serverId);

    }

    private void setUpMaps(int serverId) throws IOException, InterruptedException {
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

        clients.put(1, MyServerSocket.createServerClientSocket(server, 1));
        clients.put(2, MyServerSocket.createServerClientSocket(server, 2));
        clients.put(3, MyServerSocket.createServerClientSocket(server, 3));
        clients.put(4, MyServerSocket.createServerClientSocket(server, 4));
        clients.put(5, MyServerSocket.createServerClientSocket(server, 5));

        files.put(1, new LamportFile(1, server));
        files.put(2, new LamportFile(2, server));
        files.put(3, new LamportFile(3, server));
        files.put(4, new LamportFile(4, server));

        ExecutorService serverPool = Executors.newFixedThreadPool(4);
        for (MyServerSocket socketRunnable: servers.values()
             ) {
            socketRunnable.latch = latch;
            serverPool.execute(socketRunnable);
        }

        ExecutorService clientPool = Executors.newFixedThreadPool(6);
        for (MyServerSocket socketRunnable: clients.values()
        ) {
            clientPool.execute(socketRunnable);
        }

        for (MyServerSocket socketRunnable: clients.values()
        ) {
            socketRunnable.sendMessage(new StartMessage());
        }

        clientPool.awaitTermination(10, TimeUnit.MINUTES);

        for(MyServerSocket socket : servers.values()){
            socket.sendMessage(new ServerEndMessage());
        }

        while(latch.getCount() > 0){
        }

        for(MyServerSocket socket : servers.values()){
            socket.clean();
        }
    }


    public LamportFile getLamportFile (int fileNum) {
        return files.get(fileNum);
    }
}
