import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;

public class LamportFile {
    int fileNum;
    Server server;

    FileWriter fr;

    int lamportClock;
    static PriorityQueue<Message> requestQueue;

    HashMap<Integer, Integer> lastReceivedTimeFromConnections;

    LamportFile(int fileNum, Server s) throws IOException {
        this.fileNum = fileNum;
        server = s;
        lamportClock = 0;
        requestQueue = new PriorityQueue<>();
        lastReceivedTimeFromConnections = new HashMap<>();
        for (Integer id: server.servers.keySet()) {  lastReceivedTimeFromConnections.put(id, 0);}

        setFileWriter();
    }

    private void setFileWriter() throws IOException {
        String filepath = "/home/012/m/mx/mxg167030/mxg167030_lamport/server" + server.serverId + "/" + "f" + fileNum + ".txt";
        File file = new File(filepath);
        file.delete();
        file.createNewFile();
        fr = new FileWriter(file, true);
    }

    synchronized private void incrementClock() {
        lamportClock++;
    }

    synchronized private void incrementClock(Message message){
        incrementClock();
        lamportClock = Math.max(lamportClock, message.timeStamp+1);
    }

    synchronized void requestResourceEvent(Message message) throws IOException {
        incrementClock();
        message.timeStamp = lamportClock;
        requestQueue.add(message);
        sendToAll(message);
        checkToEnterCS();
    }

    synchronized private void sendToAll(Message message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
        }
    }

    synchronized private void sendToServer(Message message, int serverId) throws IOException {
        server.servers.get(serverId).sendMessage(message);
    }

    synchronized void receiveMessageEvent(Message message) throws IOException {
        incrementClock(message);
        setLastReceived(message);
        System.out.println("S" + server.serverId + " received message in lamportfile.java");

        switch (message.messageType){
            case Message.REQUEST:
                requestQueue.add(message);
                Message reply = new Message(Message.REPLY, message.fileNum, message.clientId, message.serverId, message.messageNum);
                reply.timeStamp = lamportClock;
                sendToServer(reply, message.serverId);
                break;
            case Message.RELEASE:
                processRelease(message);
                break;
        }

        checkToEnterCS();
    }

    synchronized private void processRelease(Message message) throws IOException {
        if(requestQueue.isEmpty())
            return;
        Message queueMessage = requestQueue.peek();
        if(queueMessage == null){
            System.err.println("couldn't release message from queue");
            return;
        }
        for (Message queuedMessage: requestQueue) {
            if(queuedMessage.serverId == message.serverId){
                System.out.println("S" + server.serverId + " removing request from queue on release");
                fr.write(message.message);
                requestQueue.remove(queuedMessage);
                break;
            }
        }
    }

    synchronized private void setLastReceived(Message message) throws IOException {
        lastReceivedTimeFromConnections.put(message.serverId, message.timeStamp);
        checkToEnterCS();
    }

    synchronized private void checkToEnterCS() throws IOException {
        if(requestQueue.isEmpty())
            return;
        Message message = requestQueue.peek();
        if(message == null)
            return;
        if(message.serverId != server.serverId)
            return;
        for (Integer time: lastReceivedTimeFromConnections.values()) {
            if(message.timeStamp >= time)
                return;
        }
        enterCSEvent(message);
    }

    synchronized private void enterCSEvent(Message message) throws IOException {
        incrementClock();

        System.out.println("server " +  server.serverId + " entered the Critical Section");
        fr.write(message.message);
        synchronizeOtherServers(message);
    }

    synchronized private void synchronizeOtherServers(Message message) throws IOException {
        Message m = new Message(Message.SERVER_APPEND, fileNum, message.clientId, message.serverId, message.messageNum );
        for (MyServerSocket socket: server.servers.values()
             ) {
            socket.sendMessage(m);
        }
    }

    synchronized public void writeToFile(Message message) throws IOException {
        fr.write(message.message);

        message.messageType = Message.ACK;
        server.servers.get(message.serverId).sendMessage(message);
    }

    synchronized public void releaseResourceEvent(Message message) throws IOException {
        incrementClock();

        requestQueue.remove(message);

        message.messageType = Message.RELEASE;
        message.timeStamp = lamportClock;

        System.out.println("S" + server.serverId + " about to send release request to all");
        sendToAll(message);

        checkToEnterCS();
    }
}
