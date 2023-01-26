package Server;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private static final ArrayList<Channel> channelList = new ArrayList<>();
    private static final ArrayList<ClientConnection> clientList = new ArrayList<>();
    private final ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    /**
     * Run an instance of PacketReceiver waiting for voice transmissions in the form of datagram packages
     * Wait for TCP connections with ServerSocket.accept()
     * Then run a thread for each socket returned to handle the client connection
     */

    public void runServer() throws IOException {
        try{
            //Listen for UDP packages, choose one port above TCP
            executorService.submit(new PacketReceiver(60001, executorService));
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("New client has connected from IP: " + socket.getRemoteSocketAddress());

                //Store IP address in clientConnection for sending voice with PacketSender
                InetAddress clientIP = socket.getInetAddress();
                ClientConnection clientConnection = new ClientConnection(socket, clientIP);

                clientConnection.assignChannel(clientConnection, 0);
                clientList.add(clientConnection);
                executorService.submit(clientConnection);
            }
        } catch (IOException e) {
            serverSocket.close();
            throw new RuntimeException(e);
        }
    }


    public static void main(String []args) throws IOException {
        setSysProperties();
        ServerSocket serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(60000);
        Server server = new Server(serverSocket);
        initializeChannels();
        server.runServer();
    }

    /**
     * Create server channels.
     */
    public static void initializeChannels(){
        channelList.add(new Channel(10, "Lobby"));
        channelList.add(new Channel(4, "Public #1"));
        channelList.add(new Channel(4, "Public #2"));
        channelList.add(new Channel(4, "Public #3"));
        channelList.add(new Channel(4, "AFK"));
    }

    /**
     * Point to relevant resources for SSL connection
     */
    public static void setSysProperties(){
        //System.setProperty("javax.net.debug", "all");
        System.setProperty("javax.net.ssl.keyStore", "src/main/resources/keystore/serverKeyStore.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/keystore/serverTrustStore.p12");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }
    public static ArrayList<Channel> getChannelList() {
        return channelList;
    }
    public static ArrayList<ClientConnection> getClientList() {
        return clientList;
    }
}
