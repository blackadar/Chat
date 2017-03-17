import java.net.*;
import java.io.*;
import java.awt.*;

/**
 * Created by Jordan Blackadar as a part of the Networking package in ScratchPad.
 *
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/16/2017 : 4:31 PM
 */

public class ChatClient extends Frame implements Runnable {
    protected DataInputStream i;
    protected DataOutputStream o;
    protected TextArea output;
    protected TextField input;
    protected Thread listener;
    public ChatClient (String title, InputStream i, OutputStream o) {
        super (title);
        this.i = new DataInputStream (new BufferedInputStream (i));
        this.o = new DataOutputStream (new BufferedOutputStream (o));
        setLayout (new BorderLayout ());
        add ("Center", output = new TextArea ());
        output.setEditable (false);
        add ("South", input = new TextField ());
        pack ();
        show ();
        input.requestFocus ();
        listener = new Thread (this);
        listener.start ();
        output.setBackground(Color.GRAY);
    }
    public void run () {
        try {
            while (true) {
                String line = i.readUTF();
                output.appendText (line + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace ();
        } finally {
            listener = null;
            input.hide ();
            validate ();
            try {
                o.close ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
    }
    public boolean handleEvent (Event e) {
        if ((e.target == input) && (e.id == Event.ACTION_EVENT)) {
            try {
                o.writeUTF ((String) e.arg);
                o.flush ();
            } catch (IOException ex) {
                ex.printStackTrace();
                listener.stop ();
            }
            input.setText ("");
            return true;
        } else if ((e.target == this) && (e.id == Event.WINDOW_DESTROY)) {
            if (listener != null)
                listener.stop ();
            hide ();
            return true;
        }
        return super.handleEvent (e);
    }

    public static void main (String args[]) throws IOException {
        Socket s = new Socket ("localhost", 9090);
        new ChatClient ("Chat " + "192.168.9.142" + ":" + 9090,
                s.getInputStream (), s.getOutputStream ());
    }
}
