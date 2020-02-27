import Message.AppendMessage;
import Message.EndMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

/*
 * FileClient
 * allows user to use command line to specify
 * a new client with a client id given on the command line
 */
public class FileClient {
    static FileClient client;
    int clientId;
    HashMap<Integer, ClientSocket> clientSockets;
    CountDownLatch latch;

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        if(args.length != 1)
            return;
        int id = Integer.parseInt(args[0]);
        client = new FileClient(id);

    }

    public FileClient(int id) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("client " + id + " starts at time: " + System.currentTimeMillis());

        client = this;
        clientId = id;
        latch = new CountDownLatch(3);

        //set up the sockets
        setUpSocketsMap();
        //start all the sockets using fixed thread pool
        //there should only be three because 3 servers
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (ClientSocket socket: clientSockets.values()) {
            pool.execute(socket);
        }

        //wait until we have received a start message from the servers
        while (latch.getCount() > 0){
        }
        //then start generating messages
        startMessageGenerationLoop();
    }

    private void startMessageGenerationLoop() throws InterruptedException, IOException, ClassNotFoundException {

        //100 messages
        for (int i = 0; i < 100; i++) {
            //wait between 0 and 1 second
            double waitTime = Math.random();
            Thread.sleep((int) waitTime * 1000);

            //choose random server/file
            int messageNum = i + 1;
            int serverNum = (int) (Math.random() * 3) + 1;
            int fileNum = (int) (Math.random() *4) + 1;

            String messageString = "client " + clientId + " message #" + messageNum + " -- server " + serverNum;
            AppendMessage message = new AppendMessage(clientId, fileNum, messageString);
            System.out.println("client " + clientId + " requests: \"" + messageString + "\" for file #" +
                    fileNum + " at time: " + System.currentTimeMillis());
            //send append message
            clientSockets.get(serverNum).sendMessage(message);
        }

        //after all the clients are done, send a done message to all the servers
        EndMessage endMessage = new EndMessage();
        for(ClientSocket socket : clientSockets.values()){
            socket.sendMessage(endMessage);
        }
        System.out.println("*******");
        exit(0);

    }

    //sockets to all the servers
    void setUpSocketsMap() throws IOException {
        clientSockets = new HashMap<>();
        clientSockets.put(1, new ClientSocket(client, 1, Util.CLIENT_LISTENER_PORT+1, latch));
        clientSockets.put(2, new ClientSocket(client, 2, Util.CLIENT_LISTENER_PORT+2, latch));
        clientSockets.put(3, new ClientSocket(client, 3, Util.CLIENT_LISTENER_PORT+3, latch));
    }
}
