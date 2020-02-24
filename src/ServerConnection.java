import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    ServerConnection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    void sendMessage(ServerMessage messsage) throws IOException {
        out.writeObject(messsage);
    }
    void sendMessage(ClientServerMessage messsage) throws IOException {
        out.writeObject(messsage);
    }

    ClientServerMessage getMessage() throws IOException, ClassNotFoundException {
        ClientServerMessage message = (ClientServerMessage) in.readObject();
        if(message == null)
            return null;
        return message;
    }
}
