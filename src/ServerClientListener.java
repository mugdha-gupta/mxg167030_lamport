import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ServerClientListener implements Runnable {
    private final int clientId;
    Socket socket;

    ServerClientListener(int clientId, Socket socket) {
        this.socket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
    try{
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        ClientServerMessage message;

        while(true){
            message = (ClientServerMessage) in.readObject();
            if(message == null)
                continue;
            if(message.messageType == ClientServerMessage.ACK){
                System.out.println("Client " + clientId + "receives a successful message from server " + message.serverId);
            }
        }

    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
        return;
    }

}
}
