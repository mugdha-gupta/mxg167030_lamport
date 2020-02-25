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

    Message sendMessage(Message message) throws IOException{
        out.writeObject(message);
        System.out.println(message.logString() + " has left CLientSocket");
        Message m = getMessage();
        System.out.println(" CLIENT: i got the messgae");
        switch (m.messageType){
            case Message.ACK:
                System.out.println(Message.successMessage);
                break;
            default:
                System.out.println(Message.failMessage);
                break;
        }

        return m;
    }

    Message getMessage() {
        while (true){Message m;
            try {
                if (!(in.available() <= 0))
                    continue;
                m = (Message) in.readObject();
                if(m == null)
                    continue;
                return m;

            } catch (IOException | ClassNotFoundException e) {
                continue;
            }
        }
    }


}
