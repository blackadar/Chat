import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * Manages String input from Sockets
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/17/2017 : 3:05 PM
 */

public class ClientListener implements Runnable {

    protected Socket socket;
    protected ObjectInputStream inputStream;
    protected ObjectOutputStream outputStream;
    protected User myUser;
    protected boolean isAFK;

    protected static ArrayList<ClientListener> all = new ArrayList<>();
    protected ArrayList<ClientActionListener> actionListeners = new ArrayList<>();
    protected MetaData clientMetaData;
    protected Server runningServer;

    public ClientListener(Socket socket, Server theServer) throws IOException, ClassNotFoundException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush(); //Necessary to avoid 'chicken or egg' situation
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        outputStream.writeObject(new MetaData(theServer.name, null)); //First, send Server MetaData. TODO: Send list of rooms
        this.clientMetaData = (MetaData)inputStream.readObject(); //Then, read the client MetaData
        this.runningServer = theServer;
        this.isAFK = false;
        initializeSave(); //Recovers Save or Creates One
        tellAll(myUser.userName + " is online.");
        tell("Welcome to the Chat Server.");
        tell("For a full list of commands, type /help .");
    }

    /**
     * Looping Logic for a ClientListener thread. Handles input and commands.
     */
    public void run(){
        try {
            all.add(this);
            for(ClientActionListener x : actionListeners){
                x.clientConnected(myUser.userName, socket.getInetAddress().toString());
            }
            alert("Welcome!");
            while (true) {
                Message received =(Message)inputStream.readObject();
                if(myUser.blacklist) throw new InvalidObjectException("Banned."); //If banned, client will send ackgnowldegment, stop
                if(!(received.contents.isEmpty())) {
                    if (hasCommand(received.contents)) {
                        executeCommand(received.contents);
                    } else if (isAFK) {
                        tellAll(myUser.userName + " is no longer AFK.");
                        isAFK = false;
                        broadcast(myUser.userName + " : " + received.contents);
                    } else {
                        broadcast(myUser.userName + " : " + received.contents);
                    }
                }
            }
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            this.stop();
        }
    }


    /**
     * Finalizes client stream and announces departure
     */
    public void stop(){
        myUser.online = false;
        all.remove(this);
        for(ClientActionListener x : actionListeners){
            x.clientDisconnected(myUser.userName, socket.getInetAddress().toString());
        }
        tellAll(myUser.userName + " is offline.");
        try {
            socket.close ();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a boolean indicating if a String starts with a slash, to execute
     * @param input The String to Analyze
     * @return boolean for containing a command
     */
    protected static boolean hasCommand(String input){
        if(input.charAt(0) == '/' || input.charAt(0) == '\\') {
            return true;
        }
        else return false;
    }

    /**
     * Analyze command, and take corresponding action
     * @param input The String command starting with a /
     */
    protected void executeCommand(String input) {
        input = input.substring(1, input.length()); //Remove beginning slash
        String[] commandParts = input.split(" "); //Split into parts on spaces
        String[] arguments = new String[commandParts.length - 1]; //Take args after command

        for (int x = 1; x < commandParts.length; x++) { //Fill arguments
            arguments[x - 1] = commandParts[x];
        }

        try {
            switch (commandParts[0].toLowerCase()) {
                case ("help"): { //Sends help documentation
                    tell("List of Available Commands: \n 1.  /name [name] : Modifies your server-wide alias. \n 2.  /afk : Notifies others that you are away. \n 3.  /list : Lists all online users. \n 4.  /me [phrase] : (Ex. /me does something = [name] does something)");
                }
                break;
                case ("hack"): { //Hacks
                    tellAll("Oh no I've been hacked by " + myUser.userName + "!");
                }
                break;
                case ("name"): { //Changes userName, checking for dupes
                    String name = "";
                    for (int i = 0; i < arguments.length; i++) {
                        if(i > 0) name += arguments[i] + " ";
                        else name += arguments[i];
                    }
                    if (name.isEmpty()) throw new IllegalArgumentException("Command Invalid: /name [user name]");
                    for (ClientListener c : all) {
                        if (name.equalsIgnoreCase(c.myUser.userName))
                            throw new IllegalArgumentException("Username already exists.");
                        else if(name.equalsIgnoreCase("server"))
                            throw new IllegalArgumentException("Cannot be named server.");
                    }

                    setUserName(name);
                    tell("Username changed to " + myUser.userName);
                }
                break;

                case ("afk"): { //Toggles isAFK and broadcasts state
                    if (this.isAFK) {
                        tellAll(myUser.userName + " is no longer AFK");
                        this.isAFK = false;
                    } else {
                        tellAll(myUser.userName + " is now AFK");
                        this.isAFK = true;
                    }
                }
                break;

                case ("list"): { // Sends list of all online users
                    String online = "";
                    for (ClientListener x : all) {
                        online += x.myUser.userName + ", ";
                    }
                    tell("Online: " + online);
                }
                break;

                case ("me"): { //Announces User-Provided State
                    if (arguments.length == 0) throw new IllegalArgumentException("Command requires parameters.");
                    tellAll(myUser.userName + " " + input.substring(input.indexOf(" ") + 1, input.length()) + ".");
                }
                break;

                case("shrug"): { //Shrugs
                    broadcast(myUser.userName + " : ¯\\_(ツ)_/¯ ");
                }
                break;

                case("clear"): { //Clears client terminal
                    command("clear");
                }
                break;

                default: { //Catches unrecognized commands
                    throw new IllegalArgumentException("Unrecognized command. Use /help for a list of all commands.");
                }
            }
        }
        catch(IllegalArgumentException e){
            tell("Server Error: " + e.getMessage());
        }
    }

    /**
     * Adds an ActionListener to the list of all listeners
     * @param toAdd the ActionListener to add to the list
     */
    public void addListener(ClientActionListener toAdd) {
        actionListeners.add(toAdd);
    }

    /**
     * Sends a message to /all, with no prefixes
     * @param message The String to send
     */
    protected static void broadcast(String message) {
        synchronized(all) {
            Enumeration allHandlers = Collections.enumeration(all);
            while (allHandlers.hasMoreElements ()) {
                ClientListener currentClientListener = (ClientListener) allHandlers.nextElement();
                try {
                    synchronized(currentClientListener.outputStream) {
                        currentClientListener.outputStream.writeObject(new Message(message));
                    }
                    currentClientListener.outputStream.flush();
                } catch (IOException e) {
                    currentClientListener.stop();
                }
            }
        }
    }

    /**
     * Sends a message to this specific client, with the Server: prefix.
     * @param message The message to display as Server
     */
    protected void tell(String message){
        try {
            synchronized(this.outputStream) {
                this.outputStream.writeObject(new Message("Server : " + message));
            }
            this.outputStream.flush();
        } catch (IOException e) {
            this.stop();
        }
    }

    /**
     * Sends a message to /all, with the Server: prefix.
     * @param message The message to broadcast as Server
     */
    protected static void tellAll(String message){
        broadcast("Server : " + message);
    }

    /**
     * Sends a command-marked message to a client to trigger an action
     */
    protected void command(String command){
        try {
            synchronized(this.outputStream) {
                this.outputStream.writeObject(new Message(command, null, true));
            }
            this.outputStream.flush();
        } catch (IOException e) {
            this.stop();
        }
    }

    protected void alert(String alertMessage){
        try {
            synchronized(this.outputStream) {
                this.outputStream.writeObject(new Message("alert", true, alertMessage));
            }
            this.outputStream.flush();
        } catch (IOException e) {
            this.stop();
        }
    }

    /**
     * Changes a ClientListener's name as it appears in chat.
     */
    public void setUserName(String name){
        //TODO: Update to work with new naming convention
        for(ClientActionListener x : actionListeners){
            x.clientChangedName(myUser.userName, name);
        }
        this.myUser.userName = name;
    }

    /**
     * Changes a user's status to Moderator
     */
    public void setIsMod(){
        this.myUser.setMod(true);
    }

    /**
     * Initializes a user's saved state. If it exists, load it, otherwise create it.
     */
    private void initializeSave(){
        boolean savedUser = false;
        for(User temp : runningServer.currentSave.all){
            if(temp.userName.equals(clientMetaData.handle)){
                myUser = temp;
                savedUser = true;
            }
        }
        System.out.println(clientMetaData.handle);
        if(savedUser == false) myUser = new User(clientMetaData.handle, false, false);
    }
}