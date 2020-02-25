import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocket {
    FileClient client;
    int serverId;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket socket;

    public ClientSocket(FileClient client, int serverId, int localPort) throws IOException {
        this.client = client;
        this.serverId = serverId;
        socket = Util.getSocketAsClient(serverId, localPort, Util.CLIENT_LISTENER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    Message sendMessage(Message message) throws IOException, ClassNotFoundException {
        out.writeObject(message);
        Message m = getMessage();
        switch (m.messageType){
            case Message.ACK:
                System.out.println(Message.successString());
                break;
            default:
                System.out.println(Message.failString());
                break;
        }

        return m;
    }

    Message getMessage() {
        while (true){
            try {
                if (!(in.available() <= 0))
                    continue;
                Message m = (Message) in.readObject();
                if(m == null)
                    continue;
                return m;

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
        }
    }


}