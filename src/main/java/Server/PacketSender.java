package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Class to decide who is to receive voice packages, pack them and ship them
 */
public class PacketSender implements Runnable{
    private final DatagramSocket senderSocket;
    private final DatagramPacket voicePacket;
    private Channel clientChannel;

    public PacketSender(DatagramPacket packageToSend) throws SocketException {
        this.senderSocket = new DatagramSocket();
        this.voicePacket = packageToSend;
    }

    /**
     *  Finds out which client sent the package and sends the data to everyone in the relevant channel but the sender
     */
    public void sendToClients() throws IOException {
        ArrayList<ClientConnection> clientConnections = Server.getClientList();
        String fromClientIP = voicePacket.getAddress().getHostAddress();
        int fromClientPort = voicePacket.getPort();
        byte[] packetBuffer = voicePacket.getData();

        //Find the relevant channel
        for(ClientConnection client : clientConnections){
            if(client.getIP().getHostAddress().equals(fromClientIP) && client.getListenPort() == fromClientPort){
                clientChannel = client.getCurrentChannel();
                break;
            }
        }

        //Go through the channel
        if(clientChannel != null){
            for(ClientConnection client : clientChannel.clientConnections){
                if(!client.getIP().getHostAddress().equals(fromClientIP) ||
                client.getIP().getHostAddress().equals(fromClientIP) && client.getListenPort() != fromClientPort){
                    DatagramPacket packageToSend = new DatagramPacket(packetBuffer, packetBuffer.length, client.getIP(), client.getListenPort());
                    System.out.println("Sending package to " + client.getIP() + "PORT: " + client.getListenPort());
                    senderSocket.send(packageToSend);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            sendToClients();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
