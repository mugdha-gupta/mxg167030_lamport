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

        if(message == null)
            return;

        if(message instanceof AppendMessage){
            AppendMessage mess = (AppendMessage) message;
            System.out.println("server " + server.serverId + " receives: \"" + mess.getMessage() + "\" for file #" +
                    mess.getFileNum() + " at time: " + System.currentTimeMillis());
            System.out.println("server " + server.serverId + " attempting to acquire resource");
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.requestResourceEvent(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        else if(message instanceof RequestMessage){
            RequestMessage mess = (RequestMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.receiveRequestMessage(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
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
                    System.out.println("server " + server.serverId + " sends successful ack to to client " + mess.getClientId());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                server.ackMessages.add(mess);
            }
            return;
        }
        else if(message instanceof ServerAppendMessage) {
            ServerAppendMessage mess = (ServerAppendMessage) message;
            file = server.getLamportFile(mess.getFileNum());
            try {
                file.writeToFile(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }
}
