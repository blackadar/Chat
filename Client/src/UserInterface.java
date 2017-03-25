import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Created by Jordan Blackadar as a part of the main package in Chat.
 * GUI for Chat interface
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.7.9
 * @since 3/17/2017 : 3:05 PM
 */
public class UserInterface extends JFrame implements Runnable {
    private Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png"));
    private JTextArea chatLog;
    private JTextField textField;
    private JButton button;
    private JPanel rootPanel;
    private JScrollPane chatLogHolder;
    private JTabbedPane serverTabs;
    protected ObjectInputStream inputStream;
    protected ObjectOutputStream outputStream;
    protected Thread listener;

    protected static String host;
    protected static int port;
    protected static String userName;

    public static void main(String[] args) throws IOException {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            recoverState();
            new UserInterface();
        } catch (Exception e) {
            System.err.println("Unhandled Exception Occurred");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to communicate with " + host + ":" + port, "Connection Lost", JOptionPane.WARNING_MESSAGE);
        }
    }

    public UserInterface() {
        super("Network Chat");
        this.setIconImage(image);
        setContentPane(rootPanel);
        this.setPreferredSize(new Dimension(600, 400));
        chatLog.setLineWrap(true);
        serverTabs.setTitleAt(0, host);
        pack();
        chatLog.setAutoscrolls(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultActionListeners();
        this.setVisible(true);
        try {
            initializeStreams();
        } catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to communicate with " + host + ":" + port, "Connection Lost", JOptionPane.WARNING_MESSAGE);

        }
        listener = new Thread(this);
        listener.start();
    }

    public void run() {
        try {
            outputStream.writeObject(new MetaData(userName));
            while (true) {
                Message current = (Message) inputStream.readObject();
                if(current.isCommand){
                    executeCommand(current);
                }
                else {
                    chatLog.append(current.contents + "\n");
                    chatLog.setCaretPosition(chatLog.getDocument().getLength());
                }
            }
        } catch (EOFException | SocketException e) {
            chatLog.append("Local : Connection to server lost.\n");
            e.printStackTrace();
            lostConnectionState();
        } catch (IOException | ClassNotFoundException ex) {
            chatLog.append("Local : General I/O Exception during Reconnection.\n");
            ex.printStackTrace();
            lostConnectionState();
        } finally {
            listener = null;
            textField.setVisible(false);
            validate();
            try {
                outputStream.close();
            } catch (IOException e) {
                chatLog.append(Arrays.toString(e.getStackTrace()) + "\n");
            }
        }
    }

    protected static void recoverState() throws IOException, ClassNotFoundException {
        File save = new File("save.sv");
        if (!(save.exists())) {
            userName = JOptionPane.showInputDialog(null, "Welcome to Network Chat.\nInput Username:");
            String providedAddress = (String) JOptionPane.showInputDialog(null, null, "Server IP and Port: ", JOptionPane.QUESTION_MESSAGE, null, null, "localhost:9090");
            if (!(providedAddress == null )) {
                host = providedAddress.split(":")[0];
                port = Integer.parseInt(providedAddress.split(":")[1]);
                FileOutputStream out = new FileOutputStream(save);
                ObjectOutputStream fileOut = new ObjectOutputStream(out);
                fileOut.writeObject(new Save(userName, host + ":" + port));
            }
        } else {
                FileInputStream in = new FileInputStream(save);
                ObjectInputStream fileOut = new ObjectInputStream(in);
                Save recovered = (Save) fileOut.readObject();
                host = recovered.lastUsedServer.split(":")[0];
                port = Integer.parseInt(recovered.lastUsedServer.split(":")[1]);
                userName = recovered.userName;
        }
    }

    protected void initializeStreams() throws IOException {
            Socket s = new Socket(host, port);
            this.outputStream = new ObjectOutputStream(s.getOutputStream());
            outputStream.flush();
            this.inputStream = new ObjectInputStream(s.getInputStream());
    }

    protected void lostConnectionState(){
        textField.setVisible(false);
        button.setText("Reconnect");
        button.removeActionListener(button.getActionListeners()[0]);
        button.addActionListener(actionEvent -> {
            try {
                Socket s = new Socket(host, port);
                this.outputStream = new ObjectOutputStream(s.getOutputStream());
                outputStream.flush();
                this.inputStream = new ObjectInputStream(s.getInputStream());
                regainedConnectionState();
            } catch (ConnectException ex) {
                ex.printStackTrace();
                chatLog.append("Local : Connection to Server Refused. (Has the server been stopped?)\n");
            } catch (SocketException ex) {
                ex.printStackTrace();
                chatLog.append("Local : Connection to Server could not be established. (Has the Server moved locations?)\n");
            } catch (EOFException ex){
                ex.printStackTrace();
                chatLog.append("Local : Connection Interrupted or Cut Short. (Have you tried restarting the server?)\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                chatLog.append("Local : General I/O Exception during Reconnection.\n");
            }
        });
        JOptionPane.showMessageDialog(null, "Unable to communicate with the chat server.", "Connection Lost", JOptionPane.WARNING_MESSAGE);
    }

    protected void regainedConnectionState(){
        listener = new Thread(this);
        listener.start();
        chatLog.append("Local : Reconnection Successful.\n");
        button.setText("Send");
        button.removeActionListener(button.getActionListeners()[0]);
        setDefaultActionListeners();
    }

    protected void setDefaultActionListeners(){
        button.addActionListener(actionEvent -> {
            sendMessage();
        });

        textField.addActionListener(actionEvent -> {
            sendMessage();
        });

        button.setVisible(true);
        textField.setVisible(true);
    }

    protected void sendMessage(){
        try {
            Message current = new Message(textField.getText());
            this.outputStream.writeObject(current);
            this.outputStream.flush();
            textField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            chatLog.append("Local : Server could not interpret your message.\n");
        }
    }

    protected void clearText(){
        chatLog.setText("Local : Cleared History.");
    }

    protected void executeCommand(Message message){
        switch(message.contents.toLowerCase()){
            case("clear") : clearText();
            break;
        }
    }
}
