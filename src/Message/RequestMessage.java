package Message;

import java.io.Serializable;

//message that requests a resource
//implements comparable to allow it to get stored in a priority queue
public class RequestMessage implements Serializable, Comparable {
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

    @Override
    public int compareTo(Object o) {
        RequestMessage m = ((RequestMessage) o);
        if(this.timestamp < m.timestamp)
            return -1;
        else if (this.timestamp > m.timestamp)
            return 1;
        else{
            if(this.requestingServer < m.requestingServer)
                return -1;
            else if(this.requestingServer > m.requestingServer)
                return 1;
            else
                return 0;
        }
    }
}
