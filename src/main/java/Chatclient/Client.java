package Chatclient;

import javafx.application.Platform;
import java.io.*;
import java.net.*;

/**
 * Class that handles the communication with the server socket
 */
public class Client {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String username;
    private int listenPort;
    private DatagramSocket UDPSocket;

    public Client(Socket socket, String username, int listenPort){
        try{
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.listenPort = listenPort;
            this.UDPSocket = new DatagramSocket(listenPort);
        }
        catch (IOException e){
            closeConnection();
        }
    }

    /**
     * Send our username and the port we use for our UDP socket to the server upon connection
     */
    public void enterUsername(){
        try{
            writer.write(username + "\"" + listenPort);
            writer.newLine();
            writer.flush();
        }
        catch (IOException e){
            closeConnection();
        }
    }

    /**
     * Sends a message to the server. Newline since server relies on readLine, and we send by flushing the buffer
     */
    public void sendMessage(String message){
        try{
            writer.write(username + ": " + message);
            writer.newLine();
            writer.flush();
        }
        catch (IOException e){
            closeConnection();
        }
    }

    /**
     * Creates a Runnable object that listens for incoming messages while the socket is open.
     * Updates the GUI components on its own thread by using Platform.runLater()
     * @param controller The controller class where we GUI components are to be modified
     * @return A Runnable to be used with an executor service
     */
    public Runnable listenForMessage(ChatController controller){
        return () -> {
            while (!socket.isClosed()) {
                try {
                    String message = reader.readLine();
                    if (message.contains("$¤")) {   //We are receiving channel info from the server
                        Platform.runLater(() -> controller.updateChannelList(Util.channelsToArray(message)));
                    } else {
                        Platform.runLater(() -> controller.updateMessages(message));
                    }
                } catch (IOException e) {
                    closeConnection();
                }
            }
        };
    }

    /**
     * Closes all open resources. Wrapped streams closes automatically.
     */
    @SuppressWarnings("Duplicates")
    public void closeConnection(){
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
            if(UDPSocket != null){
                UDPSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Send a request to join a channel to the server. Based on index in the list
     */
    public void joinRequest(int index) throws IOException {
        writer.write("$#" + index);
        writer.newLine();
        writer.flush();
    }

    public DatagramSocket getUDPSocket() {
        return UDPSocket;
    }
}
