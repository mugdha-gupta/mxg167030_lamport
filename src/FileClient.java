import Message.AppendMessage;
import Message.EndMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        client = this;
        clientId = id;
        latch = new CountDownLatch(3);
        setUpSocketsMap();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (ClientSocket socket: clientSockets.values()) {
            pool.execute(socket);
        }

        while (latch.getCount() > 0){
        }
        startMessageGenerationLoop();
    }

    private void startMessageGenerationLoop() throws InterruptedException, IOException, ClassNotFoundException {

        for (int i = 0; i < 5; i++) {
            double waitTime = Math.random();
            Thread.sleep((int) waitTime * 1000);

            int messageNum = i + 1;
            int serverNum = (int) (Math.random() * 3) + 1;
            int fileNum = (int) (Math.random() *4) + 1;

            String messageString = "client " + clientId + " message #" + messageNum + "for file "+ fileNum + "-- server" + serverNum + "\n";
            System.out.println(messageString);
            AppendMessage message = new AppendMessage(clientId, fileNum, messageString);
            clientSockets.get(serverNum).sendMessage(message);
        }

        EndMessage endMessage = new EndMessage();
        for(ClientSocket socket : clientSockets.values()){
            socket.sendMessage(endMessage);
        }

    }

    void setUpSocketsMap() throws IOException {
        clientSockets = new HashMap<>();
        clientSockets.put(1, new ClientSocket(client, 1, Util.CLIENT_LISTENER_PORT+1, latch));
        clientSockets.put(2, new ClientSocket(client, 2, Util.CLIENT_LISTENER_PORT+2, latch));
        clientSockets.put(3, new ClientSocket(client, 3, Util.CLIENT_LISTENER_PORT+3, latch));
    }
}
