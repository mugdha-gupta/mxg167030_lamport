package Message;

import java.io.Serializable;

//message sent to server from client to ask server to append a message to a file resource
public class AppendMessage implements Serializable {
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
