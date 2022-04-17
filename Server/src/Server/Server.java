package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static final int PORT = 4444;

    public static void main(String[] args) {
        HashMap<String, Player> connectedPlayers;
        HashMap<Integer, Game> notStartedGames = new HashMap<>();
        HashMap<Integer, Game> startedGames = new HashMap<>();
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                WelcomePlayerService welcomePlayerServ = new WelcomePlayerService(socket, notStartedGames, startedGames);
                Thread t = new Thread(welcomePlayerServ);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
