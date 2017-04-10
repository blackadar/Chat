public interface ServerActionListener {
    void output(String toOutput);
    void addedClient();
    void lostClient();
}
