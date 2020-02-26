import Message.AppendMessage;
import Message.SuccessMessage;

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

    void sendMessage(AppendMessage message) throws IOException{
        out.writeObject(message);
        SuccessMessage m = getMessage();
        if(m == null)
            System.out.println("client " + client.clientId +  " receives a failure from server " + serverId);
        else
            System.out.println(m.getSuccessMessge());
    }

    SuccessMessage getMessage() {
        while (true){
            Object object;
            try {
                if (!(in.available() <= 0))
                    continue;
                object = in.readObject();
                if(object == null)
                    continue;
                if(object instanceof SuccessMessage)
                    return (SuccessMessage) object;
                return null;
            } catch (IOException | ClassNotFoundException e) {
                continue;
            }
        }
    }


}
