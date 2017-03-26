import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by liamn on 3/26/2017.
 */
public class Preferences implements Serializable{
    ArrayList<Preference> preferences = new ArrayList<>();

    public void addPreference(String key, String val){
        preferences.add(new Preference(key, val));
    }

    /** Removes a preference
     * @param key
     * @return boolean true if preference was removed, false otherwise
     */
    public boolean removePreference(String key){
        int c = 0;
        boolean removed = false;
        for(Preference temp : preferences){
            if(temp.getKey().equals(key)){
                removed = true;
                break;
            }
            c++;
        }
        if(removed == true){
            preferences.remove(c);
            return true;
        }
        return false;
    }

    /**
     * Resets the value of a preference given a key
     * @param key
     * @param newval
     * @return boolean true if preference was changed, false otherwise
     */
    public boolean setPreference(String key, String newval) {
        for (int c = 0; c < preferences.size(); c++) {
            if (preferences.get(c).getKey().equals(key)) {
                preferences.get(c).setValue(newval);
                return true;
            }
        }
        return false;
    }

    public class Preference implements Serializable {
        private String key;
        private String value;

        public Preference(String key, String value){
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String toString(){
            return key + ";" + value;
        }

    }
}
