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
        System.out.println("trying to create " + filepath);
        File file = new File(filepath);
        file.delete();
        file.createNewFile();
        fr = new FileWriter(file, true);
        fr.write("appended to file\n");
        System.out.println("created file");
    }

    synchronized private void incrementClock() {
        lamportClock++;
        System.out.println("clock incremented to " + lamportClock);
    }

    synchronized private void incrementClock(Message message){
        incrementClock();
        lamportClock = Math.max(lamportClock, message.timeStamp+1);
        System.out.println("clock incremented to " +  lamportClock);
    }

    synchronized void requestResourceEvent(Message message) throws IOException {
        incrementClock();
        message.timeStamp = lamportClock;
        message.messageType = Message.REQUEST;

        requestQueue.add(message);
        System.out.println(message.logString() + " file is requesting resource, m has been added to the request Queue");
        sendToAll(message);
        checkToEnterCS();
    }

    synchronized private void sendToAll(Message message) throws IOException {
        System.out.println(message.logString() + " sending the message to all, " + message.messageType);
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
        }
    }

    synchronized private void sendToServer(Message message, int serverId) throws IOException {
        System.out.println(message.logString() + " is being sent to server " + serverId);
        server.servers.get(serverId).sendMessage(message);
    }

    synchronized void receiveMessageEvent(Message message) throws IOException {
        incrementClock(message);
        setLastReceived(message);
        System.out.println(message.logString() + " message has been received in lamportFIle");

        switch (message.messageType){
            case Message.REQUEST:
                System.out.println(message.logString() + " it is a request mesage");
                requestQueue.add(message);
                Message reply = new Message(Message.REPLY, message.fileNum, message.clientId, message.serverId, message.messageNum);
                reply.timeStamp = lamportClock;
                sendToServer(reply, message.serverId);

                System.out.println(message.logString() + " message has been added to request queue and sent to server");
                break;
            case Message.RELEASE:

                System.out.println(message.logString() + " it is a release message");
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

        System.out.println(message.logString() + " message is about to be processesed");
        for (Message queuedMessage: requestQueue) {
            if(queuedMessage.serverId == message.serverId){

                System.out.println(message.logString() + " message is being removed from the remote server queue");
                requestQueue.remove(queuedMessage);
                break;
            }
        }
    }

    synchronized private void setLastReceived(Message message) throws IOException {

        System.out.println(message.logString() + " setting lastReceived");
        lastReceivedTimeFromConnections.put(message.serverId, message.timeStamp);
        checkToEnterCS();
    }

    synchronized private void checkToEnterCS() throws IOException {

        System.out.println(" checking to enter the CS");
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


        System.out.println(message.logString() + " entering critical section");
        fr.write(message.message);
        synchronizeOtherServers(message);
    }

    synchronized private void synchronizeOtherServers(Message message) throws IOException {

        System.out.println(message.logString() + " sync other servers");
        Message m = new Message(Message.SERVER_APPEND, fileNum, message.clientId, message.serverId, message.messageNum );
        for (MyServerSocket socket: server.servers.values()
             ) {
            socket.sendMessage(m);
        }
    }

    synchronized public void writeToFile(Message message) throws IOException {

        System.out.println(message.logString() + " writing to sync server files");
        fr.write(message.message);

        message.messageType = Message.ACK;
        server.servers.get(message.serverId).sendMessage(message);
    }

    synchronized public void releaseResourceEvent(Message message) throws IOException {
        incrementClock();

        requestQueue.remove(message);

        message.messageType = Message.RELEASE;
        message.timeStamp = lamportClock;


        System.out.println(message.logString() + " release being sent to all");
        sendToAll(message);

        checkToEnterCS();
    }
}
