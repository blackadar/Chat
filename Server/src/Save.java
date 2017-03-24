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

    public void addIfMissing(User check) {
        System.out.println("Checking " + check.userName);
        for (User x : all) {
            if (x.userName.equalsIgnoreCase(check.userName)){
                System.out.println(check.userName + " already exists.");
                return;
            }
        }
        this.all.add(check);
        System.out.println("Added " + all.get(all.size() - 1).userName);
    }

    public static User instantiateUser(String name, boolean isMod, boolean blacklist){
        return (new Save()).new User(name, isMod, blacklist);
    }

    class User implements Serializable{
        public String userName;
        public boolean isMod;
        public boolean blacklist;

        public User(String userName, boolean isMod, boolean blacklist){
            this.userName = userName;
            this.isMod = isMod;
            this.blacklist = blacklist;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void setMod(boolean isMod) {
            this.isMod = isMod;
        }

        public void setBlacklist(boolean blacklist) {
            this.blacklist = blacklist;
        }
    }
}
