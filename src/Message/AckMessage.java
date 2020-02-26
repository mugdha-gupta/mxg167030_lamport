package Message;

public class AckMessage {
    int clientId;
    int fileNum;

    public AckMessage(int clientId, int fileNum) {
        this.clientId = clientId;
        this.fileNum = fileNum;
    }
}
