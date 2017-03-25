import java.io.Serializable;

/**
 * Created by Jordan Blackadar as a part of the PACKAGE_NAME package in Chat.
 * Saves the user's userName and last used server
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/23/2017 : 4:14 PM
 */
public class Save implements Serializable{
    public String userName;
    public String lastUsedServer;

    public Save(String userName, String lastUsedServer) {
        this.userName = userName;
        this.lastUsedServer = lastUsedServer;
    }
}
