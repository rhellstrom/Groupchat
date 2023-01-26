package Chatclient;

import javax.sound.sampled.SourceDataLine;
import java.net.DatagramSocket;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Thread that listens to new packages from the UDP socket and plays them
 */
public class PlayerThread implements Runnable{
    private final DatagramSocket receiveSocket;
    private final SourceDataLine speaker;
    private final byte[] buffer = new byte[1024];
    private boolean active;

    /**
     * Initialize socket and set up audio output
     */
    public PlayerThread(DatagramSocket socket) throws LineUnavailableException, SocketException {
        this.receiveSocket = socket;

        AudioFormat audioformat = new AudioFormat(44100f, 16, 1, true, true);
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, audioformat);
        speaker = (SourceDataLine) AudioSystem.getLine(lineInfo);
        speaker.open(audioformat);
    }

    /**
     * Separate classes for receiving packages and playing them might fix audio issues
     */
    @Override
    public void run() {
        DatagramPacket packetBuffer = new DatagramPacket(buffer, 0, buffer.length);
        active = true;
        while(active){
            try {
                receiveSocket.receive(packetBuffer);
                speaker.start();
                speaker.write(packetBuffer.getData(), 0, packetBuffer.getLength());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        active = false;
    }

    /**
     * Close our open speaker line
     */
    public void closeSpeaker(){
        speaker.close();
    }
}