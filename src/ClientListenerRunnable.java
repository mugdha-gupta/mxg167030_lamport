import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ClientListenerRunnable implements Runnable{
    int server_id;
    Socket socket;
    ObjectInputStream in;
    LamportFile lamportFile;

    ClientListenerRunnable(int server_id, Socket s, LamportFile lamportFile){
        this.server_id = server_id;
        socket = s;
        this.lamportFile = lamportFile;
    }

    @Override
    public void run(){
        try {
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ClientServerMessage message;

            while(true){
                message = (ClientServerMessage) in.readObject();
                if(message == null)
                    continue;
                if(message.messageType == ClientServerMessage.END_TYPE){
                    in.close();
                    break;
                }
                else{
                    lamportFile.append(message.message, message.clientId);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }


    }
}
