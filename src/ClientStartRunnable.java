import Message.StartMessage;

import java.io.IOException;

public class ClientStartRunnable implements Runnable {
    FileClient client;
    ClientSocket socket;

    ClientStartRunnable(FileClient client, ClientSocket socket){
        this.client = client;
        this.socket = socket;
    }
    @Override
    public void run() {
        while (true){
            try {
                if (socket.in.available() <= 0)
                    continue;
                Object sm = socket.in.readObject();
                if(sm == null)
                    continue;
                if(sm instanceof StartMessage)
                    return;
            }catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
        }
    }
}
