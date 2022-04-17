package Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import static Server.Utils.readRequest;

public class WelcomePlayerService implements Runnable {
    private final PrintWriter out;
    private final InputStreamReader in;
    private final Player player;
    private final HashMap<Integer, Game> notStartedGames;
    private final HashMap<Integer, Game> startedGames;

    public WelcomePlayerService(Socket s, HashMap<Integer, Game> notStartedGames, HashMap<Integer, Game> startedGames) throws IOException {
        this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        this.in = new InputStreamReader(s.getInputStream());
        this.notStartedGames = notStartedGames;
        this.startedGames = startedGames;
        this.player = new Player();
    }

    @Override
    public void run() {
        // send GAMES n
        treatGAMERequest();

        // wait for player to send REGIS or NEWPL
        // if NEWPL id port***
        // create new Server.Server.Game (partie) and add this player to it
        // if REGIS id port m***
        // check if player is already in a game and the game does exists
        // let the player join the game

        int port, m;
        boolean done = false;
        String[] requestSplit;
        String request, command, id;

        while (!done) {
            request = readRequest(in);
            requestSplit = request.split(" ");
            if (requestSplit.length < 3) {
                this.out.printf("REGNO***");
                continue;
            }
            command = requestSplit[0];
            id = requestSplit[1];
            port = Integer.parseInt(requestSplit[2]);
            switch (command) {
                case "NEWPL": // NEWPL id port***
                    treatNEWPLRequest(id, port);
                    done = true;
                    break;
                case "REGIS": // REGIS id port m***
                    if (requestSplit.length != 4) {
                        this.out.printf("REGNO***");
                        continue;
                    }
                    m = Integer.parseInt(requestSplit[3]);
                    done = treatREGISRequest(id, port, m);
                    break;
                default:
                    this.out.printf("DUNNO***");
                    break;
            }
        }

        done = false;
        // wait the player to send UNREG, SIZE?, GAME?, LIST? or START
        // in case of START, block the player, make him wait, how? (ignore his messages)
        while (!done) {
            request = readRequest(in);
            requestSplit = request.split(" ");
            command = requestSplit[0];
            switch (command) {
                case "UNREG":
                    treatUNREGRequest();
                    break;
                case "SIZE?":
                    if (requestSplit.length != 2) {
                        this.out.printf("DUNNO***");
                        continue;
                    }
                    m = Integer.parseInt(requestSplit[1]);
                    treatSIZERequest(m);
                    break;
                case "LIST?":
                    if (requestSplit.length != 2) {
                        this.out.printf("DUNNO***");
                        continue;
                    }
                    m = Integer.parseInt(requestSplit[1]);
                    treatLISTRequest(m);
                    break;
                case "GAME?":
                    treatGAMERequest();
                    break;
                case "START":
//                    treatSTARTRequest();
                    done = true;
                    break;
                default: // TODO: check if we really need to send DUNNO*** whatever the client has sent
                    this.out.printf("DUNNO***");
            }
        }
    }

    private void treatNEWPLRequest(String id, int port) {
        // NEWPL id port***
        this.player.setId(id);
        this.player.setPort(port);
        Game game = new Game();
        game.addPlayer(this.player);
        this.player.setGame(game);
        notStartedGames.put(game.getId(), game);
        this.out.printf("REGOK %d***", game.getId()); // send REGOK m
    }

    private boolean treatREGISRequest(String id, int port, int m) {
        // REGIS id port m***
        this.player.setId(id);
        this.player.setPort(port);
        if (!notStartedGames.containsKey(m)) {
            this.out.printf("REGNO***");
            return false;
        }
        notStartedGames.get(m).addPlayer(player);
        this.out.printf("REGOK %d***", m); // send REGOK m
        return true;
    }

    private Game getGame(int m) {
        if (startedGames.containsKey(m)) {
            return startedGames.get(m);
        }
        return notStartedGames.get(m);
    }

    private void treatUNREGRequest() {
        int m = this.player.getGame().getId();
        if (!this.player.unsubscribe()) {
            this.out.printf("DUNNO***");
            return;
        }
        this.out.printf("UNROK %d***", m);
    }

    private void treatSIZERequest(int m) {
        Game g = getGame(m);
        if (g == null) {
            this.out.printf("DUNNO***");
            return;
        }
        // send SIZE! m h w***
        this.out.printf("SIZE! %d %d %d***", m, g.getLabyrinth().getHeight(), g.getLabyrinth().getWidth());
    }

    private void treatLISTRequest(int m) {
        //check if the game exists
        Game g = getGame(m);
        if (g == null) {
            this.out.printf("DUNNO***");
            return;
        }
        this.out.printf("LIST! %d %d***", m, g.getNbPlayers());
        for (Player p : g.getPlayers().values()) {
            this.out.printf("PLAYR %s***", p.getId());
        }
    }

    private void treatGAMERequest() {
        // send GAMES n
        this.out.printf("GAMES %d***", notStartedGames.size());
        // send n OGAME
        for (Game g : notStartedGames.values()) {
            this.out.printf("OGAME %d %d***", g.getId(), g.getNbPlayers());
        }
    }
}
