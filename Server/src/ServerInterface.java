import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Jordan Blackadar as a part of the Server package in Chat.
 * GUI for Server
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @author Liam Brown<"liamnb525@gmail.com"/>
 * @version 0.3.5
 * @since 3/19/2017 : 2:15 PM
 */
public class ServerInterface extends JFrame implements ServerActionListener{
    private JTextArea serverLog;
    private JPanel panel;
    private JLabel numberOnlineLabel;
    private JTextField AdminField;
    private JTextField userSearch;

    private JMenuBar menus;
    private ArrayList<JMenu> menu = new ArrayList<>();

    Server running;

    public static void main(String[] args) throws IOException {
        ServerInterface current = new ServerInterface("Beta Chat Server");
    }

    public ServerInterface(String name) throws IOException {
        initGui(); //Set up GUI
        running = new Server(9090);
        running.addActionListener(this);
        running.name = name;
        running.run();
    }

    private void initGui(){
        this.setTitle("Server Monitor");
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screen_size.getWidth();
        int height = (int) screen_size.getHeight();
        Font Sans = new Font("Sans Serif", Font.PLAIN, height / 72);
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png"));
        this.setIconImage(image);
        setContentPane(panel);

        //Create Menus
        menus = new JMenuBar();

        //File
        JMenu file = new JMenu("File");
        file.setFont(Sans);
        file.add(new JMenu("Test"));
        menus.add(file);

        //Preferences
        JMenu prefs = new JMenu("Preferences");
        prefs.setFont(Sans);
        menus.add(prefs);

        //Sub Menus
        JMenu save = new JMenu("Save");
        save.setFont(Sans);
        file.add(save);

        JMenu buffer = new JMenu("Buffer");
        buffer.setFont(Sans);
        prefs.add(buffer);

        JCheckBoxMenuItem chatlog = new JCheckBoxMenuItem("Chat Log");
        chatlog.setFont(Sans);
        chatlog.addItemListener(source -> {
            if (running.loaded_prefs.readPreference("view_chat").getValue().equals("disabled")) {
                running.loaded_prefs.setPreference("view_chat", "enabled");
            } else {
                running.loaded_prefs.setPreference("view_chat", "disabled");
            }
        });
        buffer.add(chatlog);

        menu.add(file);
        menu.add(prefs);
        for (JMenu temp : menu) menus.add(temp);
        menus.setFont(Sans);
        menus.setPreferredSize(new Dimension(-1, height / 43));
        this.setJMenuBar(menus);

        this.setPreferredSize(new Dimension(width / 2, height / 2));
        numberOnlineLabel.setFont(Sans);
        numberOnlineLabel.setText("0");
        serverLog.setLineWrap(true);
        serverLog.setMinimumSize(new Dimension(-1, -1));
        serverLog.setFont(Sans);

        //Initialize administrator command field
        AdminField.setFont(new Font("Sans Serif", Font.PLAIN, height / 72));
        AdminField.setPreferredSize(new Dimension(-1, height / 43));
        AdminField.setMinimumSize(new Dimension(-1, -1));
        AdminField.setText("Command");
        AdminField.setForeground(new Color(160, 160, 160));
        AdminField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                AdminField.setText("");
                AdminField.setForeground(new Color(0, 0, 0));
            }

            @Override
            public void focusLost(FocusEvent e) {
                AdminField.setText("Command");
                AdminField.setForeground(new Color(160, 160, 160));
            }
        });
        AdminField.addActionListener(actionEvent -> {
            Message current = new Message(AdminField.getText());
            try {
                running.executeAdminCommand(current.contents);
            } catch (Exception e) {
                output("Admin command not in a valid format");
            }
            AdminField.setText("");
        });

        this.setLocationRelativeTo(null);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void output(String toOutput) {
        serverLog.append(toOutput + "\n");
        serverLog.setCaretPosition(serverLog.getDocument().getLength());
    }

    @Override
    public void addedClient() {
        numberOnlineLabel.setText((Integer.parseInt(numberOnlineLabel.getText()) +1) + "");
    }

    @Override
    public void lostClient() {
        numberOnlineLabel.setText((Integer.parseInt(numberOnlineLabel.getText()) -1) + "");
    }

    private static void threadNewServer(String name, int port){
        Server running = null;
        try {
            running = new Server(port); //Create server and set port
            running.name = name;
            Thread server = new Thread(running);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            if(!(running == null)) {
                for (User x : running.currentSave.all) {
                    x.online = false;
                }
            }
            JOptionPane.showMessageDialog(null, "A General Exception was Detected on Server " + running.name, e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

}