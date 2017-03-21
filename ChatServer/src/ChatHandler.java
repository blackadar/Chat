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
public class ChatHandler implements Runnable {

    protected Socket socket;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;
    protected String userName;
    protected boolean isAFK;
    protected static ArrayList<ChatHandler> handlers = new ArrayList<>();
    protected ArrayList<ChatListener> listeners = new ArrayList<>();

    public ChatHandler (Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream (new BufferedInputStream (socket.getInputStream()));
        this.outputStream = new DataOutputStream (new BufferedOutputStream (socket.getOutputStream()));
        this.userName = socket.getInetAddress().toString(); //Set default username to IP address
        this.userName = userName.substring(1,userName.length()); //Remove beginning slash and end dot
        this.isAFK = false;
        tellAll(userName + " is online.");
        tell("Welcome to the Chat Server.");
        tell("For a full list of commands, type /help .");
    }

    /**
     * Looping Logic for a ChatHandler thread. Handles input and commands.
     */
    public void run(){
        try {
            handlers.add(this);
            for(ChatListener x : listeners){
                x.clientConnected(userName);
            }
            while (true) {
                String received = inputStream.readUTF();
                if(received.isEmpty()){
                    //Ignore Empty Submissions
                }
                else if(hasCommand(received)){
                    executeCommand(received);
                }
                else if(isAFK){
                    tellAll(userName + " is no longer AFK.");
                    isAFK = false;
                    broadcast(userName + " : " + received);
                }
                else {
                    broadcast(userName + " : " + received);
                }
            }
        } catch (IOException e) {
            this.stop();
        } finally {
            handlers.remove(this);
            try {
                socket.close ();
            } catch (IOException e) {
                this.stop();
            }
        }
    }

    public void stop(){
        handlers.remove(this);
        for(ChatListener x : listeners){
            x.clientDisconnected(userName);
        }
        tellAll(userName + " is offline.");
    }

    protected static boolean hasCommand(String input){
        if(input.length() >= 0){
            if(input.charAt(0) == '/' || input.charAt(0) == '\\') {
                return true;
            }
            else{
                return false;
            }
        }
        else return false;
    }

    protected void executeCommand(String input){
        String command = input.substring(1,input.length()); //Remove beginning slash
        String[] commandParts = command.split(" "); //Split into parts on spaces
        String[] arguments = new String[commandParts.length - 1]; //Take args after command

        for(int x = 1; x < commandParts.length; x++){ //Fill arguments
            arguments[x - 1] = commandParts[x];
        }

        /*
        Server Commands
         */

        switch(commandParts[0].toLowerCase()){ //Switch on command (ignoring case)
            case("help") : { //Sends help documentation
                tell("List of available commands: \n 1) /name [NEW NAME] changes your name to a new name \n 2) /afk makes you AFK \n 3) /list lists all current server members");
                //TODO: Send documentation in pages to the client
            }

            break;
            case("name"): { //Changes userName
                String name = "";
                for(int i = 0; i < arguments.length; i++){
                    name += arguments[i];
                }
                try {
                    if (name.isEmpty()) throw new IllegalArgumentException("Command Invalid: /name [user name]");
                    for (ChatHandler c : handlers) {
                        if (name.equalsIgnoreCase(c.userName))
                            throw new IllegalArgumentException("Username already exists");
                    }
                    setName(name);
                    tell("Username changed to " + userName);
                }
                catch(IllegalArgumentException e){
                    tell(e.getMessage());
                }
            }
            break;

            case("afk"): { //Toggles isAFK and broadcasts state
                if(this.isAFK){
                    tellAll(userName + " is no longer AFK");
                    this.isAFK = false;
                }
                else{
                    tellAll(userName + " is now AFK");
                    this.isAFK = true;
                }
            }
            break;

            case("list"): { // Sends list of all online users
                String online = "";
                for(ChatHandler x : handlers){
                    online += x.userName + ", ";
                }
                tell("Online: " + online);
            }
            break;

            default : { //Catches unrecognized commands
                tell("Unrecognized command. Use /help for a list of all commands.");
            }
        }
    }

    /*
    Internal Methods
     */

    public void addListener(ChatListener toAdd) {
        listeners.add(toAdd);
    }

    protected static void broadcast(String message) {
        synchronized(handlers) {
            Enumeration allHandlers = Collections.enumeration(handlers);
            while (allHandlers.hasMoreElements ()) {
                ChatHandler currentChatHandler = (ChatHandler) allHandlers.nextElement();
                try {
                    synchronized(currentChatHandler.outputStream) {
                        currentChatHandler.outputStream.writeUTF(message);
                    }
                    currentChatHandler.outputStream.flush();
                } catch (IOException e) {
                    currentChatHandler.stop();
                }
            }
        }
    }

    protected void tell(String message){
        try {
            synchronized(this.outputStream) {
                this.outputStream.writeUTF("Server : " + message);
            }
            this.outputStream.flush();
        } catch (IOException e) {
            this.stop();
        }
    }

    protected void message(String sender, ChatHandler recipient, String message){
        //TODO: Implement message method
    }

    protected static void tellAll(String message){
        broadcast("Server : " + message);
    }

    protected void setName(String name){
        for(ChatListener x : listeners){
            x.clientChangedName(userName, name);
        }
        this.userName = name;
    }
}
