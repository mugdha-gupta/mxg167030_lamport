import Message.AckMessage;
import Message.StartMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static Server server;
    int serverId;
    HashMap<Integer, MyServerSocket> clients;
    HashMap<Integer, MyServerSocket> servers;
    HashMap<Integer, LamportFile> files;
    HashSet<AckMessage> ackMessages;

    public static void main(String[] args) throws IOException {
        if(args.length != 1)
            return;
        int id = Integer.parseInt(args[0]);
        server = new Server(id);
    }

    Server(int serverId) throws IOException {
        server = this;
        this.serverId = serverId;
        clients = new HashMap<>();
        servers = new HashMap<>();
        files = new HashMap<>();
        ackMessages = new HashSet<>();
        setUpMaps(serverId);

    }

    private void setUpMaps(int serverId) throws IOException {
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
//        clients.put(4, MyServerSocket.createServerClientSocket(server, 4));
//        clients.put(5, MyServerSocket.createServerClientSocket(server, 5));

        files.put(1, new LamportFile(1, server));
        files.put(2, new LamportFile(2, server));
        files.put(3, new LamportFile(3, server));
        files.put(4, new LamportFile(4, server));

        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (MyServerSocket socketRunnable: servers.values()
             ) {
            pool.execute(socketRunnable);
        }
        for (MyServerSocket socketRunnable: clients.values()
        ) {
            pool.execute(socketRunnable);
        }
        for (MyServerSocket socketRunnable: clients.values()
        ) {
            socketRunnable.sendMessage(new StartMessage());
            System.out.println("sent start");
        }

    }


    public LamportFile getLamportFile (int fileNum) {
        return files.get(fileNum);
    }
}
