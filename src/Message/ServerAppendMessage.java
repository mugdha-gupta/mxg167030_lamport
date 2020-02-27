package Message;

import java.io.Serializable;

//message sent from server executing critical section to prompt other servers to append to files
//so that the files stay synchronized
public class ServerAppendMessage implements Serializable {
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
