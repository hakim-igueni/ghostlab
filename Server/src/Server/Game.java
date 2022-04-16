package Server;

import java.util.HashMap;

public class Game {
    private int id;
    private int nbPlayersWhoSentSTART = 0;
    private HashMap<String, Player> players = new HashMap<String, Player>();
    private boolean started = false;
    private Labyrinth labyrinth;

    public Game(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNbPlayersWhoSentSTART() {
        return nbPlayersWhoSentSTART;
    }

    public void setNbPlayersWhoSentSTART(int nbPlayersWhoSentSTART) {
        this.nbPlayersWhoSentSTART = nbPlayersWhoSentSTART;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
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

    public Labyrinth getLabyrinth() {
        return labyrinth;
    }
}
