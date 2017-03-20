import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 3/19/17.
 */
public class ServerMonitor extends JFrame implements Runnable, ChatListener{
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
        serverLog.append("Local IP: " + InetAddress.getLocalHost() + "\n");
        this.run();
    }

    public void run(){
        serverLog.append("Listening on Port " + server.getLocalPort() + ".\n");
        while (true) {
            try {
                Socket client = server.accept();
                ChatHandler c = new ChatHandler(client);
                c.addListener(this);
                Thread t = new Thread(c);
                t.start();
            } catch (IOException e) {
                serverLog.append(e.getStackTrace().toString() + "\n");
            }
        }
    }

    public static void main(String[] args){
        try {
            new ServerMonitor(9090);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( null, "A General Exception was Detected.", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void clientDisconnected(String userName) {
        serverLog.append("Lost connection to " + userName + ".\n");
    }

    @Override
    public void clientConnected(String userName) {
        serverLog.append("Initializing Connection to " + userName + ".\n");
    }

    @Override
    public void clientChangedName(String old, String updated) {
        serverLog.append("Client " + old + " changed alias to " + updated + ".\n");
    }
}
