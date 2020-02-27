import Message.*;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;

public class LamportFile {
    int fileNum;
    Server server;


    int lamportClock;
    PriorityQueue<RequestMessage> requestQueue;

    HashMap<Integer, Integer> lastReceivedTimeFromConnections;

    boolean inCriticalSection;
    RequestMessage messageBeingWrittenToCS;

    String filepath;

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

    synchronized private void setFileWriter() throws IOException {
        filepath = "/home/012/m/mx/mxg167030/mxg167030_lamport/server" + server.serverId + "/" + "f" + fileNum + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
        bw.close();
    }

    synchronized void appendToFile(String message) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath, true));
        bw.write(message + "\n");
        bw.close();
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
        sendToAll(requestMessage);
        checkToEnterCS();
    }

    synchronized private void sendToAll(RequestMessage message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
        }
    }
    synchronized private void sendToAll(ReleaseMessage message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
        }
    }

    synchronized private void sendToServer(ReplyMessage message, int serverId) throws IOException {
        server.servers.get(serverId).sendMessage(message);
    }

    synchronized void receiveRequestMessage(RequestMessage message) throws IOException {
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        requestQueue.add(message);
        ReplyMessage reply = new ReplyMessage(message.getClientId(), server.serverId, lamportClock, fileNum);
        sendToServer(reply, message.getRequestingServer());
        checkToEnterCS();
    }

    synchronized void receiveReleaseMessage(ReleaseMessage message) throws IOException {
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        processRelease(message);
        checkToEnterCS();
    }

    synchronized void receiveReplyMessage(ReplyMessage message) throws IOException {
        incrementClock(message.getTimestamp());
        setLastReceived(message.getSourceServer(), message.getTimestamp());
        checkToEnterCS();
    }


    synchronized private void processRelease(ReleaseMessage message) {
        if(requestQueue.isEmpty())
            return;
        RequestMessage m = new RequestMessage(0,0,0,0,null);
        for (RequestMessage queuedMessage: requestQueue) {
            if(queuedMessage.getRequestingServer() == message.getRequestingServer() && queuedMessage.getClientId() == message.getClientId()){
                m = queuedMessage;
            }
        }
        requestQueue.remove(m);
//        System.out.println("removed from queue");

    }

    synchronized private void setLastReceived(int serverId, int timeStamp) throws IOException {
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
        enterCSEvent(message);
    }

    synchronized private void enterCSEvent(RequestMessage message) throws IOException {
        inCriticalSection = true;
        messageBeingWrittenToCS = message;
        incrementClock();
        appendToFile(message.getMessage());
        synchronizeOtherServers(message);
    }

    synchronized private void synchronizeOtherServers(RequestMessage message) throws IOException {
        ServerAppendMessage serverAppendMessage = new ServerAppendMessage(message.getClientId(), fileNum, message.getMessage(), server.serverId);
        for (MyServerSocket socket: server.servers.values()
             ) {
            socket.sendMessage(serverAppendMessage);
        }
    }

    synchronized public void writeToFile(ServerAppendMessage message) throws IOException {
        appendToFile(message.getMessage());
        AckMessage ackMessage = new AckMessage(message.getClientId(), fileNum);
        server.servers.get(message.getSourceServer()).sendMessage(ackMessage);
    }

    synchronized public void releaseResourceEvent() throws IOException {
        incrementClock();

        requestQueue.remove(messageBeingWrittenToCS);
        ReleaseMessage releaseMessage = new ReleaseMessage(messageBeingWrittenToCS.getClientId(), server.serverId, lamportClock, fileNum);
        sendToAll(releaseMessage);
        inCriticalSection = false;
        messageBeingWrittenToCS = null;
        checkToEnterCS();
    }
}
