import java.io.IOException;


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
            case Message.APPEND:
                //can only be coming from the client to the server
                //need to send to the correct lamportFile
                System.out.println("append message arrived!");
                try {
                    file.requestResourceEvent(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Message.ACK:
                if(server.ackMessages.contains(message.fileNum)){
                    message.success = true;
                }
                else
                    server.ackMessages.add(message.fileNum);
                if(message.success){
                    try {
                        server.clients.get(message.clientId).sendMessage(
                                new Message(Message.ACK, message.fileNum, message.clientId, message.serverId, message.messageNum));
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
            case Message.RELEASE:
            case Message.REQUEST:
            case Message.REPLY:
                //must be coming from another server
                //need to send to a lamport
                try {
                    file.receiveMessageEvent(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Message.SERVER_APPEND:
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
