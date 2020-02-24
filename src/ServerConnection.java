import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    Socket socket;
    ObjectOutputStream out;

    ServerConnection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    void sendMessage(ServerMessage messsage) throws IOException {
        out.writeObject(messsage);
    }
    void sendMessage(ClientServerMessage messsage) throws IOException {
        out.writeObject(messsage);
    }
}
