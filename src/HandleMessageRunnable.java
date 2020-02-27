import Message.*;

import java.io.IOException;

/*
 * HandleMessageRunnable
 * called by Server.java in order to handle processing a received message
 */
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

        if(message == null)
            return;

        //if its an append message
        if(message instanceof AppendMessage){
            AppendMessage mess = (AppendMessage) message;
            System.out.println("server " + server.serverId + " receives: \"" + mess.getMessage() + "\" for file #" +
                    mess.getFileNum() + " at time: " + System.currentTimeMillis());
            System.out.println("server " + server.serverId + " attempting to acquire resource");
            file = server.getLamportFile(mess.getFileNum());
            //tell the lamport file to request the resource
            try {
                file.requestResourceEvent(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //if its a request message
        else if(message instanceof RequestMessage){
            RequestMessage mess = (RequestMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            //send to the lamprot file and let them handle it
            try {
                file.receiveRequestMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //forward lamport the release message
        else if(message instanceof ReleaseMessage){
            ReleaseMessage mess = (ReleaseMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.receiveReleaseMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //forward the lamport file the reply message
        else if(message instanceof ReplyMessage){
            ReplyMessage mess = (ReplyMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.receiveReplyMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //its an acknowledge message
        else if(message instanceof AckMessage) {
            AckMessage mess = (AckMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            boolean success = false;
            AckMessage messToRem = new AckMessage(0, 0);
            //if we have seen one ack message for the same request server, client, and file num
            //then this is the second ack message
            //then we are done
            for (AckMessage ack : server.ackMessages) {
                if (ack.getClientId() == mess.getClientId() && ack.getFileNum() == mess.getFileNum()) {
                    //match found!
                    success = true;
                    messToRem = ack;
                    break;
                }
            }
            //we are done with this append message
            if (success) {
                server.ackMessages.remove(messToRem);
                SuccessMessage successMessage = new SuccessMessage("client " + mess.getClientId() +  " receives a successful ack from server " + server.serverId);
                try {
                    //send the client the acknowledge message to indicate success
                    server.clients.get(mess.getClientId()).sendMessage(successMessage);
                    //tell the lamport file to release the resource
                    file.releaseResourceEvent();
                    System.out.println("server " + server.serverId + " sends successful ack to to client " + mess.getClientId());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            //otherwise record the first ack message received for this resource
            else {
                server.ackMessages.add(mess);
            }
            return;
        }
        //if its a server append message
        else if(message instanceof ServerAppendMessage) {
            ServerAppendMessage mess = (ServerAppendMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                //just tell the server to write to the file
                file.writeToFile(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }
}
