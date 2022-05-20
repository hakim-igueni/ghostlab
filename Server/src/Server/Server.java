package Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 4444;

    public static void main(String[] args) {
        System.out.println("Server is running...");
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                // TODO: make sure that both h and w don't exceed the 1000 (so < 1000)
                Socket socket = server.accept();
                PlayerHandler playerHandler = new PlayerHandler(socket);
                Thread t = new Thread(playerHandler);
                t.start();
                // todo: kill the thread when the player disconnects
                // todo: (optional: add an array of threads)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
