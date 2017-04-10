import java.io.IOException;

public class HeadlessServer implements ServerActionListener{
    Server running;

    public HeadlessServer(String name) throws IOException {
        running = new Server(9090);
        running.addActionListener(this);
        running.name = name;
        running.run();
    }

    @Override
    public void output(String toOutput) {
        System.out.println(toOutput);
    }

    @Override
    public void addedClient() {
        //Take no action
    }

    @Override
    public void lostClient() {
        //Take no action
    }

    public static void main(String[] args) throws IOException {
        HeadlessServer running = new HeadlessServer("Beta Chat Server");
    }
}
