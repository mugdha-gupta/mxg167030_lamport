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

    boolean inCriticalSection;

    LamportFile(int fileNum, Server s) throws IOException {
        this.fileNum = fileNum;
        server = s;
        lamportClock = 0;
        requestQueue = new PriorityQueue<>();
        lastReceivedTimeFromConnections = new HashMap<>();
        inCriticalSection = false;
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

        System.out.println(message.logString() + " message has been received in lamportFIle");
        incrementClock(message);
        setLastReceived(message);

        switch (message.messageType){
            case Message.REQUEST:
                System.out.println(message.logString() + " it is a request mesage");
                requestQueue.add(message);
                Message reply = new Message(Message.REPLY, message.fileNum, message.clientId, server.serverId, message.messageNum);
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
        System.out.println(lastReceivedTimeFromConnections.toString());
        checkToEnterCS();
    }

    synchronized private void checkToEnterCS() throws IOException {

        System.out.println(" checking to enter the CS");
        if(requestQueue.isEmpty() || inCriticalSection == true)
            return;
        System.out.println(" queue not empty");
        Message message = requestQueue.peek();
        if(message == null)
            return;
        System.out.println(" message at top of queue not null");
        if(message.serverId != server.serverId)
            return;
        System.out.println(" message at top of queue is my message");
        for (Integer time: lastReceivedTimeFromConnections.values()) {
            System.out.println(" message time " + message.timeStamp + " lastheard time " + time);
            if(message.timeStamp >= time)
                return;
            System.out.println(" 1 time passed");
        }
        enterCSEvent(message);
    }

    synchronized private void enterCSEvent(Message message) throws IOException {
        inCriticalSection = true;
        incrementClock();


        System.out.println(message.logString() + " ---entering critical section");
        System.out.println(" trying to write message " + message.message);
        fr.write(message.message);
        synchronizeOtherServers(message);
    }

    synchronized private void synchronizeOtherServers(Message message) throws IOException {

        System.out.println(message.logString() + " sync other servers");
        message.messageType = Message.SERVER_APPEND;
        for (MyServerSocket socket: server.servers.values()
             ) {
            socket.sendMessage(message);
        }
    }

    synchronized public void writeToFile(Message message) throws IOException {

        System.out.println(message.logString() + " writing to sync server files");
        fr.write(message.message);

        message.messageType = Message.ACK;
        server.servers.get(message.serverId).sendMessage(message);
    }

    synchronized public void releaseResourceEvent(Message message) throws IOException {
        System.out.println(message.logString() + " entered release event");

        incrementClock();

        requestQueue.remove(message);

        message.messageType = Message.RELEASE;
        message.timeStamp = lamportClock;
        sendToAll(message);
        inCriticalSection = false;
        checkToEnterCS();
    }
}
