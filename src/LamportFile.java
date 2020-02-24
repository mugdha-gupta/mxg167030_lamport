import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.Socket;
import java.util.HashMap;
import java.util.PriorityQueue;

public class LamportFile {
    int serverId;
    int lamportClock;
    int fileNum;
    String filename;
    String filepath;
    FileWriter fr;
    static PriorityQueue<ServerMessage> serverMessages;
    HashMap<Integer, ServerConnection> serverConnections;
    HashMap<Integer, ServerConnection> clientConnections;
    HashMap<Integer, Integer> lastReceivedTimeFromConnections;

    LamportFile(int fileNum, int serverId, HashMap<Integer, ServerConnection> serverConnections, HashMap<Integer, ServerConnection> clientConnections) throws IOException {
        lamportClock = 0;
        this.fileNum = fileNum;
        filename = "f" + fileNum + ".txt";
        this.serverId = serverId;
        this.fileNum = 0;
        this.clientConnections = clientConnections;
        this.serverConnections = serverConnections;
        filepath = "/home/012/m/mx/mxg167030/mxg167030_lamport/server" + serverId + "/" + filename;
        File file = new File(filepath);
        file.delete();
        file.createNewFile();
        fr = new FileWriter(file, true);
        serverMessages = new PriorityQueue<>();
        lastReceivedTimeFromConnections = new HashMap<>();
        for (Integer id: serverConnections.keySet()
             ) {
            lastReceivedTimeFromConnections.put(id, 0);
        }
    }

    void append(String s, int clientNum) throws IOException {
        //TODO: implement
        requestResourceEvent(s, clientNum);
    }

    private void end() throws IOException {
        ServerMessage message = new ServerMessage(ServerMessage.END_TYPE, serverId, lamportClock, fileNum, null, 0);
        sendToAll(message);
        for (ServerConnection serverConnection: serverConnections.values()
             ) {
            serverConnection.out.close();
        }
        fr.close();
    }

    void receiveEvent(ServerMessage message) throws IOException {
        incrementClock(message);
        setLastReceived(message);
        System.out.println("S" + serverId + " received message in lamportfile.java");

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
            case ServerMessage.END_TYPE:
                end();
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

        System.out.println("server " +  serverId + " entered the Critical Section");

        fr.write(message.message);
        serverMessages.remove(message);
        releaseEvent(message.clientNum);
        checkQueue();
    }


    private void releaseEvent(int clientNum) throws IOException {
        incrementClock();
        ServerMessage toSend = new ServerMessage(ServerMessage.RELEASE_TYPE, serverId, lamportClock, fileNum, null, clientNum);
        System.out.println("S" + serverId + " about to send release request to all");
        sendAck(clientNum);
        sendToAll(toSend);
    }

    private void sendAck(int clientNum) throws IOException {
        ServerConnection conn = clientConnections.get(clientNum);
        conn.sendMessage(new ClientServerMessage(ClientServerMessage.ACK, serverId, null, 0, fileNum));
    }

    private void setLastReceived(ServerMessage message) {
        lastReceivedTimeFromConnections.put(message.senderId, message.timeStamp);
    }

    private void processRelease(ServerMessage message) throws IOException {
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
                System.out.println("S" + serverId + " removing request from queue on release");
                fr.write(message.message);
                serverMessages.remove(queuedMessage);
                break;
            }
        }
    }

    private void processReply(ServerMessage message) {

    }

    private void receiveRequest(ServerMessage message) throws IOException {
        serverMessages.add(message);
        sendReplyEvent(message);
    }

    private void sendReplyEvent(ServerMessage message) throws IOException {
        incrementClock();
        ServerMessage toSend = new ServerMessage(ServerMessage.REPLY_TYPE, serverId, lamportClock, fileNum, null, message.clientNum);
        ServerConnection socket = serverConnections.get(message.senderId);
        System.out.println("S" + serverId + " sending reply to " + message.senderId);

        socket.sendMessage(toSend);
    }

    void requestResourceEvent(String s, int clientNum) throws IOException {
        incrementClock();
        ServerMessage message = new ServerMessage(ServerMessage.REQUEST_TYPE, serverId, lamportClock, fileNum, s, clientNum);
        serverMessages.add(message);
        System.out.println("S" + serverId + " added request to queue");
        sendToAll(message);
    }

    private void sendToAll(ServerMessage message) throws IOException {
        System.out.println("S" + serverId + " sending " + message.messageType +" message to all");

        for (ServerConnection socket: serverConnections.values()
             ) {
            socket.sendMessage(message);
        }
    }

    private void incrementClock() {
        System.out.println("S" + serverId + " incrementing clock");

        lamportClock++;
    }

    private void incrementClock(ServerMessage message){
        System.out.println("S" + serverId + " incrementing clock");

        lamportClock = Math.max(lamportClock+1, message.timeStamp+1);
    }

}
