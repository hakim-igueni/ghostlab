package Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 4444;

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                // TODO: make sure that both h and w don't exceed the 1000 (so < 1000)
                Socket socket = server.accept();
                PlayerThread welcomePlayerServ = new PlayerThread(socket);
                Thread t = new Thread(welcomePlayerServ);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
