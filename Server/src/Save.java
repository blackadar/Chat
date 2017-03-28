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
    public ArrayList<User> all;

    public Save(){
        all = new ArrayList<User>();
    }

    public static Save read() throws IOException, ClassNotFoundException {
        File save = new File("save.svs");
        FileInputStream in = new FileInputStream(save);
        ObjectInputStream fileIn = new ObjectInputStream(in);
        Save toReturn =  (Save) fileIn.readObject();
        fileIn.close();
        in.close();
        return toReturn;
    }

    public static void write(Save s) throws IOException {
        FileOutputStream out = new FileOutputStream(new File("save.svs"));
        ObjectOutputStream fileOut = new ObjectOutputStream(out);
        fileOut.writeObject(s);
        fileOut.close();
        out.close();
    }

    public void recognize(User check) {
        for (User x : all) {
            if (x.userName.equalsIgnoreCase(check.userName)){
                return;
            }
        }
        this.all.add(check);
    }



    public static void updateUser(){

    }


}
