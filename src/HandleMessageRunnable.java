import java.io.IOException;

import static Message.*;

public class HandleMessageRunnable implements Runnable {
    Server server;
    Message message;

    public HandleMessageRunnable(Message m, Server s) {
        server = s;
        message = m;
    }

    @Override
    public void run() {
        LamportFile file = server.getLamportFile(message.fileNum);

        switch (message.messageType){
            //implement all switch statemests
            case APPEND:
                //can only be coming from the client to the server
                //need to send to the correct lamportFile
                try {
                    file.requestResourceEvent(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ACK:
                if(server.ackMessages.contains(message.fileNum)){
                    message.success = true;
                }
                else
                    server.ackMessages.add(message.fileNum);
                if(message.success){
                    try {
                        server.clients.get(message.clientId).sendMessage(
                                new Message(ACK, message.fileNum, message.clientId, message.serverId, message.messageNum));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.ackMessages.remove(message.fileNum);
                    try {
                        file.releaseResourceEvent(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case RELEASE:
            case REQUEST:
            case REPLY:
                //must be coming from another server
                //need to send to a lamport
                try {
                    file.receiveMessageEvent(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SERVER_APPEND:
                try {
                    file.writeToFile(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:

                break;

        }
    }
}
