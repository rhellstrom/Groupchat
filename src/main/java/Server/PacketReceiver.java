package Server;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

/**
 * Class that listens for incoming voice packages and creates an instance of PacketSender to deal with them
 */
public class PacketReceiver implements Runnable{
    private final DatagramSocket receiverSocket;
    private final ExecutorService executorService;

    public PacketReceiver(int port, ExecutorService exec) throws SocketException {
        this.receiverSocket = new DatagramSocket(port);
        this.executorService = exec;
    }

    @Override
    public void run() {
        byte[] receiverBuffer = new byte[44100]; //Size makes no sense for now, experiment
        DatagramPacket receiverPacket = new DatagramPacket(receiverBuffer, receiverBuffer.length);

        while(true){
            try {
                receiverSocket.receive(receiverPacket);
                System.out.println("Received UDP package from " + receiverPacket.getAddress().getHostAddress() + " " + receiverPacket.getPort());
                executorService.submit(new PacketSender(receiverPacket));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
