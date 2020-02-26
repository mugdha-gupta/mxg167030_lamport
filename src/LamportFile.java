import Message.*;

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
    static PriorityQueue<RequestMessage> requestQueue;

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

    private void setFileWriter() throws IOException {
        filepath = "/home/012/m/mx/mxg167030/mxg167030_lamport/server" + server.serverId + "/" + "f" + fileNum + ".txt";
        System.out.println("trying to create " + filepath);
        File file = new File(filepath);
        file.delete();
        file.createNewFile();
    }

    synchronized void appendToFile(String message) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath, true));
        bw.write(message);
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
//        System.out.println("An append Request arrived at Server " + server.serverId + "\n");
        incrementClock();
        RequestMessage requestMessage = new RequestMessage(message.getClientId(), server.serverId, fileNum, lamportClock, message.getMessage());
//        System.out.println("REQUEST: \n" +
//                                    "\ttime:" + requestMessage.getTimestamp() +
//                                    "\tclientId:" + requestMessage.getClientId());
        requestQueue.add(requestMessage);
//        System.out.println("\tadded to queue");
//        System.out.println("\t2a. message added to request queue: " + requestQueue.toString());
//        System.out.println(message.logString() + " file is requesting resource, m has been added to the request Queue");
        sendToAll(requestMessage);
//        System.out.println("\t2b. message sent to all other servers");
        checkToEnterCS();
    }

    synchronized private void sendToAll(RequestMessage message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
//            System.out.println("\tsent to All");
//            System.out.println("3. Message type " + message.getClass().getName() + " sent to server " + serverSocket.remoteServerId);
        }
    }
    synchronized private void sendToAll(ReleaseMessage message) throws IOException {
        for (MyServerSocket serverSocket : server.servers.values()) {
            serverSocket.sendMessage(message);
//            System.out.println("3. Message type " + message.getClass().getName() + " sent to server " + serverSocket.remoteServerId);
        }
    }

    synchronized private void sendToServer(ReplyMessage message, int serverId) throws IOException {
//        System.out.println( " is being sent to server " + serverId);
        server.servers.get(serverId).sendMessage(message);
    }

    synchronized void receiveRequestMessage(RequestMessage message) throws IOException {
        System.out.println("An Receive message arrived at Server " + server.serverId + "\n");
        incrementClock(message.getTimestamp());
//        System.out.println("REQUEST: \n" +
//                "\ttime:" + message.getTimestamp() +
//                "\tclientId:" + message.getClientId() +
//                "\trequesting server:" + message.getRequestingServer());
//        System.out.println("\ttimestamp set to " + lamportClock);
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
//        System.out.println("\t4a. request message arrived at lamp file with timestamp " + message.getTimestamp());
        requestQueue.add(message);
//        System.out.println("\trequest added to queue");
        ReplyMessage reply = new ReplyMessage(message.getClientId(), server.serverId, lamportClock, fileNum);
//        System.out.println("REPLY: \n" +
//                "\ttime:" + reply.getTimestamp() +
//                "\tclientId:" + reply.getClientId());
//        System.out.println("\treply sent with timestamp " + lamportClock);
//        System.out.println("\tsending to " + message.getRequestingServer());
        sendToServer(reply, message.getRequestingServer());
        checkToEnterCS();
    }

    synchronized void receiveReleaseMessage(ReleaseMessage message) throws IOException {

        System.out.println("An release message arrived at Server " + server.serverId + "\n");
//        System.out.println("received release mess");
        incrementClock(message.getTimestamp());
        setLastReceived(message.getRequestingServer(), message.getTimestamp());
        processRelease(message);
        checkToEnterCS();
    }

    synchronized void receiveReplyMessage(ReplyMessage message) throws IOException {

//        System.out.println("A reply message arrived at Server " + server.serverId + "\n");
//        System.out.println("received reply mess");
//        System.out.println("REPLY: \n" +
//                "\ttime:" + message.getTimestamp() +
//                "\tclientId:" + message.getClientId() +
//                "\treplyingServer:"+ message.getSourceServer());
        incrementClock(message.getTimestamp());
//        System.out.println("\ttime stamp set to " + lamportClock);
        setLastReceived(message.getSourceServer(), message.getTimestamp());
        checkToEnterCS();
    }


    synchronized private void processRelease(ReleaseMessage message) {
        System.out.println("processing release");
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

//        System.out.println( " setting lastReceived");
        lastReceivedTimeFromConnections.put(serverId, timeStamp);
        System.out.println(lastReceivedTimeFromConnections.toString());
        checkToEnterCS();
    }

    synchronized private void checkToEnterCS() throws IOException {
        System.out.println("checking to enter");
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
//        System.out.println("--check CS passed--");
        enterCSEvent(message);
    }

    synchronized private void enterCSEvent(RequestMessage message) throws IOException {
//        System.out.println("entering CS");;
        inCriticalSection = true;
        messageBeingWrittenToCS = message;
        incrementClock();
        appendToFile(message.getMessage());
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
//        System.out.println("writing to file");
        appendToFile(message.getMessage());
        AckMessage ackMessage = new AckMessage(message.getClientId(), fileNum);
        server.servers.get(message.getSourceServer()).sendMessage(ackMessage);
    }

    synchronized public void releaseResourceEvent() throws IOException {
//        System.out.println("releasing resource");
        incrementClock();

        requestQueue.remove(messageBeingWrittenToCS);
        ReleaseMessage releaseMessage = new ReleaseMessage(messageBeingWrittenToCS.getClientId(), server.serverId, lamportClock, fileNum);
        sendToAll(releaseMessage);
        inCriticalSection = false;
        messageBeingWrittenToCS = null;
        checkToEnterCS();
    }
}
