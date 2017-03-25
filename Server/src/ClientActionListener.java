/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * Agreement for a Listener on a ClientListener.
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/19/2017 : 11:52 PM
 */
interface ClientActionListener {
    void clientDisconnected(String userName, String address);
    void clientConnected(String userName, String address);
    void clientChangedName(String old, String updated);
}