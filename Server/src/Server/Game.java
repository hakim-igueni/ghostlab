package Server;

import java.util.HashMap;
import java.util.function.Consumer;

public class Game {
    private static byte nbGames = 0;
    private byte id;
    private byte nbPlayersWhoSentSTART = 0;
    private HashMap<String, Player> players = new HashMap<>();
    private boolean started = false;
    private Labyrinth labyrinth;

    public Game() {
        nbGames++;
        this.id = nbGames;
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public void forEachPlayer(Consumer<Player> action) {
        players.forEach((id, player) -> action.accept(player));
    }

    public void setPlayers(HashMap<String, Player> players) {
        this.players = players;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public byte getNbPlayers() {
        return (byte) players.size();
    }

    public short getLabyrinthWidth() {
        return labyrinth.getWidth();
    }

    public short getLabyrinthHeight() {
        return labyrinth.getHeight();
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
}
