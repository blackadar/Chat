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
public class UserInterface extends JFrame implements Runnable{
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

    public UserInterface(InputStream inputStream, OutputStream outputStream) {
        super("Network Chat");
        recoverState();
        this.setIconImage(image);
        setContentPane(rootPanel);
        this.setPreferredSize(new Dimension(600,400));
        chatLog.setLineWrap(true);
        serverTabs.setTitleAt(0, host);
        pack();
        chatLog.setAutoscrolls(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            this.outputStream = new ObjectOutputStream(outputStream);
            outputStream.flush();
            this.inputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener = new Thread(this);
        listener.start();

        button.addActionListener(actionEvent -> {
            try {
                Message current = new Message(textField.getText());
                this.outputStream.writeObject(current);
                this.outputStream.flush();
                textField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        textField.addActionListener(actionEvent -> {
            try {
                Message current = new Message(textField.getText());
                this.outputStream.writeObject(current);;
                this.outputStream.flush();
                textField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.setVisible(true);
    }

    public void run () {
        try {
            while(true) {
                Message current = (Message) inputStream.readObject();
                String line = current.contents;
                chatLog.append(line + "\n");
                chatLog.setCaretPosition(chatLog.getDocument().getLength());
            }
        }
        catch(EOFException|SocketException e){
            chatLog.append("Local : Connection to server lost.\n");
            textField.setVisible(false);
            button.setText("Reconnect");
            button.removeActionListener(button.getActionListeners()[0]);
            button.addActionListener(actionEvent -> {
                try {
                    Socket s = new Socket(host, port);
                    this.inputStream = new ObjectInputStream(inputStream);
                    this.outputStream = new ObjectOutputStream(outputStream);
                    listener = new Thread (this);
                    listener.start ();
                    chatLog.append("Local : Reconnection Successful.\n");
                    textField.setVisible(true);
                    button.setText("Send");
                    button.removeActionListener(button.getActionListeners()[0]);
                    button.addActionListener(actionEvent1 -> {
                        try{
                            Message current = new Message(textField.getText());
                            this.outputStream.writeObject(current);
                            outputStream.flush();
                            textField.setText("");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (ConnectException ex) {
                    chatLog.append("Local : Connection to Server Refused. (Has the server been stopped?)\n");
                } catch (SocketException ex) {
                    ex.printStackTrace();
                    chatLog.append("Local : Connection to Server could not be established. (Has the Server moved locations?)\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    chatLog.append("Local : General I/O Exception during Reconnection.\n");
                }
            });

            JOptionPane.showMessageDialog(null, "Unable to communicate with the chat server.", "Connection Lost", JOptionPane.WARNING_MESSAGE);
        }

        catch (IOException | ClassNotFoundException ex) {
            chatLog.append("Local : General I/O Exception during Reconnection.\n");
            chatLog.append(Arrays.toString(ex.getStackTrace()) + "\n");
            ex.printStackTrace();
        } finally {
            listener = null;
            textField.setVisible(false);
            validate ();
            try {
                outputStream.close ();
            } catch (IOException e) {
                chatLog.append(Arrays.toString(e.getStackTrace()) + "\n");
            }
        }
    }

    public static void main(String[] args) throws IOException {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
                catch(Exception ex){
                    try {
                        UIManager.setLookAndFeel(
                                UIManager.getCrossPlatformLookAndFeelClassName());
                    }catch(Exception e){
                    e.printStackTrace();
                }
            }

        try{
            String inputDialog = (String) JOptionPane.showInputDialog(null, null, "Server IP and Port: ", JOptionPane.QUESTION_MESSAGE, null, null, "localhost:9090");
            if(!(inputDialog == null || ("".equals(inputDialog)))) {
                host = inputDialog.split(":")[0];
                port = Integer.parseInt(inputDialog.split(":")[1]);
                Socket s = new Socket(host, port);
                new UserInterface(s.getInputStream(), s.getOutputStream());
            }
        } catch(Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Unable to communicate with " + host + ":" + port, "Connection Lost", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void recoverState(){
        File save = new File("save.csave");
        if(!(save.exists())){
            JOptionPane.showInputDialog(null, "Welcome to Network Chat.\nInput Username:");
        }
    }
}
