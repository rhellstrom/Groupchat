package Chatclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javax.net.ssl.SSLSocketFactory;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatController {
    private Client client;
    private RecorderThread recThread;
    private PlayerThread playerThread;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final ObservableList<String> channels = FXCollections.observableArrayList();
    @FXML
    private Button connectButton = new Button();
    @FXML
    private Button recordButton = new Button();
    @FXML
    private TextField ipField = new TextField();
    @FXML
    private TextField portField = new TextField();
    @FXML
    private TextField nameField = new TextField();
    @FXML
    public ListView<String> channelList = new ListView<>();
    @FXML
    public ListView<String> messageList = new ListView<>();
    @FXML
    public TextField messageField = new TextField();
    @FXML
    private TextField listenField = new TextField();

    public void initialize(){
        setSysProperties();
        initChannelList();
        messageList.setItems(messages);

        //To speed things up when testing
        ipField.setText("127.0.0.1");
        portField.setText("60000");
        listenField.setText("6666");
    }

    /**
     * Method for the connect/disconnect button
     */
    public void connectAction() {
        if(connectButton.getText().equals("Connect")){
            connect();
            connectButton.setText("Disconnect");
        }
        else{
            disconnect();
            connectButton.setText("Connect");
            channels.clear();
        }
    }

    /**
     * Create SSL connection and an instance of Client to manage the connection.
     * Start a playerThread waiting for voice packages as well as a thread for listening for TCP messages
     */
    private void connect(){
        try{
            Socket socket = SSLSocketFactory.getDefault().createSocket(getIP(), getPort());
            client = new Client(socket, nameField.getText(), getListenPort());
            client.enterUsername();

            playerThread = new PlayerThread(client.getUDPSocket());
            recThread = new RecorderThread(1024, getIP(), getPort(), client.getUDPSocket());
            executorService.submit(playerThread);
            executorService.submit(client.listenForMessage(this));
            updateMessages("Connected to server");
        }
        catch (ConnectException e){
            System.out.println("Could not connect to server");
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves String from textField and sends it to server
     */
    @FXML
    private void getMessage() {
        String message = messageField.getText();
        messageField.clear();
        client.sendMessage(message);
        updateMessages("You: " + message);
    }

    public void updateMessages(String message){
        messages.add(message);
    }

    /**
     * Clears and updates the ObservableList
     * @param channelsToAdd Array containing the channel names
     */
    public void updateChannelList(String []channelsToAdd){
        channels.clear();
        channels.addAll(channelsToAdd);
    }

    private void disconnect() {
        messages.clear();
        playerThread.stop();
        client.closeConnection();
        recThread.closeMic();
        playerThread.closeSpeaker();
        recThread = null;
        playerThread = null;
    }

    @FXML
    private void record(){
        executorService.submit(recThread);
    }

    @FXML
    private void stopRecord(){
        recThread.stop();
    }

    /**
     * Sets the listView to be populated by the ObservableList channels content
     * Adds a listener to request the switching of channels upon selection
     */
    private void initChannelList(){
        channelList.setItems(channels);
        channelList.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            try {
                int index = channelList.getSelectionModel().getSelectedIndex();
                if(index >= 0){
                    client.joinRequest(index);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Point to relevant resources for SSL connection
     */
    public static void setSysProperties(){
        //System.setProperty("javax.net.debug", "all");
        System.setProperty("javax.net.ssl.keyStore", "src/main/resources/keystore/clientKeyStore.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/keystore/clientTrustStore.p12");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }

    /**
     * Fetch IP from GUI
     */
    public InetAddress getIP() throws UnknownHostException {
        return Util.stringToIP(ipField.getText());
    }

    /**
     * Fetch port from GUI
     */
    public int getPort(){
        return Util.stringToPort(portField.getText());
    }

    /**
     * Fetch listen port from GUI
     */
    public int getListenPort(){
        return Util.stringToPort(listenField.getText());
    }
}