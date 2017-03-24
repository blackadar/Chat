import java.io.*;
import java.util.ArrayList;

/**
 * Created by Jordan Blackadar as a part of the Server package in Chat.
 * Defines a Save and its abilities
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */

public class Save implements Serializable{
    public ArrayList<User> all = new ArrayList<>();

    public static Save revive() throws IOException, ClassNotFoundException {
        File save = new File("save.svs");
        FileInputStream in = new FileInputStream(save);
        ObjectInputStream fileIn = new ObjectInputStream(in);
        Save toReturn =  (Save) fileIn.readObject();
        fileIn.close();
        in.close();
        return toReturn;
    }

    public static void preserve(Save s) throws IOException {
        FileOutputStream out = new FileOutputStream(new File("save.svs"));
        ObjectOutputStream fileOut = new ObjectOutputStream(out);
        fileOut.writeObject(s);
        fileOut.close();
        out.close();
    }

    public void instantiate(String name, boolean isMod){
        this.all.add(new User(name, isMod));
    }


    class User implements Serializable{
        public String name;
        public boolean isMod;

        public User(String name, boolean isMod){
            this.name = name;
            this.isMod = isMod;
        }

    }
}
