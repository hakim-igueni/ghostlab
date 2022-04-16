package Server;

import java.net.*;
import java.util.HashMap;
import java.io.*;

import static Server.Utils.readRequest;

public class Server {
    private static final int PORT = 4444;

    // private HashMap<String, Server.Player> connectedPlayers;
//    private HashMap<Integer, Game> notStartedGames = new HashMap<>();
//    private HashMap<Integer, Game> startedGames = new HashMap<>();
    // TODO: think if it is necessary to have a HashMap or List

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            Socket socket = server.accept();
            InputStreamReader inSR = new InputStreamReader(socket.getInputStream());
            System.out.println(readRequest(inSR));
            System.out.println(readRequest(inSR));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // public static void main(String[] args) {
    // try (ServerSocket server = new ServerSocket(PORT)) {
    // while (true) {
    // Socket socket = server.accept();
    // Server.Server.WelcomePlayerService welcPlayerServ = new Server.Server.WelcomePlayerService(socket,
    // notStartedGames, startedGames);
    // Thread t = new Thread(welcPlayerServ);
    // t.start();
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
}
