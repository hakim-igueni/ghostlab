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
    private final HashMap<String, Consumer<String[]>> commands = new HashMap<>();
    private final PrintWriter out;
    private final InputStreamReader in;
    private final Player player;

    public WelcomePlayerService(Socket s) throws IOException {
        this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        this.in = new InputStreamReader(s.getInputStream());
        this.player = new Player(in, out);
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

        while (true) { // while the player is still connected
            request = readRequest(this.in);
            if (request == null) { // the client is disconnected
                // remove the player from the list of players
//                ServerImpl.INSTANCE.removeConnectedPlayer(this.player.getId());
                // remove the player from the game where he was
                if (this.player.getGame() != null) {
                    this.player.getGame().removePlayer(this.player);

                    // remove the game if it has no players left
                    if (this.player.getGame().getNbPlayers() == 0) {
                        ServerImpl.INSTANCE.removeGame(this.player.getGame());
                    }
                }
                break;
            }
            args = request.split(" "); // TODO: check if the method split does exactly what we want
            commands.getOrDefault(args[0], (a -> sendDUNNO())).accept(args);
        }
    }

    public boolean isInvalidId(String id) {
        return !id.matches("^[\\da-zA-Z ]{8}$");
    }

    public boolean isInvalidPort(String port) {
        return !port.matches("\\d{4}");
    }

    private void treatNEWPLRequest(String[] args) {
        // NEWPL id port***
        try {
            if (args.length != 3) {
                throw new Exception("NEWPL request must have 3 arguments");
            }
            String id = args[1];
            if (isInvalidId(id)) {
                throw new Exception("ID must have 8 alphanumeric characters");
            }
            if (isInvalidPort(args[2])) {
                throw new Exception("Port must have 4 digits");
            }
            int port = Integer.parseInt(args[2]);
            this.player.setId(args[1]);
            this.player.setPort(port);
            Game game = new Game();
            game.addPlayer(this.player);
            this.player.setGame(game);
            ServerImpl.INSTANCE.addNotStartedGame(game);
            this.out.printf("REGOK %c***", game.getId()); // send REGOK m***
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }

    private void treatREGISRequest(String[] args) {
        // REGIS id port m***
        try {
            if (args.length != 4) {
                throw new Exception("REGIS request must have 3 arguments: REGIS id port m");
            }
            String id = args[1];
            if (isInvalidId(id)) {
                throw new Exception("ID must have 8 alphanumeric characters");
            }
            int port = Integer.parseInt(args[2]);
            this.player.setId(id);
            this.player.setPort(port);
            byte m = args[3].getBytes()[0]; // TODO: check if args[3] is a single byte
            if (!ServerImpl.INSTANCE.isNotStartedGame(m)) {
                this.out.printf("REGNO***");
                return;
            }
            ServerImpl.INSTANCE.addPlayerToGame(this.player, m);
            this.out.printf("REGOK %c***", m); // send REGOK m
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }

    private void treatUNREGRequest(String[] args) {
        // UNREG***
        try {
            if (args.length != 1) { // TODO: see what happens when the player sends [UNREG ***] (a space after UNREG)
                throw new Exception("UNREG request must have 0 arguments");
            }
            byte m = this.player.getGame().getId();
            if (!this.player.unsubscribe()) {
                sendDUNNO();
                return;
            }
            // send UNROK m***
            this.out.printf("UNROK %c***", m);
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }


    private void treatSIZERequest(String[] args) {
        // SIZE? m***
        try {
            if (args.length != 2) {
                throw new Exception("SIZE? request must have 1 argument: SIZE? m");
            }
            byte m = args[1].getBytes()[0]; // TODO: check if args[1] is a single byte
            Game g = ServerImpl.INSTANCE.getGame(m);
            if (g == null) {
                throw new Exception("Game does not exist");
            }

            // send SIZE! m h w***
            short h = g.getLabyrinthWidth();
            short w = g.getLabyrinthHeight();
            byte h0, h1, w0, w1;
            h0 = (byte) h; // lowest weight byte
            h1 = (byte) (h >> 8); // strongest weight byte
            w0 = (byte) w; // lowest weight byte
            w1 = (byte) (w >> 8); // strongest weight byte
            this.out.printf("SIZE! %c %c%c %c%c***", m, h0, h1, w0, w1); // send SIZE! m h w***
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }

    private void treatLISTRequest(String[] args) {
        // LIST? m***
        try {
            if (args.length != 2) {
                throw new Exception("LIST? request must have 1 argument: LIST? m");
            }
            //check if the game exists
            byte m = args[1].getBytes()[0]; // TODO: check if args[1] is a single byte
            Game g = ServerImpl.INSTANCE.getGame(m);
            if (g == null) {
                throw new Exception("Game does not exist");
            }
            this.out.printf("LIST! %c %d***", m, g.getNbPlayers()); // send LIST! m s***
            g.forEachPlayer(p -> p.sendPLAYR(this.out));
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }

    private void treatGAMERequest(String[] args) {
        // send GAMES n
        try {
            if (args.length != 1) {
                throw new Exception("GAMES request must have 0 arguments");
            }
            this.out.printf("GAMES %c***", ServerImpl.INSTANCE.nbNotStartedGames());
            // send n OGAME
            ServerImpl.INSTANCE.forEachNotStartedGame(g -> g.sendOGAME(this.out));
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }

    private void treatSTARTRequest(String[] args) {
        // block the player, make him wait, how? (ignore his messages)
        try {
            if (args.length != 1) {
                throw new Exception("START request must have 0 arguments");
            }
            Game g = this.player.getGame();
            if (g == null) {
                throw new Exception("Player is not in a game");
            }
            // TODO: block the player
            this.player.start();
        } catch (Exception e) {
            e.printStackTrace();
            sendDUNNO();
        }
    }
}
