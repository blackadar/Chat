
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Jordan Blackadar as a part of the Networking package in ScratchPad.
 *
 * @author Jordan Blackadar<"jordan.blackadar@outlook.com"/>
 * @version 0.1.0
 * @since 3/16/2017 : 2:49 PM
 */
public class DateServer implements Runnable{

    public static void main(String[] args) throws IOException{
        ServerSocket listener = new ServerSocket(9090);
        try{
            while(true) {
                Socket socket = listener.accept();
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    //out.println(new Date().toString());
                    out.println("http://www.javaworld.com/article/2076864/java-concurrency/building-an-internet-chat-system.html");
                }
                finally{
                    socket.close();
                }
            }
        }
        finally{
            listener.close();
        }
    }

    @Override
    public void run() {

    }
}
