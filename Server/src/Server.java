import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * GUI for Server interface
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */
public class Server extends JFrame implements Runnable, ClientActionListener {
    private UserData userList = new UserData();
    File savedUserList = new File("Users.svs");

    private JTextArea serverLog;
    private JPanel panel;
    private JLabel numberOnlineLabel;
    private ServerSocket server;
    protected int numberOnline = 0;


    public Server(int port) throws IOException {
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

        //Load username list from file.
        userList = loadUserData();

        this.run();
    }

    public void run(){
        output("Listening on Port " + server.getLocalPort() + ".");
        updateLabel();
        while(true) {
            try {
                Socket client = server.accept();
                ClientListener c = new ClientListener(client, "METADATA USERNAME");
                c.addListener(this);
                Thread t = new Thread(c);
                t.start();
                userList.users.add((new UserData()).instantiateUser(false, "h"));
                writeUserData(userList);
            } catch (IOException e) {
                output(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public static void main(String[] args){
        try {
            new Server(9090);
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
        output("ClientListener " + old + " changed alias to " + updated + ".");
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

    /**
     * Loads the list of username and statuses(UserData objects) into memory
     * @return UserData[] An array of all UserData objects in the list
     */
    private UserData loadUserData(){
        ObjectInputStream in = null;
        UserData loaded = null;
        if(!savedUserList.exists()){
            System.err.println("Cannot load list, does not exist...");
            return new UserData();
        } else {
            try {
                in = new ObjectInputStream(new FileInputStream(savedUserList));
                loaded = (UserData) in.readObject();
            } catch (IOException e){
                System.err.println("Could not open stream to read usernames into memory");
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                System.err.println("Could not load UserData object from file");
            }
            return loaded;
        }
    }

    private void writeUserData(UserData toSave){
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(savedUserList));
            out.writeObject(toSave);
        } catch (IOException e){
            System.err.println("Could not open stream to write usernames into memory");
        }
    }

    /**
    Contains the server's knowledge of each user who has connected previously.
     Will be written to file to persist indefinitely between multiple server launches.
     **/
    private class UserData implements Serializable{
       ArrayList<User> users = new ArrayList<>();

        private class User implements Serializable{
            boolean isMod;
            String userName;

            public User(boolean isMod, String userName){
                this.isMod = isMod;
                this.userName = userName;
            }
        }

        public User instantiateUser(boolean isMod, String userName){
            return new User(isMod, userName);
        }
    }

}
