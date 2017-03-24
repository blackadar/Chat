import java.io.Serializable;

/**
 * Created by liamn on 3/24/2017.
 */
class User implements Serializable {
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