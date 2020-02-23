public class ServerMessage {
    int messageType;
    int senderId;
    int timeStamp;
    int fileNum;

    final static int REQUEST_TYPE = 0;
    final static int REPLY_TYPE = 1;
    final static int RELEASE_TYPE = 2;


    public ServerMessage(int messageType, int senderId, int timeStamp, int fileNum){
        this.messageType = messageType;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.fileNum = fileNum;
    }
}
