package Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

import static Server.Utils.readRequest;

public class PlayerHandler implements Runnable {
    private final HashMap<String, Consumer<String[]>> beforeGameSTARTCommands = new HashMap<>();
    private final HashMap<String, Consumer<String[]>> afterGameSTARTCommands = new HashMap<>();
    private final PrintWriter out;
    private final InputStreamReader in;
    private final Player player;
    private final Socket socket;

    public PlayerHandler(Socket s) throws IOException {
        this.socket = s;
        this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        this.in = new InputStreamReader(s.getInputStream());
        this.player = new Player(in, out);
        beforeGameSTARTCommands.put("NEWPL", this::treatNEWPLRequest);
        beforeGameSTARTCommands.put("REGIS", this::treatREGISRequest);
        beforeGameSTARTCommands.put("UNREG", this::treatUNREGRequest);
        beforeGameSTARTCommands.put("SIZE?", this::treatSIZERequest);
        beforeGameSTARTCommands.put("GAME?", this::treatGAMERequest);
        beforeGameSTARTCommands.put("LIST?", this::treatLISTRequest);
        beforeGameSTARTCommands.put("START", this::treatSTARTRequest);

        // todo: Si le joueur envoie un de ces messages alors que la partie est finie, la partie lui répond [GOBYE***] et ferme la connexion.
        afterGameSTARTCommands.put("UPMOV", this::treatUPMOVRequest);
        afterGameSTARTCommands.put("DOMOV", this::treatDOMOVRequest);
        afterGameSTARTCommands.put("LEMOV", this::treatLEMOVRequest);
        afterGameSTARTCommands.put("RIMOV", this::treatRIMOVRequest);
        afterGameSTARTCommands.put("GLIS?", this::treatGLISRequest);
        afterGameSTARTCommands.put("IQUIT", this::treatIQUITRequest);
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

        // before game starts
        while (!this.player.hasSentSTART()) { // while the player is still connected and didn't send START
            request = readRequest(this.in);
            if (request == null) { // the client is disconnected
                System.out.printf("[Server] Player %s disconnected\n\n", this.player.getId());
                this.player.unsubscribe();
                return; // exit the thread
            }
            args = request.split(" "); // TODO: check if the method split does exactly what we want
            beforeGameSTARTCommands.getOrDefault(args[0], (a -> sendDUNNO())).accept(args);
        }

        // after game starts
        while (true) {
            request = readRequest(this.in);
            if (request == null) { // the client is disconnected
                System.out.printf("[Server] Player %s disconnected\n\n", this.player.getId());
                this.player.unsubscribe();
                return; // exit the thread
            }
            args = request.split(" ");
            afterGameSTARTCommands.getOrDefault(args[0], (a -> sendDUNNO())).accept(args);
        }
    }

    public boolean isInvalidId(String id) { // todo: add this method to Utils
        return !id.matches("^[\\da-zA-Z ]{8}$");
    }

    public boolean isInvalidPort(String port) {
        return !port.matches("\\d{4}");
    }

    public boolean isInvalidd(String d) {
        return !d.matches("\\d{3}");
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
            if (this.player.getGame() != null) {
                System.out.printf("[Req-NEWPL] Error: Player %s is already in a game\n\n", this.player.getId());
                this.out.printf("REGNO***");
                return;
            }
            int port = Integer.parseInt(args[2]);
            this.player.setId(args[1]); // TODO: check if the id is already used, if so, send an error REGNO
            this.player.setPort(port);
            Game game = new Game();
            game.addPlayer(this.player);
            System.out.printf("[Req-NEWPL] Player %s requested to create a new game\n", this.player.getId());
            this.player.setGame(game);
            ServerImpl.INSTANCE.addNotStartedGame(game);

            // send REGOK m***
            this.out.printf("REGOK %c***", game.getId());
            System.out.printf("[Ans-NEWPL] Player %s successfully created a new game and joined it\n\n", this.player.getId());
        } catch (Exception e) {
            System.out.printf("[Req-NEWPL] Error: %s\n\n", e.getMessage());
            sendDUNNO();
        }
    }

    private void treatREGISRequest(String[] args) {
        // REGIS id port m***
        try {
            if (args.length != 4) {
                throw new Exception("REGIS request must have 3 arguments: REGIS id port m");
            }
            if (isInvalidId(args[1])) {
                throw new Exception("ID must have 8 alphanumeric characters");
            }
            if (isInvalidPort(args[2])) {
                throw new Exception("Port must have 4 digits");
            }
            if (this.player.getGame() != null) {
                System.out.printf("[Req-REGIS] Error: Player %s is already in a game\n\n", this.player.getId());
                this.out.printf("REGNO***");
                return;
            }
            String id = args[1]; // TODO: check if the id is already used, if so, send an error REGNO
            int port = Integer.parseInt(args[2]);
            this.player.setId(id);
            this.player.setPort(port);
            byte m = args[3].getBytes()[0]; // TODO: check if args[3] is a single byte
            System.out.printf("[Req-REGIS] Player %s is trying to join game %d\n", id, m);
            if (!ServerImpl.INSTANCE.isNotStartedGame(m)) {
                System.out.printf("[Req-REGIS] Error: Game %d is already started\n", m);
                this.out.printf("REGNO***");
                return;
            }
            ServerImpl.INSTANCE.addPlayerToGame(this.player, m);
            this.player.setGame(ServerImpl.INSTANCE.getGame(m));

            // send REGOK m***
            this.out.printf("REGOK %c***", m);
            System.out.printf("[Ans-REGIS] Player %s joined game %d\n\n", id, m);
        } catch (Exception e) {
            System.out.printf("[Req-REGIS] Error: %s\n\n", e.getMessage());
            sendDUNNO();
        }
    }

    private void treatUNREGRequest(String[] args) {
        // UNREG***
        try {
            System.out.printf("[Req-UNREG] Player %s requested to unregister\n", this.player.getId());
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
            System.out.printf("[Ans-UNREG] Player %s unregistered from game %d\n\n", this.player.getId(), m);
        } catch (Exception e) {
            System.out.printf("[Req-UNREG] Error: %s\n\n", e.getMessage());
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
            System.out.printf("[Req-SIZE?] Player %s requested labyrinth size of game %d\n", this.player.getId(), m);

            // send SIZE! m h w***
            short h = g.getLabyrinthWidth();
            short w = g.getLabyrinthHeight();
            byte h0, h1, w0, w1;
            h0 = (byte) h; // lowest weight byte
            h1 = (byte) (h >> 8); // strongest weight byte
            w0 = (byte) w; // lowest weight byte
            w1 = (byte) (w >> 8); // strongest weight byte
            this.out.printf("SIZE! %c %c%c %c%c***", m, h0, h1, w0, w1); // send SIZE! m h w***
            System.out.printf("[Ans-SIZE?] Labyrinth size: (w=%d, h=%d) of game %d sent to player %s\n\n", w, h, m, this.player.getId());
        } catch (Exception e) {
            System.out.printf("[Req-SIZE?] Error: %s\n\n", e.getMessage());
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
            System.out.printf("[Req-LIST?] Player %s requested list of players of game %d\n", this.player.getId(), m);
            Game g = ServerImpl.INSTANCE.getGame(m);
            if (g == null) {
                throw new Exception("Game does not exist");
            }

            // send LIST! m s***
            this.out.printf("LIST! %c %c***", m, g.getNbPlayers());
            System.out.printf("[Ans-LIST?] List of players of game %d sent to player %s\n", m, this.player.getId());
            g.forEachPlayer(p -> {
                p.sendPLAYR(this.out);
                System.out.printf("[Ans-LIST?] PLAYR %s\n", p.getId());
            });

            System.out.println();
        } catch (Exception e) {
            System.out.printf("[Req-LIST?] Error: %s\n\n", e.getMessage());
            sendDUNNO();
        }
    }

    private void treatGAMERequest(String[] args) {
        // GAME?***
        try {
            if (args.length != 1) {
                throw new Exception("GAMES request must have 0 arguments");
            }
            System.out.printf("[Req-GAMES] Player %s requested list of not started games\n", this.player.getId());

            // send GAMES n
            this.out.printf("GAMES %c***", ServerImpl.INSTANCE.nbNotStartedGames());
            // send n OGAME
            System.out.printf("[Ans-GAMES] List of not started games sent to player %s\n", this.player.getId());
            ServerImpl.INSTANCE.forEachNotStartedGame(g -> {
                g.sendOGAME(this.out);
                System.out.printf("[Ans-GAMES] OGAME %d %d\n", g.getId(), g.getNbPlayers());
            });
            System.out.println();
        } catch (Exception e) {
            System.out.printf("[Req-GAMES] Error: %s\n\n", e.getMessage());
            sendDUNNO();
        }
    }

    private void treatSTARTRequest(String[] args) {
        // block the player, make him wait, how? (ignore his messages maybe?)
        try {
            if (args.length != 1) {
                throw new Exception("START request must have 0 arguments");
            }
            System.out.printf("[Req-START] Player %s requested to start a game\n", this.player.getId());
            Game g = this.player.getGame();
            if (g == null) {
                throw new Exception("Player is not in a game");
            }
            System.out.printf("[Ans-START] Player %s is waiting for game %d to start\n", this.player.getId(), g.getId());
            this.player.pressSTART();
            System.out.printf("[Ans-START] Player %s is now in game %d\n", this.player.getId(), g.getId());
        } catch (Exception e) {
            System.out.printf("[Req-START] Error: %s\n", e.getMessage());
            sendDUNNO();
        }
    }

    private void treatUPMOVRequest(String[] args) {
        // UPMOV d***
        try {
            // Verify if the request has one argument
            if (args.length != 2) {
                throw new Exception("UPMOV request must have 1 argument: UPMOV d");
            }
            if (isInvalidd(args[1])) {
                throw new Exception("d must have 3 characters");
            }
            int d = Integer.parseInt(args[1]);

            //get position of the player
            int y = this.player.getCol();
            int x = this.player.getRow();

            int dist = x - d;
            if (dist < 0) {
                throw new Exception("The distance" + dist + "can not be traversed");
            } else {
                this.player.setRow(dist);
                this.out.printf("MOVE! %03d %03d***", x, y);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void treatDOMOVRequest(String[] args) {
        // DOMOV d***
        try {
            // Verify if the request has one argument
            if (args.length != 2) {
                throw new Exception("UPMOV request must have 1 argument: UPMOV d");
            }
            if (isInvalidd(args[1])) {
                throw new Exception("d must have 3 characters");
            }
            int d = Integer.parseInt(args[1]);

            //get position of the player
            int y = this.player.getCol();
            int x = this.player.getRow();

            int dist = x + d;
            if (dist >= this.player.getGame().getLabyrinthHeight()) {
                throw new Exception("The distance" + dist + "can not be traversed");
            } else {
                this.player.setRow(dist);
                this.out.printf("MOVE! %03d %03d***", x, y);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void treatRIMOVRequest(String[] args) {
        // RIMOV d***
        try {
            // Verify if the request has one argument
            if (args.length != 2) {
                throw new Exception("RIMOV request must have 1 argument: RIMOV d");
            }
            if (isInvalidd(args[1])) {
                throw new Exception("d must have 3 characters");
            }
            int d = Integer.parseInt(args[1]);

            //get position of the player
            int y = this.player.getCol();
            int x = this.player.getRow();

            int dist = y + d;
            if (dist >= this.player.getGame().getLabyrinthWidth()) {
                throw new Exception("The distance" + dist + "can not be traversed");
            } else {
                this.player.setRow(dist);
                this.out.printf("MOVE! %03d %03d***", x, y);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void treatLEMOVRequest(String[] args) {
        // LEMOV d***
        try {
            // Verify if the request has one argument
            if (args.length != 2) {
                throw new Exception("LEMOV request must have 1 argument: LEMOV d");
            }
            if (isInvalidd(args[1])) {
                throw new Exception("d must have 3 characters");
            }
            int d = Integer.parseInt(args[1]);

            //get position of the player
            int y = this.player.getCol();
            int x = this.player.getRow();

            int dist = y - d;
            if (dist < 0) {
                throw new Exception("The distance" + dist + "can not be traversed");
            } else {
                this.player.setRow(dist);
                this.out.printf("MOVE! %03d %03d***", x, y);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void treatGLISRequest(String[] args) {
        // GLIS?***
        try {
            if (args.length != 2) {
                throw new Exception("GLIS? request must have 1 argument: LIST? m");
            }
            System.out.printf("[Req-GLIS?] Player %s requested list of players of his game\n", this.player.getId());
            Game g = this.player.getGame();
            if (g == null) {
                throw new Exception("Player is not in a game");
            }

            // send GPLYR id x y p***
            this.out.printf("GLIS! %c***", g.getNbPlayers());
            System.out.printf("[Ans-GLIS?] List of players of game %d sent to player %s\n", g.getId(), this.player.getId());
            g.forEachPlayer(p -> {
                p.sendGPLYR(this.out);
                System.out.printf("[Ans-GLIS?] GPLYR %s %04d %04d %04d\n", p.getId(), p.getRow(), p.getCol(), p.getScore());
            });

            System.out.println();
        } catch (Exception e) {
            System.out.printf("[Req-GLIS?] Error: %s\n\n", e.getMessage());
            sendDUNNO();
        }
    }

    private void treatIQUITRequest(String[] args) {
        // IQUIT***
        try {
            if (args.length != 1) {
                throw new Exception("IQUIT request must have 0 arguments");
            }
            System.out.printf("[Req-IQUIT] Player %s requested to quit the party %d\n", this.player.getId(), this.player.getGame().getId());
            Game g = this.player.getGame();
            if (g == null) {
                throw new Exception("Player is not in a game");
            }
            this.player.unsubscribe();

            // send GOBYE***
            this.out.printf("GOBYE***");
            // close the connection
            this.socket.close();
            System.out.printf("[Ans-IQUIT] Player %s left the game %d\n", this.player.getId(), g.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
