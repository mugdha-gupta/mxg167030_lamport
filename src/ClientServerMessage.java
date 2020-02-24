import java.io.Serializable;

public class ClientServerMessage implements Serializable {
    public static final int APPEND_TYPE = 0;
    public static final int ACK = 1;
    public static final int Fail = 2;
    public static final int END_TYPE = 3;
    public int messageType;
    int clientId;
    int serverId;
    String message;
    int messageNum;
    int fileNum;

    public ClientServerMessage(int clientId, int serverId, String message, int messageNum, int fileNum) {
        this.clientId = clientId;
        this.serverId = serverId;
        this.message = message;
        this.messageNum = messageNum;
        this.fileNum = fileNum;
    }
}
