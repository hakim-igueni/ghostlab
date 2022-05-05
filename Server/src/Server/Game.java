package Server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.function.Consumer;

public class Game {
    private volatile static byte nbGames = 0;
    private final HashMap<String, Player> players = new HashMap<>();
    private final Labyrinth labyrinth;
    private final byte id;
    private byte nbPlayersWhoSentSTART = 0;
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

    public synchronized void incrNbPlayersWhoSentSTARTAndWait() {
        nbPlayersWhoSentSTART++;
        if (nbPlayersWhoSentSTART == players.size()) {
            startGame();
        }
        try {
            System.out.println(Thread.currentThread().getName() + ": waiting for game to start");
            wait(); // wait for other players to send START to start the game
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void startGame() {
        this.started = true;
        notifyAll();
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
            nbPlayersWhoSentSTART--;
        }
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public void sendOGAME(PrintWriter out) {
        out.printf("OGAME %c %c***", this.id, getNbPlayers());
    }
}
