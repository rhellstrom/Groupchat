package Chatclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Util {
    public static InetAddress stringToIP(String IPText) throws UnknownHostException {
        return InetAddress.getByName(IPText);
    }
    public static int stringToPort(String port){
        return Integer.parseInt(port);
    }
    public static String[] channelsToArray(String channelString){
        return channelString.substring(2).split("\"");
    }
}
