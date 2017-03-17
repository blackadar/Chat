import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by jordan on 3/16/17.
 */
public class ChatClient extends JFrame implements Runnable{
    private JToolBar toolBar;
    private JTextArea chatLog;
    private JTextField textField;
    private JButton button;
    private JPanel rootPanel;
    private JScrollPane chatLogHolder;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream1;
    protected Thread listener;

    protected static String host = "localhost";
    protected static int port = 9090;

    public ChatClient(InputStream inputStream, OutputStream outputStream) {
        super("Network Chat");
        setContentPane(rootPanel);
        this.setPreferredSize(new Dimension(600,400));
        chatLogHolder.setAutoscrolls(true);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream));
        this.outputStream1 = new DataOutputStream(new BufferedOutputStream(outputStream));
        listener = new Thread(this);
        listener.start();

        button.addActionListener(actionEvent -> {
            try {
                outputStream1.writeUTF(textField.getText());
                outputStream1.flush();
                textField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        textField.addActionListener(actionEvent -> {
            try {
                outputStream1.writeUTF(textField.getText());
                outputStream1.flush();
                textField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.setVisible(true);
    }

    public void run () {
        try {
            while (true) {
                String line = inputStream.readUTF();
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
                    this.inputStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    this.outputStream1 = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                    listener = new Thread (this);
                    listener.start ();
                    chatLog.append("Local : Reconnection Successful.\n");
                    textField.setVisible(true);
                    button.setText("Send");
                    button.removeActionListener(button.getActionListeners()[0]);
                    button.addActionListener(actionEvent1 -> {
                        try{
                            outputStream1.writeUTF(textField.getText());
                            outputStream1.flush();
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

        catch (IOException ex) {
            ex.printStackTrace ();
        } finally {
            listener = null;
            textField.setVisible(false);
            validate ();
            try {
                outputStream1.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        try{
            Socket s = new Socket (host, port);
            new ChatClient(s.getInputStream (), s.getOutputStream ());
        } catch(Exception e){
        JOptionPane.showMessageDialog(null, "Unable to communicate with " + host + ":" + port, "Connection Lost", JOptionPane.WARNING_MESSAGE);
        }
    }
}
