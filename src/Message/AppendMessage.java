package Message;

public class AppendMessage{
    int clientId;
    int fileNum;
    String message;

    public AppendMessage(int clientId, int fileNum, String message) {
        this.clientId = clientId;
        this.fileNum = fileNum;
        this.message = message;
    }

    public int getClientId() {
        return clientId;
    }

    public int getFileNum() {
        return fileNum;
    }

    public String getMessage() {
        return message;
    }
}
