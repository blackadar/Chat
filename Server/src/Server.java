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
 * @author Liam Brown<"liamnb525@gmail.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */
public class Server extends JFrame implements Runnable, ClientActionListener {
    public static final String SERVER_ERR_LBL = "<<ERROR>> ";
    private JTextArea serverLog;
    private JPanel panel;
    private JLabel numberOnlineLabel;
    private ServerSocket server;
    protected int numberOnline = 0;
    protected File save = new File("save.svs");
    protected Save currentSave;


    public Server(int port) throws IOException {
        super("Chat Server");
        initGui(); //Set up GUI
        server = new ServerSocket(port); //Create socket to listen for connections
        output("Local IP: " + InetAddress.getLocalHost());  //Output the local IP
        initSaveFile(); //Initialize the save file, creating it if it does not exist
        this.run(); //Begin server operation
    }

    /**
     * Main loop for running server.  Waits for connections, upon receiving them creates a new ClientListener thread.
     * @throws
     */
    public void run() {
        output("Listening on Port " + server.getLocalPort() + ".");
        updateLabel();
        while (true) { //Continuously look for and accept new incoming connections
            try {
                Socket client = server.accept(); //Block until a connection is attempted

                //Create a client listener object to handle new user
                ClientListener pending_user = new ClientListener(client, this);
                pending_user.addListener(this); //Assign this server as the listener for new client

                //Check if the user is in the saved list of users
                currentSave.addIfMissing(pending_user.myUser);
                Save.preserve(currentSave); //Save the user list

                //Create and run client listener thread
                Thread t = new Thread(pending_user);
                t.start();
            } catch (IOException e) {
                System.err.println("Could not store user data file");
                e.printStackTrace();
                output(SERVER_ERR_LBL + "Could not store user data file");
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server(9090); //Create serve and set port
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

    private void initGui(){
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png"));
        this.setIconImage(image);
        setContentPane(panel);
        this.setPreferredSize(new Dimension(600, 400));
        serverLog.setLineWrap(true);
        updateLabel();
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void initSaveFile () throws IOException{
        if(save.exists()){
            try {
                currentSave = Save.revive();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            currentSave = new Save();
        }
    }
}