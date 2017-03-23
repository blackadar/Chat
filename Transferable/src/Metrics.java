import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jordan Blackadar as a part of the PACKAGE_NAME package in Chat.
 * Initialization Data sent upon Connection
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/23/2017 : 1:41 PM
 */
public class Metrics implements Serializable{
    public String handle;
    public ArrayList<String> destinations = new ArrayList<>();
    public boolean isServer;

    /**
     * Intended for Server -> Client initialization. Supports Server Federation.
     * @param handle The server's friendly name
     * @param destinations The server's default rooms
     */
    public Metrics(String handle, ArrayList<String> destinations){
        this.handle = handle;
        this.destinations = destinations;
        isServer = true;
    }

    /**
     * Intended for Client -> Server initialization.
     * @param handle The client's friendly name
     */
    public Metrics(String handle){
        this.handle = handle;
        isServer = false;
    }
}
