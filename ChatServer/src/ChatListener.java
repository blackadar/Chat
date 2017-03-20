/**
 * Created by jordan on 3/19/17.
 */
interface ChatListener {
    void clientDisconnected(String userName);
    void clientConnected(String userName);
    void clientChangedName(String old, String updated);
}
