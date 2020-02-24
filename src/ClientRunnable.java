import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ClientRunnable implements Runnable {
    int clientNum;
    HashMap<Integer, ServerConnection> servers;

    public ClientRunnable(int clientNum, HashMap<Integer, ServerConnection> servers) {
        this.clientNum = clientNum;
        this.servers = servers;
    }

    @Override
    public void run() {
        for(int i = 0; i < 100 ; i++){
            double waitTime = Math.random();
            try {
                TimeUnit.MILLISECONDS.wait((int) (waitTime*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int messageNum = i+1;
            int serverNum = (int)(Math.random()*3) + 1;
            String message = "client " + clientNum + " message #" + messageNum + " -- server" + serverNum;
//            int fileNum = (int)(Math.random()*3) + 1;
            int fileNum = 1;

            try {
                servers.get(serverNum).sendMessage(new ClientServerMessage(clientNum, serverNum, message, i+1, fileNum));
                ClientServerMessage receiveMessage = servers.get(serverNum).getMessage();
                if(receiveMessage == null)
                    continue;
                String type = (receiveMessage.messageType == ClientServerMessage.ACK)? " a successful ack " : " a failure ";
                System.out.println("client " + clientNum + " receives "  + type + "from server " +  serverNum);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
