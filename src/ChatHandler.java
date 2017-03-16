
import java.net.*;
import java.io.*;
import java.util.*;
/**
 * Created by Jordan Blackadar as a part of the Networking package in ScratchPad.
 *
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/16/2017 : 3:41 PM
 */
public class ChatHandler implements Runnable {

    protected Socket s;
    protected DataInputStream i;
    protected DataOutputStream o;

    public ChatHandler (Socket s) throws IOException {
        System.out.println("Creating a Chat Handler");
        this.s = s;
        this.i = new DataInputStream (new BufferedInputStream (s.getInputStream()));
        this.o = new DataOutputStream (new BufferedOutputStream (s.getOutputStream()));
    }
    protected static ArrayList<ChatHandler> handlers = new ArrayList<>();
    public void run() {
        try {
            handlers.add(this);
            while (true) {
                String msg = s.getInetAddress() + ": " + i.readUTF ();
                broadcast(msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace ();
        } finally {
            handlers.remove(this);
            try {
                s.close ();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void start(){
        System.out.println("Starting a thread.");
    }

    public void stop(){
        System.out.println("Stopping a thread.");
    }

    protected static void broadcast (String message) {
        synchronized (handlers) {
            Enumeration e = Collections.enumeration(handlers);
            while (e.hasMoreElements ()) {
                ChatHandler c = (ChatHandler) e.nextElement ();
                try {
                    synchronized (c.o) {
                        c.o.writeUTF (message);
                    }
                    c.o.flush ();
                } catch (IOException ex) {
                    c.stop();
                }
            }
        }
    }
}
