package Server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class WelcomePlayerService implements Runnable {
    private Socket socket;
    private Player player;
    private HashMap<Integer, Game> notStartedGames;
    private HashMap<Integer, Game> startedGames;

    public WelcomePlayerService(Socket s, HashMap<Integer, Game> notStartedGames, HashMap<Integer, Game> startedGames, Player player) {
        this.socket = s;
        this.notStartedGames = notStartedGames;
        this.startedGames = startedGames;
        this.player = player;
    }

    public void execSIZERequest(PrintWriter pw, int m) {
        // verify if the game exists
        if (notStartedGames.containsKey(m) || startedGames.containsKey(m)) {
            Game g = notStartedGames.get(m);
            if (g == null) {
                g = startedGames.get(m);
            }
            // send SIZE! m h w
            pw.printf("SIZE! %d %d %d***", m, g.getLabyrinth().getHeight(), g.getLabyrinth().getWidth());
        } else {
            pw.printf("DUNNO***");
        }
    }

    public void execGAMERequest(PrintWriter pw) {
        // send GAMES n
        pw.printf("GAMES %d***", notStartedGames.size());

        // send n OGAME
        for (Game g : notStartedGames.values()) {
            pw.printf("OGAME %d %d***", g.getId(), g.getNbPlayers());
        }
    }

    public void execLISTRequest(PrintWriter pw, int m) {
        //check if the game exists
        if (startedGames.containsKey(m) || notStartedGames.containsKey(m)) {
            Game g = startedGames.get(m);
            if (g == null) {
                g = notStartedGames.get(m);
            }
            pw.printf("LIST! %d %d***", m, g.getNbPlayers());
            for (Player p : g.getPlayers().values()) {
                pw.printf("PLAYR %d***", p.getId());
            }
        } else {
            pw.print("DUNNO***");
        }
    }

    public void execUNREGRequest(PrintWriter pw) {
        // check if the game exists
        if (this.player.getGame() != null) {
            int m = this.player.getGame().getId();
            this.player.unsubscribe();
            pw.printf("UNROK %d***", m);
        } else {
            pw.print("DUNNO***");
        }
    }

    public void run() {
        try {
            InputStreamReader inSR = new InputStreamReader(socket.getInputStream());
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            execGAMERequest(pw);
            execLISTRequest(pw, -1);
            execSIZERequest(pw, -1);

            // wait for player to send REGIS or NEWPL
            // if NEWPL
            // create new Server.Server.Game (partie) and add this player to it
            // if REGIS
            // check if player is already in a game and the game does exists
            // let the player join the game

            // wait the player to send UNREG, SIZE?, GAME?, LIST? or START
            // in case of START, block the player, make him wait, how? (ignore his messages)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}