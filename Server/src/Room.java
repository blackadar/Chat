import java.util.ArrayList;

/**
 * Created by Jordan Blackadar as a part of the PACKAGE_NAME package in Chat.
 * Manages ChatHandlers by Rooms.
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/23/2017 : 10:02 AM
 */
public class Room {
    public String name;
    private ArrayList<ClientListener> participants = new ArrayList<>();
}
