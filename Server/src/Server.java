import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jordan Blackadar as a part of the Server package in Chat.
 * GUI for Server interface
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @author Liam Brown<"liamnb525@gmail.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */
public class Server extends JFrame implements Runnable, ClientActionListener {
    public String name = "Beta Chat Server";
    public static final String SERVER_ERR_LBL = "<<ERROR>> ";
    private JTextArea serverLog;
    private JPanel panel;
    private JLabel numberOnlineLabel;
    private JTextField AdminField;
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
                if(pending_user.myUser.blacklist == true){ //Ensure that user is not banned before starting a listener
                    output("Banned user " + pending_user.myUser.userName + " attempted to connect to the server");

                    //Ignore the connection attempt before starting new ClientListener thread
                    pending_user = null;
                    continue;
                }
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
            } catch (ClassNotFoundException e) {
                System.err.println("Client did not properly initialize connection");
                e.printStackTrace();
                output(SERVER_ERR_LBL + "Client did not properly initialize connection");
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server(9090); //Create server and set port
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "A General Exception was Detected.", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void clientDisconnected(String userName, String address) {
        numberOnline--;
        updateLabel();
        output("Lost connection to " + userName + " at " + address);
    }

    @Override
    public void clientConnected(String userName, String address) {
        numberOnline++;
        updateLabel();
        output("Initializing Connection to " + userName + " at " + address);
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
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screen_size.getWidth();
        int height = (int) screen_size.getHeight();

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png"));
        this.setIconImage(image);
        setContentPane(panel);
        this.setPreferredSize(new Dimension(width / 2, height / 2));
        numberOnlineLabel.setFont(new Font("Sans Serif", Font.PLAIN, height / 72));
        serverLog.setLineWrap(true);
        serverLog.setFont(new Font("Sans Serif", Font.PLAIN, height / 72));
        updateLabel();
        this.setLocationRelativeTo(null);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Initialize administrator command field
        AdminField.setFont(new Font("Sans Serif", Font.PLAIN, height / 72));
        AdminField.setSize(new Dimension(-1, height / 14));
        AdminField.setText("Administrator command area");
        AdminField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                AdminField.setText("");
            }
        });
        AdminField.addActionListener(actionEvent -> {
                Message current = new Message(AdminField.getText());
                executeAdminCommand(current.contents);
                AdminField.setText("");
        });

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

    private void executeAdminCommand(String input) {
        String [] cmd = input.substring(1).split(" ");
        switch(cmd[0]){
            case "mod":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        output("Modded: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).isMod = true;
                        break;
                    }
                }
                break;
            case "unmod":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        output("Unmodded: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).isMod = false;
                        break;
                    }
                }
                break;
            case "ban":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        output("Banned: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).blacklist = true;
                        break;
                    }
                }
                break;
            case "unban":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        output("Unbanned: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).blacklist = false;
                        break;
                    }
                }
                break;
            default:
                output("Admin command " + cmd[1] + " attempted does not exist.");
                break;
        }
    }
}