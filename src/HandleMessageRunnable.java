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
//                System.out.println(message.logString() + " is an append, in the runnable");

                System.out.println("1. Server: " + server.serverId + " received an append message");
                try {
                    file.requestResourceEvent(message);
                    System.out.println("\t1a. append message has been sent to lamp file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Message.ACK:
//                System.out.println(message.logString() + " is an ack, in the runnable");

                if(server.ackMessages.contains(message.fileNum)){
//                    System.out.println(message.logString() + " 2nd ack, success");
                    message.success = true;
                }
                else
                    server.ackMessages.add(message.fileNum);
                if(message.success){
                    server.ackMessages.remove(message.fileNum);

                    try {
                        server.clients.get(message.clientId).sendMessage(message);
//                        System.out.println(" sent message to client");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
//                System.out.println(message.logString() + " is an "+ message.messageType +", in the runnable");
                try {
                    file.receiveMessageEvent(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Message.SERVER_APPEND:
//                System.out.println(message.logString() + " is an SERVER APPEND, in the runnable");

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
