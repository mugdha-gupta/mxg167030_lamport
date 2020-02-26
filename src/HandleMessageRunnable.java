import Message.AppendMessage;
import Message.RequestMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.AcceptPendingException;


public class HandleMessageRunnable implements Runnable {
    Server server;
    Object message;

    public HandleMessageRunnable(Object m, Server s) {
        server = s;
        message = m;
    }

    @Override
    public void run() {
        LamportFile file;

        if(message instanceof AppendMessage){
            AppendMessage mess = (AppendMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            System.out.println("1. Server: " + server.serverId + " received an append message");
            try {
                file.requestResourceEvent(mess;
                System.out.println("\t1a. append message has been sent to lamp file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(message instanceof RequestMessage){

        }





        switch (message.messageType){

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
                System.out.println("4. Request message arrived at server " + server.serverId);
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
