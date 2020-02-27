package Message;

import java.io.Serializable;

//reply message sent in response to a request
public class ReplyMessage implements Serializable {
    int clientId;
    int sourceServer;
    int timestamp;
    int fileNum;

    public ReplyMessage(int clientId, int sourceServer, int timestamp, int fileNum) {
        this.clientId = clientId;
        this.sourceServer = sourceServer;
        this.timestamp = timestamp;
        this.fileNum = fileNum;
    }

    public int getClientId() {
        return clientId;
    }

    public int getSourceServer() {
        return sourceServer;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getFileNum() {
        return fileNum;
    }
}
