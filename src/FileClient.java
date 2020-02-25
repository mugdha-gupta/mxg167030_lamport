import java.io.IOException;
import java.util.HashMap;

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
        startMessageGenerationLoop();

    }

    private void startMessageGenerationLoop() throws InterruptedException, IOException, ClassNotFoundException {
        for (int i = 0; i < 100; i++) {
            double waitTime = Math.random();
            Thread.sleep((int) waitTime * 1000);

            int messageNum = i + 1;
            int serverNum = (int) (Math.random() * 3) + 1;
            int fileNum = (int) (Math.random() *4) + 1;

            Message message = new Message(Message.APPEND, fileNum, clientId, serverNum, messageNum);
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
