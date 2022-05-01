package Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

import static Server.Utils.readRequest;

public class WelcomePlayerService implements Runnable {
    private final static HashMap<String, Consumer<String[]>> commands = new HashMap<>();
    private final PrintWriter out;
    private final InputStreamReader in;
    private final Player player;

    public WelcomePlayerService(Socket s) throws IOException {
        this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        this.in = new InputStreamReader(s.getInputStream());
        this.player = new Player();
        commands.put("NEWPL", this::treatNEWPLRequest);
        commands.put("REGIS", this::treatREGISRequest);
        commands.put("UNREG", this::treatUNREGRequest);
        commands.put("SIZE?", this::treatSIZERequest);
        commands.put("GAME?", this::treatGAMERequest);
        commands.put("LIST?", this::treatLISTRequest);
        commands.put("START", this::treatSTARTRequest);
    }

    private void sendDUNNO() {
        this.out.printf("DUNNO***");
    }

    @Override
    public void run() {
        String[] args = {"GAME?"};
        String request;

        // send GAMES n
        treatGAMERequest(args);

        // wait for player to send REGIS or NEWPL
        // if NEWPL id port***
        // create new Server.Server.Game (partie) and add this player to it
        // if REGIS id port m***
        // check if player is already in a game and the game does exist
        // let the player join the game
        // wait the player to send UNREG, SIZE?, GAME?, LIST? or START
        // in case of START, block the player, make him wait, how? (ignore his messages)

        while (true) { // while the player is still connected
            request = readRequest(this.in);
            if (request == null) { // the client is disconnected
                // remove the player from the list of players
                ServerImpl.INSTANCE.removeConnectedPlayer(this.player.getId());

                // remove the player from the game where he was
                if (this.player.getGame() != null) {
                    this.player.getGame().removePlayer(this.player);

                    // remove the game if it has no players left
                    if (this.player.getGame().getNbPlayers() == 0) {
                        ServerImpl.INSTANCE.removeGame(this.player.getGame());
                    }
                }

                System.out.println("Player [" + this.player.getId() + "] disconnected");
                break;
            }
            args = request.split(" ");
            commands.getOrDefault(args[0], (a -> sendDUNNO())).accept(args);
        }
    }

    private void treatNEWPLRequest(String[] args) {
        // NEWPL id port***
        if (args.length != 3) {
            sendDUNNO();
            return;
        }
        this.player.setId(args[1]);
        this.player.setPort(Integer.parseInt(args[2]));
        Game game = new Game();
        game.addPlayer(this.player);
        this.player.setGame(game);
        ServerImpl.INSTANCE.addNotStartedGame(game);
        this.out.printf("REGOK %c***", game.getId()); // send REGOK m
    }

    private void treatREGISRequest(String[] args) {
        // REGIS id port m***
        if (args.length != 4) {
            sendDUNNO();
            return;
        }
        this.player.setId(args[1]);
        this.player.setPort(Byte.parseByte(args[2]));
        byte m = Byte.parseByte(args[3]);
        if (!ServerImpl.INSTANCE.isNotStartedGame(m)) {
            this.out.printf("REGNO***");
            return;
        }
        ServerImpl.INSTANCE.addPlayerToGame(this.player, m);
        this.out.printf("REGOK %d***", m); // send REGOK m
    }

    private void treatUNREGRequest(String[] args) {
        // UNREG***
        if (args.length != 1) {  // TODO: see what happens when the player sends [UNREG ***] (a space after UNREG)
            sendDUNNO();
            return;
        }
        int m = this.player.getGame().getId();
        if (!this.player.unsubscribe()) {
            sendDUNNO();
            return;
        }
        // send UNROK m***
        this.out.printf("UNROK %d***", m);
    }


    private void treatSIZERequest(String[] args) {
        // SIZE? m***
        if (args.length != 2) {
            sendDUNNO();
            return;
        }
        byte m = Byte.parseByte(args[1]);
        Game g = ServerImpl.INSTANCE.getGame(m);
        if (g == null) {
            sendDUNNO();
            return;
        }
        // send SIZE! m h w***
        short h = g.getLabyrinthWidth();
        short w = g.getLabyrinthHeight();
        byte[] b1 = new byte[2], b2 = new byte[2];
        b1[0] = (byte) h;
        b1[1] = (byte) (h >> 8);
        b2[0] = (byte) w;
        b2[1] = (byte) (w >> 8);
        this.out.printf("SIZE! %c %c%c %c%c***", m, b1[0], b1[1], b2[0], b2[1]);
    }

    private void treatLISTRequest(String[] args) {
        // LIST? m***
        if (args.length != 2) {
            sendDUNNO();
            return;
        }
        //check if the game exists
        byte m = Byte.parseByte(args[1]);
        Game g = ServerImpl.INSTANCE.getGame(m);
        if (g == null) {
            sendDUNNO();
            return;
        }
        this.out.printf("LIST! %d %d***", m, g.getNbPlayers());
        g.forEachPlayer(p -> this.out.printf("PLAYR %s***", p.getId()));
    }

    private void treatGAMERequest(String[] args) {
        // send GAMES n
        if (args.length != 1) {
            sendDUNNO();
            return;
        }
        this.out.printf("GAMES %c***", (char) ServerImpl.INSTANCE.nbNotStartedGames());
        // send n OGAME
        ServerImpl.INSTANCE.forEachNotStartedGame(g -> this.out.printf("OGAME %c %c***", (char) g.getId(), (char) g.getNbPlayers()));
    }

    private void treatSTARTRequest(String[] args) {
//        if (args.length != 1) {
//            sendDUNNO();
//            return;
//        }
//        Game g = this.player.getGame();
//        if (g == null) {
//            sendDUNNO();
//            return;
//        }
//        this.player.sendSTART();

    }
}
