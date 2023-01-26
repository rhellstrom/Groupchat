package Chatclient;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Class to open microphone line, record and send packages
 */

public class RecorderThread implements Runnable{
    private final TargetDataLine mic; //Mic line to application
    private final byte[] buffer;  //Buffer for the audio. To be put in datagram packet
    private final DatagramSocket sendSocket;
    private final InetAddress serverIP;
    private final int port;
    private boolean active;

    /**
     * Set up a UDP socket for sending packages
     * Initialize buffer and open a mic line
     */
    public RecorderThread(int bufferSize, InetAddress serverIP, int port, DatagramSocket socket) throws LineUnavailableException, SocketException {
        this.buffer = new byte[bufferSize];
        this.serverIP = serverIP;
        this.port = port;
        this.sendSocket = socket;

        AudioFormat audioFormat = new AudioFormat(44100f, 16, 1, true, true);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null); //Various info for getMixer
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(audioFormat);
        mic.start();
    }

    public void stop() {
        this.active = false;
    }

    /**
     * Open mic, record packages we pack into datagram-packet and send. Repeat until we release hotkey or something
     */
    @Override
    public void run() {
        active = true;
        while(active){
            System.out.println(mic.read(buffer, 0, buffer.length)); //Reads input until buffer is full
            DatagramPacket voicePackage = new DatagramPacket(buffer, buffer.length, serverIP, port + 1); //Server UDP port is always 1 above TCP port
            try {
                sendSocket.send(voicePackage); //And ship it
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Close our open mic line
     */
    public void closeMic(){
        mic.close();
    }
}