
import java.net.*;
import java.io.*;
import java.util.*;
/**
 * Created by Jordan Blackadar as a part of the Networking package in ScratchPad.
 *
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/16/2017 : 3:41 PM
 */
public class ChatHandler implements Runnable {

    protected Socket socket;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;
    protected String userName;
    protected boolean isAFK;
    protected boolean isModerator;
    protected boolean isAdministrator;

    public ChatHandler (Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream (new BufferedInputStream (socket.getInputStream()));
        this.outputStream = new DataOutputStream (new BufferedOutputStream (socket.getOutputStream()));
        this.userName = socket.getInetAddress().toString();
        this.userName = userName.substring(1,userName.length()-2); //Remove beginning slash and end dot
        this.isAFK = false;
        this.isModerator = false;
        this.isAdministrator = false;
        tellAll(userName + " is online.");
        tell("Welcome to the Chat Server.");
        tell("For a full list of commands, type /help .");
    }

    protected static ArrayList<ChatHandler> handlers = new ArrayList<>();

    public void run() {
        try {
            handlers.add(this);
            while (true) {
                String received = inputStream.readUTF();
                if(hasCommand(received)){
                    executeCommand(received);
                }
                else if(isAFK){
                    tellAll(userName + " is no longer AFK");
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
        System.err.println("Status: 3 (Lost Client Connection) to " + socket.getInetAddress());
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
        String[] commandParts = command.split(" ");
        String[] arguments = new String[commandParts.length - 1];
        for(int x = 1; x < commandParts.length; x++){
            arguments[x - 1] = commandParts[x];
        }
        if(commandParts.length == 0){
            commandParts = new String[1];
            commandParts[0] = command;
        }
        switch(commandParts[0].toLowerCase()){
            case("name"): {
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

            case("afk"): {
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

        }
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

    protected static void tellAll(String message){
        broadcast("Server : " + message);
    }

    protected void setName(String name){
        this.userName = name;
    }
}
