package Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 4444;

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                WelcomePlayerService welcomePlayerServ = new WelcomePlayerService(socket);
                Thread t = new Thread(welcomePlayerServ);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
