package Message;

public class ServerAppendMessage {
    int clientId;
    int fileNum;
    String message;
    int sourceServer;

    public ServerAppendMessage(int clientId, int fileNum, String message, int sourceServer) {
        this.clientId = clientId;
        this.fileNum = fileNum;
        this.message = message;
        this.sourceServer = sourceServer;
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

    public int getSourceServer() {
        return sourceServer;
    }
}
