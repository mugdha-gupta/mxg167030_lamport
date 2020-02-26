import Message.AppendMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileClient {
    static FileClient client;
    int clientId;
    HashMap<Integer, ClientSocket> clientSockets;

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        if(args.length != 1)
            return;
        int id = Integer.parseInt(args[0]);
        client = new FileClient(id);

    }

    public FileClient(int id) throws IOException, InterruptedException, ClassNotFoundException {
        client = this;
        clientId = id;
        setUpSocketsMap();
        startClient();
    }

    private void startClient() throws InterruptedException, IOException, ClassNotFoundException {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (ClientSocket socket: clientSockets.values()
             ) {
            pool.execute(new ClientStartRunnable(client, socket));
        }
        pool.awaitTermination(10, TimeUnit.SECONDS );
        startMessageGenerationLoop();
    }

    private void startMessageGenerationLoop() throws InterruptedException, IOException, ClassNotFoundException {
        System.out.println("starting message generation");
        for (int i = 0; i < 1; i++) {
            double waitTime = Math.random();
            Thread.sleep((int) waitTime * 1000);

            int messageNum = i + 1;
            int serverNum = (int) (Math.random() * 3) + 1;
            int fileNum = (int) (Math.random() *4) + 1;

            String messageString = "client " + clientId + " message #" + messageNum + " -- server" + serverNum + "\n";;
            AppendMessage message = new AppendMessage(clientId, fileNum, messageString);
            clientSockets.get(serverNum).sendMessage(message);
        }
    }

    void setUpSocketsMap() throws IOException {
        clientSockets = new HashMap<>();
        clientSockets.put(1, new ClientSocket(client, 1, Util.CLIENT_LISTENER_PORT+1));
        clientSockets.put(2, new ClientSocket(client, 2, Util.CLIENT_LISTENER_PORT+2));
        clientSockets.put(3, new ClientSocket(client, 3, Util.CLIENT_LISTENER_PORT+3));
    }
}
