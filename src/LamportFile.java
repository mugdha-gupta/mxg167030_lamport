import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;

public class LamportFile {
    int serverId;
    int lamportClock;
    int fileNum;
    static PriorityQueue<ServerMessage> serverMessages;
    HashMap<Integer, ServerConnection> serverConnections;
    HashMap<Integer, Integer> lastReceivedTimeFromConnections;

    LamportFile(int fileNum, int serverId, HashMap<Integer, ServerConnection> serverConnections){
        lamportClock = 0;
        this.fileNum = fileNum;
        this.serverId = serverId;
        this.fileNum = 0;
        this.serverConnections = serverConnections;
        serverMessages = new PriorityQueue<>();
        lastReceivedTimeFromConnections = new HashMap<>();
        for (Integer id: serverConnections.keySet()
             ) {
            lastReceivedTimeFromConnections.put(id, 0);
        }
    }

    void append(String s) throws IOException {
        //TODO: implement
        requestResourceEvent();
    }

    void receiveEvent(ServerMessage message) throws IOException {
        incrementClock(message);
        setLastReceived(message);

        switch (message.messageType){
            case ServerMessage.REQUEST_TYPE:
                receiveRequest(message);
                break;
            case ServerMessage.REPLY_TYPE:
                processReply(message);
                break;
            case ServerMessage.RELEASE_TYPE:
                processRelease(message);
                break;
        }

        checkQueue();
    }

    private void checkQueue() throws IOException {
        if(serverMessages.isEmpty())
            return;
        ServerMessage message = serverMessages.peek();
        if(message == null)
            return;
        if(message.senderId != serverId)
            return;
        for (Integer time: lastReceivedTimeFromConnections.values()
             ) {
            if(message.timeStamp >= time)
                return;
        }
        //TODO: write to a file
        serverMessages.remove(message);
        System.out.println("server " +  serverId + " entered the Critical Section");
        releaseEvent();
        checkQueue();
    }

    private void releaseEvent() throws IOException {
        incrementClock();
        ServerMessage toSend = new ServerMessage(ServerMessage.RELEASE_TYPE, serverId, lamportClock, fileNum);
        sendToAll(toSend);
    }

    private void setLastReceived(ServerMessage message) {
        lastReceivedTimeFromConnections.put(message.senderId, message.timeStamp);
    }

    private void processRelease(ServerMessage message) {
        if(serverMessages.isEmpty())
            return;
        ServerMessage queueMessage = serverMessages.peek();
        if(queueMessage == null){
            System.err.println("couldn't release message from queue");
            return;
        }
        for (ServerMessage queuedMessage: serverMessages
             ) {
            if(queuedMessage.senderId == message.senderId &&
                queuedMessage.fileNum == message.fileNum){
                serverMessages.remove(queuedMessage);
                break;
            }
        }
    }

    private void processReply(ServerMessage message) {

    }

    private void receiveRequest(ServerMessage message) throws IOException {
        serverMessages.add(message);
        sendReplyEvent(message.senderId);
    }

    private void sendReplyEvent(int senderId) throws IOException {
        incrementClock();
        ServerMessage message = new ServerMessage(ServerMessage.REPLY_TYPE, serverId, lamportClock, fileNum);
        ServerConnection socket = serverConnections.get(senderId);
        socket.sendMessage(message);
    }

    void requestResourceEvent() throws IOException {
        incrementClock();
        ServerMessage message = new ServerMessage(ServerMessage.REQUEST_TYPE, serverId, lamportClock, fileNum);
        serverMessages.add(message);
        sendToAll(message);
    }

    private void sendToAll(ServerMessage message) throws IOException {
        for (ServerConnection socket: serverConnections.values()
             ) {
            socket.sendMessage(message);
        }
    }

    private void incrementClock() {
        lamportClock++;
    }

    private void incrementClock(ServerMessage message){
        lamportClock = Math.max(lamportClock+1, message.timeStamp+1);
    }

}
