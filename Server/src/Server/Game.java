package Server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.function.Consumer;

public class Game {
    private volatile static byte nbGames = 0;
    private final HashMap<String, Player> players = new HashMap<>();
    private final HashMap<String, Player> playersWhoDidntSendSTART = new HashMap<>();
    private final Labyrinth labyrinth;
    private final byte id;
    private boolean started = false;
    private Thread gameManagerThread;

    public Game() {
        Game.incrNbGames();
        this.id = nbGames;
        labyrinth = new Labyrinth((short) 10, (short) 10);
    }

    public synchronized static void incrNbGames() {
        nbGames++;
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
        GameManager gameManager = new GameManager(this);
        this.gameManagerThread = new Thread(gameManager); // TODO: check if we really need an attribute for this
        this.gameManagerThread.start();
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
}
