import Message.*;

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
    static PriorityQueue<RequestMessage> requestQueue;

    HashMap<Integer, Integer> lastReceivedTimeFromConnections;

    boolean inCriticalSection;
    RequestMessage messageBeingWrittenToCS;

    LamportFile(int fileNum, Server s) throws IOException {
        this.fileNum = fileNum;
        server = s;
        lamportClock = 0;
        requestQueue = new PriorityQueue<>();
        lastReceivedTimeFromConnections = new HashMap<>();
        inCriticalSection = false;
        messageBeingWrittenToCS = null;
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
    }

    synchronized private void incrementClock(int timeStamp){
        incrementClock();
        lamportClock = Math.max(lamportClock, timeStamp+1);
    }

    synchronized void requestResourceEvent(AppendMessage message) throws IOException {
        incrementClock();
        RequestMessage requestMessage = new RequestMessage(message.getClientId(), server.serverId, fileNum, lamportClock, message.getMessage());
        requestQueue.add(requestMessage);
        System.out.println("\t2a. message added to request queue: " + requestQueue.toString());
//        System.out.println(message.logString() + " file is requesting resource, m has been added to the request Queue");
        sendToAll(requestMessage);
        System.out.println("\t2b. message sent to all other servers");
        checkToEnterCS();
    }

    synchronized private void sendToAll(RequestMessage message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
            System.out.println("3. Message type " + message.getClass().getName() + " sent to server " + serverSocket.remoteServerId);
        }
    }
    synchronized private void sendToAll(ReleaseMessage message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
            System.out.println("3. Message type " + message.getClass().getName() + " sent to server " + serverSocket.remoteServerId);
        }
    }

    synchronized private void sendToServer(ReplyMessage message, int serverId) throws IOException {
        System.out.println( " is being sent to server " + serverId);
        server.servers.get(serverId).sendMessage(message);
    }

    synchronized void receiveRequestMessage(RequestMessage message) throws IOException {
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        System.out.println("\t4a. request message arrived at lamp file");
        requestQueue.add(message);
        ReplyMessage reply = new ReplyMessage(message.getClientId(), server.serverId, lamportClock, fileNum);
        sendToServer(reply, message.getRequestingServer());
        checkToEnterCS();
    }

    synchronized void receiveReleaseMessage(ReleaseMessage message) throws IOException {
        System.out.println("received release mess");
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        processRelease(message);
        checkToEnterCS();
    }

    synchronized void receiveReplyMessage(ReplyMessage message) throws IOException {
        System.out.println("received reply mess");
        incrementClock(message.getTimestamp());
        setLastReceived(message.getSourceServer(), message.getTimestamp());
        checkToEnterCS();
    }


    synchronized private void processRelease(ReleaseMessage message) {
        System.out.println("processing release");
        if(requestQueue.isEmpty())
            return;
        for (RequestMessage queuedMessage: requestQueue) {
            if(queuedMessage.getRequestingServer() == message.getRequestingServer() && queuedMessage.getClientId() == message.getClientId()){
                requestQueue.remove(queuedMessage);
                System.out.println("removed from queue");
            }
        }
    }

    synchronized private void setLastReceived(int serverId, int timeStamp) throws IOException {

        System.out.println( " setting lastReceived");
        lastReceivedTimeFromConnections.put(serverId, timeStamp);
        System.out.println(lastReceivedTimeFromConnections.toString());
        checkToEnterCS();
    }

    synchronized private void checkToEnterCS() throws IOException {
        if(requestQueue.isEmpty() || inCriticalSection == true)
            return;
        RequestMessage message = requestQueue.peek();
        if(message == null)
            return;
        if(message.getRequestingServer() != server.serverId)
            return;
        for (Integer time: lastReceivedTimeFromConnections.values()) {
            if(message.getTimestamp() >= time)
                return;
        }
        System.out.println("--check CS passed--");
        enterCSEvent(message);
    }

    synchronized private void enterCSEvent(RequestMessage message) throws IOException {
        inCriticalSection = true;
        messageBeingWrittenToCS = message;
        incrementClock();
        fr.write(message.getMessage());
        synchronizeOtherServers(message);
    }

    synchronized private void synchronizeOtherServers(RequestMessage message) throws IOException {
        System.out.println("synch other servers");
        ServerAppendMessage serverAppendMessage = new ServerAppendMessage(message.getClientId(), fileNum, message.getMessage(), server.serverId);
        for (MyServerSocket socket: server.servers.values()
             ) {
            socket.sendMessage(serverAppendMessage);
        }
    }

    synchronized public void writeToFile(ServerAppendMessage message) throws IOException {
        System.out.println("writing to file");
        fr.write(message.getMessage());
        AckMessage ackMessage = new AckMessage(message.getClientId(), fileNum);
        server.servers.get(message.getSourceServer()).sendMessage(ackMessage);
    }

    synchronized public void releaseResourceEvent() throws IOException {
        System.out.println("releasing resource");
        incrementClock();

        requestQueue.remove(messageBeingWrittenToCS);
        ReleaseMessage releaseMessage = new ReleaseMessage(messageBeingWrittenToCS.getClientId(), server.serverId, lamportClock, fileNum);
        sendToAll(releaseMessage);
        inCriticalSection = false;
        messageBeingWrittenToCS = null;
        checkToEnterCS();
    }
}
