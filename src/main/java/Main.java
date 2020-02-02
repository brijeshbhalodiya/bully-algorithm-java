public class Main{
    public static void main(String[] args) {
        try {
            ServerListener server = new ServerListener();
            server.start();
        } catch (Exception e) {
            System.out.println("Error when starting the server");
        }
    }
}