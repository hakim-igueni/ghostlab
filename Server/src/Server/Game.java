package Server;

import java.util.HashMap;
import java.util.function.Consumer;

public class Game {
    private static int nbGames = 0;
    private int id;
    private int nbPlayersWhoSentSTART = 0;
    private HashMap<String, Player> players = new HashMap<>();
    private boolean started = false;
    private Labyrinth labyrinth;

    public Game() {
        nbGames++;
        this.id = nbGames;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getNbPlayers() {
        return players.size();
    }

    public int getLabyrinthWidth() {
        return labyrinth.getWidth();
    }

    public int getLabyrinthHeight() {
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
