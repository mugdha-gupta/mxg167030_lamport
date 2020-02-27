import Message.AppendMessage;
import Message.EndMessage;
import Message.StartMessage;
import Message.SuccessMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/*
 * Socket that send append requests and waits for a response for the client
 */
public class ClientSocket implements Runnable{
    FileClient client;
    int serverId;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket socket;
    CountDownLatch latch;

    public ClientSocket(FileClient client, int serverId, int localPort, CountDownLatch latch) throws IOException {
        this.client = client;
        this.serverId = serverId;
        socket = Util.getSocketAsClient(serverId, localPort, Util.CLIENT_LISTENER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        this.latch = latch;
    }

    //send an append request
    void sendMessage(AppendMessage message) throws IOException, ClassNotFoundException {
        out.writeObject(message);
        //wait until you get a message back
        SuccessMessage m = getMessage();
        //print success or fail
        if(m == null)
            System.out.println("client " + client.clientId +  " receives a failure from server " + serverId);
        else
            System.out.println(m.getSuccessMessge());
    }

    //send an end message when the client is done
    void sendMessage(EndMessage message) throws IOException {
        out.writeObject(message);
        //cleanup
        out.close();
        in.close();
        socket.close();
    }

    //monitor to get a reply message to append request
    SuccessMessage getMessage() throws IOException, ClassNotFoundException {
        Object object;
        while (true){
            object = in.readObject();
            if(object == null)
                continue;

            //if not a success message, then return null
            if(object instanceof SuccessMessage)
                return (SuccessMessage) object;
            return null;
        }
    }

    //run when the client first starts up
    //waits for a start message from servers to synchronize start
    @Override
    public void run() {
        Object object;
        while (true){
            try {
                object = in.readObject();
                if(object == null)
                    continue;
                if(object instanceof StartMessage){
                    latch.countDown();
                    return;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
