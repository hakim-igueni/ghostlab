package Server;

import java.util.HashMap;
import java.util.function.Consumer;

public class ServerImpl {
    // The server's state.
    public static final ServerImpl INSTANCE = new ServerImpl();
    private final HashMap<String, Player> connectedPlayers = new HashMap<>();
    private final HashMap<Integer, Game> notStartedGames = new HashMap<>();
    private final HashMap<Integer, Game> startedGames = new HashMap<>();

    private ServerImpl() {
    }

    public void addPlayer(String name, Player player) {
        connectedPlayers.put(name, player);
    }

    public void removeConnectedPlayer(String name) {
        connectedPlayers.remove(name);
    }

    public Player getPlayer(String name) {
        return connectedPlayers.get(name);
    }

    public void addPlayerToGame(Player player, int gameId) {
        Game game = notStartedGames.get(gameId);
        if (game != null) {
            game.addPlayer(player);
        }
    }

    public void addNotStartedGame(Game game) {
        if (notStartedGames.size() < 42) {
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
    }

    public Game getGame(int id) {
        if (startedGames.containsKey(id)) {
            return startedGames.get(id);
        }
        return notStartedGames.get(id);
    }

    public Game getNotStartedGame(int id) {
        return notStartedGames.get(id);
    }

    public void addStartedGame(Game game) {
        notStartedGames.remove(game.getId());
        startedGames.put(game.getId(), game);
    }

    public void removeStartedGame(int id) {
        startedGames.remove(id);
    }

    public void removeNotStartedGame(int id) {
        notStartedGames.remove(id);
    }

    public boolean isPlayerConnected(String name) {
        return connectedPlayers.containsKey(name);
    }

    public boolean isStartedGame(int id) {
        return startedGames.containsKey(id);
    }

    public boolean isNotStartedGame(int id) {
        return notStartedGames.containsKey(id);
    }

    public int nbStartedGames() {
        return startedGames.size();
    }

    public int nbNotStartedGames() {
        return notStartedGames.size();
    }

    public void forEachNotStartedGame(Consumer<Game> consumer) {
        notStartedGames.forEach((k, v) -> consumer.accept(v));
    }
}
