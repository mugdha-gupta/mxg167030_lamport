import java.io.Serializable;

public class Message implements Comparable, Serializable {
    int messageType;
    int fileNum;
    static int clientId;
    static int serverId;
    int messageNum;
    String message;

    int timeStamp;
    boolean success = false;

    static final int APPEND = 0;
    public static final int ACK = 1;
    static final int REQUEST = 3;
    static final int REPLY = 4;
    static final int RELEASE = 5;
    static final int SERVER_APPEND = 6;

    public Message(int messageType, int fileNum, int clientId, int serverId, int messageNum) {
        this.messageType = messageType;
        this.fileNum = fileNum;
        this.clientId = clientId;
        this.serverId = serverId;
        this.messageNum = messageNum;

        switch (messageType){
            //implement all switch statemests
            case APPEND:
                message  = "client " + clientId + " message #" + messageNum + " -- server" + serverId;
                break;
            default:
                break;
        }
    }

    public static String successString() {
        return "client " + clientId +  " receives a successful ack from server " + serverId;
    }

    public static String failString() {
        return "client " + clientId +  " receives a failure from server " + serverId;
    }

    @Override
    public int compareTo(Object o) {
        Message m = ((Message) o);
        if(this.timeStamp < m.timeStamp)
            return -1;
        else if (this.timeStamp > m.timeStamp)
            return 1;
        else{
            if(this.serverId < m.serverId)
                return -1;
            else if(this.serverId > m.serverId)
                return 1;
            else
                return 0;
        }
    }
}