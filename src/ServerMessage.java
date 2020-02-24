import java.io.Serializable;

public class ServerMessage implements Comparable<ServerMessage>, Serializable {
    int messageType;
    int senderId;
    int timeStamp;
    int fileNum;
    String message;
    int clientNum;

    final static int REQUEST_TYPE = 0;
    final static int REPLY_TYPE = 1;
    final static int RELEASE_TYPE = 2;
    final static int END_TYPE = 3;

    public ServerMessage(int messageType, int senderId, int timeStamp, int fileNum, String message, int clientNum){
        this.messageType = messageType;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.fileNum = fileNum;
        this.message = message;
        this.clientNum = clientNum;
    }

    @Override
    public int compareTo(ServerMessage o) {
        if(this.timeStamp < o.timeStamp)
            return -1;
        else if (this.timeStamp > o.timeStamp)
            return 1;
        else{
            if(this.senderId < o.senderId)
                return -1;
            else if(this.senderId > o.senderId)
                return 1;
            else
                return 0;
        }
    }
}
