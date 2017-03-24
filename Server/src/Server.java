import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jordan Blackadar as a part of the Server package in Chat.
 * GUI for Server interface
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */
public class Server extends JFrame implements Runnable, ClientActionListener {
    private JTextArea serverLog;
    private JPanel panel;
    private JLabel numberOnlineLabel;
    private ServerSocket server;
    protected int numberOnline = 0;
    protected File save = new File("save.svs");
    public Save currentSave;


    public Server(int port) throws IOException {
        super("Chat Server");
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png"));
        this.setIconImage(image);
        setContentPane(panel);
        this.setPreferredSize(new Dimension(600, 400));
        serverLog.setLineWrap(true);
        updateLabel();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        server = new ServerSocket(port);
        output("Local IP: " + InetAddress.getLocalHost());
        if(save.exists()){
            try {
                currentSave = Save.revive();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            currentSave = new Save();
        }
        this.run();
    }

    public void run() {
        output("Listening on Port " + server.getLocalPort() + ".");
        updateLabel();
        while (true) {
            try {
                Socket client = server.accept();
                ClientListener pending_user = new ClientListener(client, this);
                pending_user.addListener(this);
                System.out.println("Is mod: " + pending_user.myUser.isMod);
                currentSave.addIfMissing(pending_user.myUser);
                Save.preserve(currentSave);
                Thread t = new Thread(pending_user);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
                output(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server(9090);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "A General Exception was Detected.", e.getMessage(), JOptionPane.ERROR_MESSAGE);
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
        output("ClientListener " + old + " changed alias to " + updated + ".");
    }

    private void updateLabel() {
        numberOnlineLabel.setText("Online: " + numberOnline);
        serverLog.setCaretPosition(serverLog.getDocument().getLength());
    }

    private void output(String toOutput) {
        serverLog.append(toOutput + "\n");
        System.out.println(toOutput);
        updateLabel();
    }
}