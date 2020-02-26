import Message.*;

import java.io.IOException;


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
                file.requestResourceEvent(mess);
                System.out.println("\t1a. append message has been sent to lamp file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(message instanceof RequestMessage){
            RequestMessage mess = (RequestMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.receiveRequestMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(message instanceof ReleaseMessage){
            ReleaseMessage mess = (ReleaseMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.receiveReleaseMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(message instanceof ReplyMessage){
            ReplyMessage mess = (ReplyMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.receiveReplyMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(message instanceof AckMessage) {
            AckMessage mess = (AckMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            boolean success = false;
            AckMessage messToRem = new AckMessage(0, 0);
            for (AckMessage ack : server.ackMessages) {
                if (ack.getClientId() == mess.getClientId() && ack.getFileNum() == mess.getFileNum()) {
                    //match found!
                    success = true;
                    messToRem = ack;
                    break;
                }
            }

            if (success) {
                server.ackMessages.remove(messToRem);
                SuccessMessage successMessage = new SuccessMessage("client " + mess.getClientId() +  " receives a successful ack from server " + server.serverId);
                try {
                    server.clients.get(mess.getClientId()).sendMessage(successMessage);
                    file.releaseResourceEvent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                server.ackMessages.add(mess);
            }
        }
        else if(message instanceof ServerAppendMessage) {
            ServerAppendMessage mess = (ServerAppendMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.writeToFile(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
