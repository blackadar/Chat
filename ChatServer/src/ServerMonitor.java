import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 3/19/17.
 */
public class ServerMonitor extends JFrame implements Runnable{
    private JTextArea serverLog;
    private JPanel panel;
    private JTextField inputField;
    private JButton sendButton;
    private ServerSocket server;


    public ServerMonitor (int port) throws IOException {
        super("Chat Server");
        setContentPane(panel);
        this.setPreferredSize(new Dimension(600,400));
        serverLog.setLineWrap(true);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        server = new ServerSocket(port);
        serverLog.append("IP: " + InetAddress.getLocalHost() + "\n");
        this.run();
    }

    public void run(){
        serverLog.append("Starting on Port " + server.getLocalPort() + "\n");
        while (true) {
            serverLog.append("Status: 0 (Awaiting Connection Requests)" + "\n");
            Socket client = null;
            try {
                client = server.accept();
            } catch (IOException e) {
                serverLog.append(e.getStackTrace().toString());
            }
            serverLog.append("Status: 1 (Initializing Connection) to " + client.getInetAddress() + "\n");
            ChatHandler c = null;
            try {
                c = new ChatHandler(client);
            } catch (IOException e) {
                serverLog.append(e.getStackTrace().toString() + "\n");
            }
            Thread t = new Thread(c);
            t.start();
        }
    }

    public static void main(String[] args){
        try {
            ServerMonitor monitor = new ServerMonitor(9090);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( null, "A General I/O Exception was Detected.", "Network Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
