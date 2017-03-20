import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * Manages threads of ChatHandlers
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/17/2017 : 3:06 PM
 */
public class ChatServer {

    protected ArrayList<Thread> threads = new ArrayList<>();

    public ChatServer (int port) throws IOException {
        ServerSocket server = new ServerSocket (port);
        System.out.println("IP: " + InetAddress.getLocalHost());
        while (true) {
            System.out.println("Status: 0 (Awaiting Connection Requests)");
            Socket client = server.accept();
            System.out.println ("Status: 1 (Initializing Connection) to " + client.getInetAddress());
            ChatHandler c = new ChatHandler(client);
            Thread t = new Thread(c);
            threads.add(t);
            t.start();
        }
    }
    public static void main (String args[]) throws IOException {
        new ChatServer (9090);
    }
}