package Server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class Game {
    public static final int MAX_GAMES = 256; // Maximum number of games
    private static final HashSet<Byte> availableGameIds = new HashSet<>();

    // this HashSet is used to store the available game ids and reuse them when a game is over
    static {
        for (int i = 0; i <= 255; i++) {
            availableGameIds.add((byte) i);
        }
    }

    private final HashMap<String, Player> players = new HashMap<>();
    private final HashMap<String, Player> playersWhoDidntSendSTART = new HashMap<>();
    private final Labyrinth labyrinth;
    private final byte id;
    private boolean started = false;
    private GameManager gameManager;


    public Game() {
        this.id = (byte) Game.nextAvailableGameId();
        labyrinth = new Labyrinth();
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public synchronized static int nextAvailableGameId() {
        if (availableGameIds.size() == 0) {
            throw new RuntimeException("No more game ids available");
        }
        byte id = availableGameIds.iterator().next();
        availableGameIds.remove(id);
        return id;
    }

    public synchronized static void addAvailableGameId(byte id) {
        availableGameIds.add(id);
    }

    public byte getId() {
        return id;
    }

    public synchronized void forEachPlayer(Consumer<Player> action) {
        players.forEach((id, player) -> action.accept(player));
    }

    public boolean isStarted() {
        return started;
    }


    public synchronized void removeFromPlayersWhoDidntSendSTART(Player player) {
        playersWhoDidntSendSTART.remove(player.getId());

        if (playersWhoDidntSendSTART.size() == 0) {
            startGame();
        }
    }

    public synchronized void startGame() {
        this.started = true;
        ServerImpl.INSTANCE.startGame(this);
        this.gameManager = new GameManager(this);
        Thread t = new Thread(gameManager); // TODO: check if we really need an attribute for this
        t.start();
    }

    public synchronized byte getNbPlayers() {
        return (byte) players.size();
    }

    public short getLabyrinthWidth() {
        return labyrinth.getWidth();
    }

    public short getLabyrinthHeight() {
        return labyrinth.getHeight();
    }

    public byte getNbGhosts() {
        return labyrinth.getNbGhosts();
    }

    public void removePlayer(Player player) {
        players.remove(player.getId());
        if (player.hasSentSTART()) {
            playersWhoDidntSendSTART.remove(player.getId());
        }
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
        if (!player.hasSentSTART()) {
            playersWhoDidntSendSTART.put(player.getId(), player);
        }
    }

    public void sendOGAME(PrintWriter out) {
        out.printf("OGAME %c %c***", this.id, getNbPlayers());
    }

    public Labyrinth getLabyrinth() {
        return labyrinth;

    }
}
