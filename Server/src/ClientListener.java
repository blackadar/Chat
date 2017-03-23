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
    protected String userName;
    protected boolean isAFK;
    protected static ArrayList<ClientListener> handlers = new ArrayList<>();
    protected ArrayList<ClientActionListener> listeners = new ArrayList<>();

    public ClientListener(Socket socket, String userName) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.userName = userName;
        this.isAFK = false;
        tellAll(userName + " is online.");
        tell("Welcome to the Chat Server.");
        tell("For a full list of commands, type /help .");
    }

    /**
     * Looping Logic for a ClientListener thread. Handles input and commands.
     */
    public void run(){
        try {
            handlers.add(this);
            for(ClientActionListener x : listeners){
                x.clientConnected(userName);
            }
            while (true) {
                Message received =(Message)inputStream.readObject();
                if(received.contents.isEmpty()){
                    //Ignore Empty Submissions
                }
                else if(hasCommand(received.contents)){
                    executeCommand(received.contents);
                }
                else if(isAFK){
                    tellAll(userName + " is no longer AFK.");
                    isAFK = false;
                    broadcast(userName + " : " + received.contents);
                }
                else {
                    broadcast(userName + " : " + received.contents);
                }
            }
        } catch (IOException e) {
            this.stop();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
        for(ClientActionListener x : listeners){
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

    protected void executeCommand(String input) {
        String command = input.substring(1, input.length()); //Remove beginning slash
        String[] commandParts = command.split(" "); //Split into parts on spaces
        String[] arguments = new String[commandParts.length - 1]; //Take args after command

        for (int x = 1; x < commandParts.length; x++) { //Fill arguments
            arguments[x - 1] = commandParts[x];
        }

        /*
        Server Commands
         */

        try {
            switch (commandParts[0].toLowerCase()) { //Switch on command (ignoring case)
                case ("help"): { //Sends help documentation
                    tell("List of Available Commands: \n 1.  /name [name] : Modifies your server-wide alias. \n 2.  /afk : Notifies others that you are away. \n 3.  /list : Lists all online users. \n 4.  /me [phrase]: (Ex. /me does something = [name] does something)");
                }
                break;
                case ("hack"): {//Hacks
                    tellAll("Oh no I've been hacked by " + userName + "!");
                }
                break;
                case ("name"): { //Changes userName
                    String name = "";
                    for (int i = 0; i < arguments.length; i++) {
                        name += arguments[i];
                    }
                    if (name.isEmpty()) throw new IllegalArgumentException("Command Invalid: /name [user name]");
                    for (ClientListener c : handlers) {
                        if (name.equalsIgnoreCase(c.userName))
                            throw new IllegalArgumentException("Username already exists");
                        else if(name.equalsIgnoreCase("server"))
                            throw new IllegalArgumentException("Cannot be named server ");
                    }
                    setName(name);
                    tell("Username changed to " + userName);
                }
                break;

                case ("afk"): { //Toggles isAFK and broadcasts state
                    if (this.isAFK) {
                        tellAll(userName + " is no longer AFK");
                        this.isAFK = false;
                    } else {
                        tellAll(userName + " is now AFK");
                        this.isAFK = true;
                    }
                }
                break;

                case ("list"): { // Sends list of all online users
                    String online = "";
                    for (ClientListener x : handlers) {
                        online += x.userName + ", ";
                    }
                    tell("Online: " + online);
                }
                break;

                case ("me"): { //Announces User-Provided State
                    if (arguments.length == 0) throw new IllegalArgumentException("Command requires parameters.");
                    tellAll(userName + " " + input.substring(input.indexOf(" ") + 1, input.length()) + ".");
                }
                break;

                case("shrug"): {
                    tellAll(userName + ": ¯\\_(ツ)_/¯ ");
                }
                break;

                default: { //Catches unrecognized commands
                    throw new IllegalArgumentException("Unrecognized command. Use /help for a list of all commands.");
                }


            }
        }
        catch(IllegalArgumentException e){
            tell(e.getMessage());
        }
    }

    /*
    Internal Methods
     */

    public void addListener(ClientActionListener toAdd) {
        listeners.add(toAdd);
    }

    protected static void broadcast(String message) {
        synchronized(handlers) {
            Enumeration allHandlers = Collections.enumeration(handlers);
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

    protected void message(String sender, ClientListener recipient, String message){
        //TODO: Implement message method
    }

    protected static void tellAll(String message){
        broadcast("Server : " + message);
    }

    protected void setName(String name){
        for(ClientActionListener x : listeners){
            x.clientChangedName(userName, name);
        }
        this.userName = name;
    }
}
