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
    public boolean isCommand;
    public String destinationRoom;

    /**
     * Initializes a message for a /all room.
     * @param contents The contents of the message.
     */
    public Message(String contents){
        this.contents = contents;
        this.isCommand = false;
        this.destinationRoom = "all";
    }

    /**
     * Initializes a message for a specified room
     * @param contents The contents of the message.
     * @param destinationRoom The name of the destination room.
     */
    public Message(String contents, String destinationRoom){
        this.contents = contents;
        this.isCommand = false;
        this.destinationRoom = destinationRoom;
    }

    /**
     * Initializes a message for a client that may or may not be a command, or destined for a room.
     * @param contents The contents of the message.
     * @param destinationRoom The name of the destination room.
     * @param isCommand Marker for an executable command in contents.
     */
    public Message(String contents, String destinationRoom, boolean isCommand){
        this.contents = contents;
        this.isCommand = isCommand;
        this.destinationRoom = destinationRoom;
    }
}
