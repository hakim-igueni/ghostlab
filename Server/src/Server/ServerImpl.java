package Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class ServerImpl {
    // The server's state.
    public static final ServerImpl INSTANCE = new ServerImpl();
    private final HashSet<String> connectedPlayers = new HashSet<>();
    private final HashMap<Byte, Game> notStartedGames = new HashMap<>();
    private final HashMap<Byte, Game> startedGames = new HashMap<>();

    private ServerImpl() {
    }

    public void addPlayer(String playerName) {
        connectedPlayers.add(playerName);
    }

    public void removePlayer(String playerName) {
        connectedPlayers.remove(playerName);
    }

    public boolean isPlayerConnected(String playerName) {
        return connectedPlayers.contains(playerName);
    }

    public synchronized void addPlayerToGame(Player player, byte gameId) {
        Game game = notStartedGames.get(gameId);
        if (game != null) {
            game.addPlayer(player);
        }
    }

    public synchronized void startGame(Game game) {
        notStartedGames.remove(game.getId());
        startedGames.put(game.getId(), game);
    }

    public synchronized void addNotStartedGame(Game game) {
//        if (notStartedGames.size() < 42) {
        if (notStartedGames.size() <= Game.MAX_GAMES) {
            notStartedGames.put(game.getId(), game);
        } else {
            System.out.println("Too many games!!!!!!!!!!!!!");
            throw new RuntimeException("Too many games"); // TODO: treat this properly
        }
    }

    public void removeGame(Game game) {
        if (game.isStarted()) {
            startedGames.remove(game.getId());
        } else {
            notStartedGames.remove(game.getId());
        }
        Game.addAvailableGameId(game.getId());
        System.out.printf("Game %s removed.\n", game.getId());
    }

    public Game getGame(byte id) {
        if (startedGames.containsKey(id)) {
            return startedGames.get(id);
        }
        return notStartedGames.get(id);
    }

    public synchronized boolean isNotStartedGame(byte id) {
        return notStartedGames.containsKey(id);
    }

    public synchronized byte nbNotStartedGames() {
        return (byte) notStartedGames.size();
    }

    public synchronized void forEachNotStartedGame(Consumer<Game> consumer) {
        notStartedGames.forEach((k, v) -> consumer.accept(v));
    }
}
