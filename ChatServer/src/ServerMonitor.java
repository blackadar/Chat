import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * GUI for Server interface
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */
public class ServerMonitor extends JFrame implements Runnable, ChatListener{
    private JTextArea serverLog;
    private JPanel panel;
    private JLabel numberOnlineLabel;
    private ServerSocket server;
    protected int numberOnline = 0;


    public ServerMonitor (int port) throws IOException {
        super("Chat Server");
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png"));
        this.setIconImage(image);
        setContentPane(panel);
        this.setPreferredSize(new Dimension(600,400));
        serverLog.setLineWrap(true);
        updateLabel();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        server = new ServerSocket(port);
        output("Local IP: " + InetAddress.getLocalHost());
        this.run();
    }

    public void run(){
        output("Listening on Port " + server.getLocalPort() + ".");
        updateLabel();
        while(true) {
            try {
                Socket client = server.accept();
                ChatHandler c = new ChatHandler(client);
                c.addListener(this);
                Thread t = new Thread(c);
                t.start();
            } catch (IOException e) {
                output(Arrays.toString(e.getStackTrace()));
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
        numberOnline--;
        updateLabel();
        output("Lost connection to " + userName + ".");
    }

    @Override
    public void clientConnected(String userName) {
        numberOnline++;
        updateLabel();
        output("Initializing Connection to " + userName);
    }

    @Override
    public void clientChangedName(String old, String updated) {
        output("Client " + old + " changed alias to " + updated + ".");
    }

    private void updateLabel(){
        numberOnlineLabel.setText("Online: " + numberOnline);
        serverLog.setCaretPosition(serverLog.getDocument().getLength());
    }

    private void output(String toOutput){
        serverLog.append(toOutput + "\n");
        System.out.println(toOutput);
        updateLabel();
    }
}
