/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * Agreement for a Listener on a ChatHandler.
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/19/2017 : 11:52 PM
 */
interface ChatListener {
    void clientDisconnected(String userName);
    void clientConnected(String userName);
    void clientChangedName(String old, String updated);
}