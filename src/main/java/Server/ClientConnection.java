package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable{

    /**
     * A static list to keep track of our connected clients
     * Buffered I/O for efficiency
     */
    private final ArrayList<Channel> channelListReference = Server.getChannelList();
    private final ArrayList<ClientConnection> clientList = Server.getClientList();
    private Channel currentChannel;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    public String clientUsername;
    private InetAddress IP;
    private int listenPort; //Port that client listens for voicePackages

    /**
     * Wrap the byte stream provided through the socket into a character stream used by our buffered I/O
     * Read the username from client.
     */
    public ClientConnection(Socket socket, InetAddress clientIP) throws IOException {
        try{
            this.socket = socket;
            this.IP = clientIP;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] numberAndPort = reader.readLine().split("\"");
            this.clientUsername = numberAndPort[0];
            this.listenPort = Integer.parseInt(numberAndPort[1]);

        } catch (IOException e) {
            closeConnection();
        }
    }

    /**
     * Wait for buffer to read a message halting the program
     * Check if the message is null, else havoc ensues when someone leaves the server
     */
    @Override
    public void run() {
        while(!socket.isClosed()){
            try{
                String message = reader.readLine();
                if(message == null){
                    throw new IOException();    //DO NOT REMOVE
                }
                if(message.contains("$¤")){
                    sendChannelList();
                }
                if(message.contains("$#")){
                    changeChannel(message);
                }
                else{
                    broadcast(message);
                }
            } catch (IOException e) {
                try {
                    closeConnection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
        }
    }

    /**
     * If the request was reasonable we put the client in desired channel
     */
    public void changeChannel(String request) throws IOException {
        int parsed = Integer.parseInt(request.substring(2));
        if(parsed >= 0 && parsed < channelListReference.size()){
            assignChannel(this, parsed);
        }
    }
    /**
     * Removes client from currentChannel
     */
    public void removeClient() throws IOException {
        currentChannel.removeConnection(this);
        broadcast(clientUsername + " has left the channel");
        updateClientChannels();
    }

    /**
     * Remove connection from channel list and from the servers client list
     * Close open streams
     */
    @SuppressWarnings("Duplicates")
    public void closeConnection() throws IOException {
        removeClient();
        clientList.remove(this);
        try{
            if(socket != null){
                socket.close();
            }
            if(reader != null){
                reader.close();
            }
            if(writer != null){
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loop through the client list, sending the message to everyone BUT the sender
     */
    public void broadcast(String message) throws IOException {
        System.out.println(message);
        for(ClientConnection handler : currentChannel.clientConnections){
            try{
                if(handler != this){
                    handler.writer.write(message);
                    handler.writer.newLine();
                    handler.writer.flush();
                }
            } catch (IOException e) {
                closeConnection();
            }
        }
    }

    /**
     * Send the channelList to all clients connected in order to force a channellist refresh
     * This can probably be done prettier
     */
    public void updateClientChannels() throws IOException {
        for(Channel channel1 : channelListReference){
            for(ClientConnection clientConnection : channel1.clientConnections){
                clientConnection.sendChannelList();
            }
        }
    }

    /**
     * Send the channelList as a string for the client to break up and display
     * Start transmission with a dollar-sign($) to signal that data is coming
     * Can probably be done prettier
     */
    public void sendChannelList() throws IOException {
        try{
            writer.write("$¤");
            for(Channel channel : channelListReference){
                writer.write(channel.toString() + "\"");
            }
            writer.newLine();
            writer.flush();
        } catch (IOException e){
            closeConnection();
        }
    }

    /**
     * Remove the client from its current channel, adds to desired index and send an update to all clients
     */
    public void assignChannel(ClientConnection client, int channel) throws IOException {
        if(client.getCurrentChannel() != null){
            client.removeClient();
        }
        channelListReference.get(channel).addConnection(client);
        client.updateClientChannels();
    }

    public void setCurrentChannel(Channel channel){
        this.currentChannel = channel;
    }
    public Channel getCurrentChannel() {
        return currentChannel;
    }
    public InetAddress getIP() {
        return IP;
    }
    public int getListenPort() {
        return listenPort;
    }
}

