package Message;

public class AckMessage {
    int clientId;
    int fileNum;

    public AckMessage(int clientId, int fileNum) {
        this.clientId = clientId;
        this.fileNum = fileNum;
    }

    public int getClientId() {
        return clientId;
    }

    public int getFileNum() {
        return fileNum;
    }
}
