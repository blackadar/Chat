import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Server implements ClientActionListener, Runnable{

    public String name;
    private static final String SERVER_ERR_LBL = "Error: ";
    private ServerSocket server;
    private int numberOnline = 0;

    private File save = new File("save.svs");
    Save currentSave;
    private File preferences = new File("prefs.reg");
    public Preferences loaded_prefs;

    private ArrayList<ServerActionListener> listeners = new ArrayList<>();

    public Server(int port) throws IOException {
        server = new ServerSocket(port); //Create socket to listen for connections
        initSaveFile(); //Initialize the save file, creating it if it does not exist
        if(preferences.exists()) readPrefs();
        else initPrefs();
    }

    /**
     * Main loop for running server.  Waits for connections, upon receiving them creates a new ClientListener thread.
     */
    public void run() {
        try {
            output("Local IP: " + InetAddress.getLocalHost());  //Output the local IP
        } catch (UnknownHostException e) {
            e.printStackTrace(); //Fatal Error
        }
        output("Listening on Port " + server.getLocalPort() + ".");
        while (true) { //Continuously look for and accept new incoming connections
            try {
                Socket client = server.accept(); //Block until a connection is attempted

                //Create a client listener object to handle new user
                ClientListener pendingUser = new ClientListener(client, this);
                if(pendingUser.client.blacklist){ //Ensure that user is not banned before starting a listener
                    output("Banned user " + pendingUser.client.userName + " attempted to connect to the server");

                    //Ignore the connection attempt before starting new ClientListener thread
                    pendingUser = null;
                    continue;
                }
                pendingUser.addListener(this); //Assign this server as the listener for new client

                //Check if the user is in the saved list of users
                currentSave.recognize(pendingUser.client);
                Save.write(currentSave); //Save the user list
                pendingUser.client.online = true;

                //Create and run client listener thread
                Thread t = new Thread(pendingUser);
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

    protected void output(String toOutput) {
        for(ServerActionListener x : listeners){
            x.output(toOutput);
        }
    }

    void addActionListener(ServerActionListener toAdd){
        listeners.add(toAdd);
    }

    public void executeAdminCommand(String input) {
        String [] cmd = input.substring(1).split(" ");
        boolean exists = false;
        switch(cmd[0]){
            case "alert":
                //Send user a command-alert Message
                output("Not yet implemented.");
                break;
            case "mod":

                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])) {
                        exists = true;
                        output("Modded: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).isMod = true;
                        break;
                    }
                }
                if(!exists) output("Failed to unmod nonexistent user " + cmd[1]);
                break;
            case "unmod":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        exists = true;
                        output("Unmodded: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).isMod = false;
                        break;
                    }
                }
                if(!exists) output("Failed to ban nonexistent user " +cmd[1]);
                break;
            case "ban":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        exists = true;
                        output("Banned: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).blacklist = true;
                        break;
                    }
                }
                if(!exists) output("Failed to ban nonexistent user " + cmd[1]);
                break;
            case "unban":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        exists = true;
                        output("Unbanned: " + currentSave.all.get(c).userName);
                        currentSave.all.get(c).blacklist = false;
                        break;
                    }
                }
                if(!exists) output("Failed to unban nonexistent user " + cmd[1]);
                break;
            case "userinfo":
                for(int c = 0; c < currentSave.all.size(); c++){
                    if(currentSave.all.get(c).userName.equals(cmd[1])){
                        exists = true;
                        output(currentSave.all.get(c).toString());
                        break;
                    }
                }
                if(!exists) output("Failed to print user info for nonexistent user " + cmd[1]);
                break;
            default:
                output("Admin command " + cmd[1] + " attempted does not exist.");
                break;
        }
    }

    @Override
    public void clientDisconnected(String userName, String address) {
        numberOnline--;
        output("Lost connection to " + userName + " at " + address);
        for(ServerActionListener x : listeners){x.lostClient();}
    }

    @Override
    public void clientConnected(String userName, String address) {
        numberOnline++;
        output("Initializing Connection to " + userName + " at " + address);
        for(ServerActionListener x : listeners){x.addedClient();}
    }

    @Override
    public void clientChangedName(String old, String updated) {
        output("ClientListener " + old + " changed alias to " + updated + ".");
    }

    private void initSaveFile () throws IOException{
        if(save.exists()){
            try {
                currentSave = Save.read();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            currentSave = new Save();
        }
    }

    /**
     * Initializes preferences object and writes it to file preferences.sprefs with some default settings and values.
     */
    private void initPrefs(){
        try {
            loaded_prefs = new Preferences();
            loaded_prefs.addPreference("view_chat" , "disabled");
            loaded_prefs.addPreference("color_scheme" , "default");
            loaded_prefs.addPreference("reject_connections" , "disabled");
            loaded_prefs.addPreference("require_password" , "disabled");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(preferences));
            out.writeObject(loaded_prefs);
            out.flush();
            out.close();

        } catch (Exception e) {
            output("Failed to initialize preferences file");
        }
    }

    /**
     * Reads the preferences file into a preferences object.
     */
    private void readPrefs(){
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(preferences));
            loaded_prefs = (Preferences) in.readObject();
            in.close();
        } catch (Exception e) {
            output("Failed to read preferences file");
        }
    }

    /**
     * Writes the preferences object into the preferences.sprefs file.
     */
    private void writePrefs(){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(preferences));
            out.writeObject(loaded_prefs);
            out.flush();
            out.close();
        } catch (Exception e) {
            output("Failed to write preferences file");
        }
    }
}
