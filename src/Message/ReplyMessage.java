package Message;

public class ReplyMessage {
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
}
