import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerListenerRunnable implements Runnable{
    int server_id;
    Socket socket;
    ObjectInputStream in;
    LamportFile lamportFile;

    ServerListenerRunnable(int server_id, Socket s, LamportFile lamportFile){
        this.server_id = server_id;
        socket = s;
        this.lamportFile = lamportFile;
    }

    @Override
    public void run(){
        try {
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ServerMessage message;

            while(true){
                message = (ServerMessage) in.readObject();
                if(message == null)
                    continue;
                if(message.messageType == ServerMessage.END_TYPE){
                    in.close();
                    break;
                }
                ServerMessage finalMessage = message;
                        synchronized (lamportFile){
                            try {
                                lamportFile.receiveEvent(finalMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }


    }
}
