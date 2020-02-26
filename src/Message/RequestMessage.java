package Message;

public class RequestMessage {
    int clientId;
    int requestingServer;
    int fileNum;
    int timestamp;
    String message;

    public RequestMessage(int clientId, int requestingServer, int fileNum, int timestamp, String message) {
        this.clientId = clientId;
        this.requestingServer = requestingServer;
        this.fileNum = fileNum;
        this.timestamp = timestamp;
        this.message = message;
    }

    public int getClientId() {
        return clientId;
    }

    public int getRequestingServer() {
        return requestingServer;
    }

    public int getFileNum() {
        return fileNum;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
