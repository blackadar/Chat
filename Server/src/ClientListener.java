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

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    User client;
    private boolean isAFK;

    private static ArrayList<ClientListener> all = new ArrayList<>();
    private ArrayList<ClientActionListener> actionListeners = new ArrayList<>();
    private MetaData clientMetaData;
    private Server runningServer;

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
        tellAll(client.userName + " is online.");
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
                x.clientConnected(client.userName, socket.getInetAddress().toString());
            }
            alert("Welcome!");
            while (true) {
                Message received =(Message)inputStream.readObject();
                if(client.blacklist) throw new InvalidObjectException("Banned."); //If banned, client will send ackgnowldegment, stop
                if(!(received.contents.isEmpty())) {
                    if (hasCommand(received.contents)) {
                        executeCommand(received.contents);
                        if(runningServer.loaded_prefs.readPreference("view_chat").getValue().equals("enabled")) {
                            runningServer.output("<<USER COMMAND - " + client.userName + " : " + received.contents + ">>");
                        }
                    } else if (isAFK) {
                        tellAll(client.userName + " is no longer AFK.");
                        isAFK = false;
                        if(runningServer.loaded_prefs.readPreference("view_chat").getValue().equals("enabled")) {
                            runningServer.output("<<" + client.userName + " : " + received.contents + ">>");
                        }
                        broadcast(client.userName + " : " + received.contents);
                    } else {
                        if(runningServer.loaded_prefs.readPreference("view_chat").getValue().equals("enabled")) {
                            runningServer.output("<<" + client.userName + " : " + received.contents + ">>");
                        }
                        broadcast(client.userName + " : " + received.contents);
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
    private void stop(){
        client.online = false;
        all.remove(this);
        for(ClientActionListener x : actionListeners){
            x.clientDisconnected(client.userName, socket.getInetAddress().toString());
        }
        tellAll(client.userName + " is offline.");
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
    private static boolean hasCommand(String input){
        return input.charAt(0) == '/' || input.charAt(0) == '\\';
    }

    /**
     * Analyze command, and take corresponding action
     * @param input The String command starting with a /
     */
    private void executeCommand(String input) {
        input = input.substring(1, input.length()); //Remove beginning slash
        String[] commandParts = input.split(" "); //Split into parts on spaces
        String[] arguments = new String[commandParts.length - 1]; //Take args after command

        System.arraycopy(commandParts, 1, arguments, 0, commandParts.length - 1);

        try {
            switch (commandParts[0].toLowerCase()) {
                case ("help"): { //Sends help documentation
                        if (arguments[0] != null || arguments[0].equals("1")) {
                            tell("List of Available Commands: \n 1.  /name [name] : Modifies your server-wide alias. " +
                                    "\n 2.  /afk : Notifies others that you are away. \n 3.  /list : Lists all online users." + "" +
                                    "\n 4.  /me [phrase] : (Ex. /me does something = [name] does something)");
                        } else if (arguments[0].equals("2")) {
                            tell("List of Available Commands Page 2: " +
                                    "\n 1. /clear : Clears your screen of all previous messages." +
                                    "\n 2. /whisper [name] [message] : Doesn't work yet...");
                        }

                }
                break;

                case ("hack"): { //Hacks
                    tellAll("Oh no I've been hacked by " + client.userName + "!");
                }
                break;
                case ("name"): { //Changes userName, checking for dupes
                    String name = "";

                    name += arguments[0];

                    if (name.isEmpty()) throw new IllegalArgumentException("Command Invalid: /name [user name]");
                    for (ClientListener c : all) {
                        if (name.equalsIgnoreCase(c.client.userName))
                            throw new IllegalArgumentException("Username already exists.");
                        else if(name.equalsIgnoreCase("server"))
                            throw new IllegalArgumentException("Cannot be named server.");
                    }

                    setUserName(name);
                    tell("Username changed to " + client.userName);
                }
                break;

                case ("afk"): { //Toggles isAFK and broadcasts state
                    if (this.isAFK) {
                        tellAll(client.userName + " is no longer AFK");
                        this.isAFK = false;
                    } else {
                        tellAll(client.userName + " is now AFK");
                        this.isAFK = true;
                    }
                }
                break;

                case ("list"): { // Sends list of all online users
                    String online = "";
                    for (ClientListener x : all) {
                        online += x.client.userName + ", ";
                    }
                    tell("Online: " + online);
                }
                break;

                case ("me"): { //Announces User-Provided State
                    if (arguments.length == 0) throw new IllegalArgumentException("Command requires parameters.");
                    tellAll(client.userName + " " + input.substring(input.indexOf(" ") + 1, input.length()) + ".");
                }
                break;

                case("shrug"): { //Shrugs
                    broadcast(client.userName + " : ¯\\_(ツ)_/¯ ");
                }
                break;

                case("clear"): { //Clears client terminal
                    command("clear");
                }
                break;

                case("whisper"): { // Private message
                    String name = "";
                    String message = "";
                    boolean exists = false;

                    name += arguments[0];

                    for(int i = 1; i < arguments.length; i++) {
                        message += arguments[i] + " ";
                    }

                    if (name.isEmpty()) throw new IllegalArgumentException("Command Invalid: /whisper [username] [message]");
                    if (message.isEmpty()) throw new IllegalArgumentException("Command Invalid: /whisper [username] [message]");

                    for (ClientListener c : all) {
                        if (name.equalsIgnoreCase(c.client.userName)) {
                            exists = true;
                                c.privateMessage(this.client.userName + " : " + message);
                        }
                    }
                    if(exists == false){
                        for(User x : runningServer.currentSave.all){
                            if(x.userName.equalsIgnoreCase(name)){
                                x.pendingMessages.add(new Message(this.client.userName + " : " + message));
                                exists = true;
                            }
                        }
                    }
                        if(exists == false) throw new IllegalArgumentException("Name does not exist.");
                }

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
    void addListener(ClientActionListener toAdd) {
        actionListeners.add(toAdd);
    }

    /**
     * Sends a message to /all, with no prefixes
     * @param message The String to send
     */
    private static void broadcast(String message) {
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
    private void tell(String message){
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
    private static void tellAll(String message){
        broadcast("Server : " + message);
    }

    /**
     * Sends a command-marked message to a client to trigger an action
     */
    private void command(String command){
        try {
            synchronized(this.outputStream) {
                this.outputStream.writeObject(new Message(command, null, true));
            }
            this.outputStream.flush();
        } catch (IOException e) {
            this.stop();
        }
    }

    private void alert(String alertMessage){
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
    private void setUserName(String name){
        //TODO: Update to work with new naming convention
        for(ClientActionListener x : actionListeners){
            x.clientChangedName(client.userName, name);
        }
        this.client.userName = name;
    }

    /**
     * Changes a user's status to Moderator
     */
    public void setIsMod(){
        this.client.setMod(true);
    }

    public void privateMessage(String message){
        try {
            synchronized(this.outputStream) {
                this.outputStream.writeObject(new Message( " : " + message));
            }
            this.outputStream.flush();
        } catch (IOException e) {
            this.stop();
        }
    }

    /**
     * Initializes a user's saved state. If it exists, load it, otherwise create it.
     */
    private void initializeSave(){
        boolean savedUser = false;
        for(User temp : runningServer.currentSave.all){
            if(temp.userName.equals(clientMetaData.handle)){
                client = temp;
                client.online = true;
                if(client.pendingMessages.size() > 0) this.alert("You have received " + client.pendingMessages.size() + " pending messages.");
                for(Message x : client.pendingMessages){
                    this.privateMessage(x.contents);
                }
                this.client.clearPendingMessages();
                savedUser = true;
            }
        }
        if(!savedUser) client = new User(clientMetaData.handle, false, false);
    }
}