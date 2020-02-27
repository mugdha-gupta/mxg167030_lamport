package Message;

import java.io.Serializable;

//message that indicates the synchronization is finished
public class AckMessage implements Serializable {
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
