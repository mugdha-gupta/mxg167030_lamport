import Message.*;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;

/*
 * LamportFile class
 * Implements lamport mutual exclusion algorithm
 * methods synchronized so that multithreading doesn't interfere with the algorithm
 */
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
        //each server will write to its own copy of the file
        filepath = "/home/012/m/mx/mxg167030/mxg167030_lamport/server" + server.serverId + "/" + "f" + fileNum + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
        bw.close();
    }

    //make sure to append to file
    synchronized void appendToFile(String message) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath, true));
        bw.write(message + "\n");
        bw.close();
    }

    //increment clock
    synchronized private void incrementClock() {
        lamportClock++;
    }

    //increment clock based on the timestamp of a message
    synchronized private void incrementClock(int timeStamp){
        incrementClock();
        lamportClock = Math.max(lamportClock, timeStamp+1);
    }

    //request file event
    synchronized void requestResourceEvent(AppendMessage message) throws IOException {
        incrementClock();
        RequestMessage requestMessage = new RequestMessage(message.getClientId(), server.serverId, fileNum, lamportClock, message.getMessage());
        //add request to queue and send to all other servers
        requestQueue.add(requestMessage);
        sendToAll(requestMessage);
        //since we modified the queue make sure to check if append is possible
        checkToEnterCS();
    }

    //methods to send messages to all other servers
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

    //send a message to a specific server
    synchronized private void sendToServer(ReplyMessage message, int serverId) throws IOException {
        server.servers.get(serverId).sendMessage(message);
    }

    //reveive request event
    synchronized void receiveRequestMessage(RequestMessage message) throws IOException {
        //increment clock and set last received time
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        //add request to queue and send a reply to the server that requested the file
        requestQueue.add(message);
        ReplyMessage reply = new ReplyMessage(message.getClientId(), server.serverId, lamportClock, fileNum);
        sendToServer(reply, message.getRequestingServer());
        //modified queue so check if we can enter the cs
        checkToEnterCS();
    }

    //receive release event
    synchronized void receiveReleaseMessage(ReleaseMessage message) throws IOException {
        //increment clock, set last received, and release the cs
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        processRelease(message);
        //last received time changed so check queue
        checkToEnterCS();
    }

    //receive reply event
    synchronized void receiveReplyMessage(ReplyMessage message) throws IOException {
        incrementClock(message.getTimestamp());
        setLastReceived(message.getSourceServer(), message.getTimestamp());
        checkToEnterCS();
    }

    //helper method to process a release
    synchronized private void processRelease(ReleaseMessage message) {
        if(requestQueue.isEmpty())
            return;
        //delete all instances of a message from the same server, client, with same filenum
        RequestMessage m = new RequestMessage(0,0,0,0,null);
        for (RequestMessage queuedMessage: requestQueue) {
            if(queuedMessage.getRequestingServer() == message.getRequestingServer() && queuedMessage.getClientId() == message.getClientId()){
                m = queuedMessage;
            }
        }
        //remove outside foreach loop
        requestQueue.remove(m);

    }

    //set the last received message time
    synchronized private void setLastReceived(int serverId, int timeStamp) throws IOException {
        lastReceivedTimeFromConnections.put(serverId, timeStamp);
        checkToEnterCS();
    }

    //check if the server process can now enter the critical section
    synchronized private void checkToEnterCS() throws IOException {
        if(requestQueue.isEmpty() || inCriticalSection == true)
            return;
        RequestMessage message = requestQueue.peek();
        if(message == null)
            return;
        //if the message on top of the queue is from the native server
        if(message.getRequestingServer() != server.serverId)
            return;
        //and this server has heard from other two servers after this request was made
        for (Integer time: lastReceivedTimeFromConnections.values()) {
            if(message.getTimestamp() >= time)
                return;
        }
        //then we can enter the critical section
        enterCSEvent(message);
    }

    //enter the critical section
    synchronized private void enterCSEvent(RequestMessage message) throws IOException {
        inCriticalSection = true;
        messageBeingWrittenToCS = message;
        incrementClock();
        //append to the file and syncrhonize other servers
        appendToFile(message.getMessage());
        synchronizeOtherServers(message);
    }

    //synchronize other servers
    synchronized private void synchronizeOtherServers(RequestMessage message) throws IOException {
        //send a special message that doesn't count as an event to tell the other servers to append
        //since we are in the critical section no other servers can be updating the same fileNum
        //so they can append it on our prompting
        ServerAppendMessage serverAppendMessage = new ServerAppendMessage(message.getClientId(), fileNum, message.getMessage(), server.serverId);
        for (MyServerSocket socket: server.servers.values()
             ) {
            socket.sendMessage(serverAppendMessage);
        }
    }

    //called by the server on receiving a serverappendmessage to synchronize this server
    //with one currently executing the critical section
    synchronized public void writeToFile(ServerAppendMessage message) throws IOException {
        appendToFile(message.getMessage());
        //tell the server you have appended the file and are  up to date
        AckMessage ackMessage = new AckMessage(message.getClientId(), fileNum);
        server.servers.get(message.getSourceServer()).sendMessage(ackMessage);
    }

    //release the resource
    synchronized public void releaseResourceEvent() throws IOException {
        incrementClock();
        //remove the request from the queue
        requestQueue.remove(messageBeingWrittenToCS);
        //tell the other servers to release the request as well
        ReleaseMessage releaseMessage = new ReleaseMessage(messageBeingWrittenToCS.getClientId(), server.serverId, lamportClock, fileNum);
        sendToAll(releaseMessage);
        inCriticalSection = false;
        messageBeingWrittenToCS = null;
        checkToEnterCS();
    }
}
