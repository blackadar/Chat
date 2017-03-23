import java.io.Serializable;

/**
 * Created by Jordan Blackadar as a part of the Transferable package in Chat.
 * Defines a Serializable Message for Sending
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/23/2017 : 1:35 PM
 */
public class Message implements Serializable{
    public String contents;
    Prefix prefix;

    /**
     * Initializes a message for a /all room.
     * @param contents The contents of the message.
     */
    public Message(String contents){
        this.contents = contents;
        this.prefix = new Prefix(false, "all");
    }

    /**
     * Initializes a message for a specified room
     * @param contents The contents of the message.
     * @param destinationRoom The name of the destination room.
     */
    public Message(String contents, String destinationRoom){
        this.contents = contents;
        this.prefix = new Prefix(false, destinationRoom);
    }

    /**
     * Initializes a message for a client that may or may not be a command, or destined for a room.
     * @param contents The contents of the message.
     * @param destinationRoom The name of the destination room.
     * @param isCommand Marker for an executable command in contents.
     */
    public Message(String contents, String destinationRoom, boolean isCommand){
        this.contents = contents;
        this.prefix = new Prefix(isCommand, destinationRoom);
    }

    /**
       Inner class representing message prefix, to be used exclusively by instances of Message.
     */
    private class Prefix implements Serializable{
        boolean isCommand;
        String destinationRoom;

        public Prefix(boolean isCommand, String destinationRoom) {
            this.isCommand = isCommand;
            this.destinationRoom = destinationRoom;
        }
        public boolean isCommand(){
            return isCommand;
        }
        public String getDestinationRoom(){
            return destinationRoom;
        }
    }
}
