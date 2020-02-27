package Message;

import java.io.Serializable;

//message that prompts other servers to release a resource done executing in critical section
public class ReleaseMessage implements Serializable {
    int clientId;
    int requestingServer;
    int timestamp;
    int fileNum;

    public ReleaseMessage(int clientId, int requestingServer, int timestamp, int fileNum) {
        this.clientId = clientId;
        this.requestingServer = requestingServer;
        this.timestamp = timestamp;
        this.fileNum = fileNum;
    }

    public int getClientId() {
        return clientId;
    }

    public int getRequestingServer() {
        return requestingServer;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getFileNum() {
        return fileNum;
    }
}
