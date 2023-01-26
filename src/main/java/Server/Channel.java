package Server;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A class to provide clients to connect and chat inside channels
 * Holds all connections in a list and use that list to broadcast messages
 */

public class Channel {
    public ArrayList<ClientConnection> clientConnections = new ArrayList<>();
    private final String channelName;
    private final int capacity;

    public Channel(int capacity, String roomName){
        this.capacity = capacity;
        this.channelName = roomName;
    }

    public void addConnection(ClientConnection newClient) throws IOException {
        if(clientConnections.size() != capacity){
            clientConnections.add(newClient);
            newClient.setCurrentChannel(this);
            newClient.broadcast(newClient.clientUsername + " has entered the channel");
        }
        else{
            System.out.println("Channel is full");
        }
    }

    /**
     * Remove connection from channel list
     */
    public void removeConnection(ClientConnection connection){
        clientConnections.remove(connection);
    }
    @Override
    public String toString() {
        return channelName + " " + clientConnections.size() + "/" + capacity;
    }
}
