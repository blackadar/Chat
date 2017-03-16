

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jordan Blackadar as a part of the Networking package in ScratchPad.
 *
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/16/2017 : 3:39 PM
 */
public class ChatServer {
    public ChatServer (int port) throws IOException {
        ServerSocket server = new ServerSocket (port);
        while (true) {
            System.out.println("Awaiting client...");
            Socket client = server.accept();
            System.out.println ("Accepted from " + client.getInetAddress ());
            ChatHandler c = new ChatHandler(client);
            Thread t = new Thread(c);
            t.start();
        }
    }
    public static void main (String args[]) throws IOException {
        new ChatServer (9090);
    }
}
