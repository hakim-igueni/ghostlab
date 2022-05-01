package Server;

import java.util.HashMap;
import java.util.function.Consumer;

public class ServerImpl {
    // The server's state.
    public static final ServerImpl INSTANCE = new ServerImpl();
    private final HashMap<String, Player> connectedPlayers = new HashMap<>();
    private final HashMap<Byte, Game> notStartedGames = new HashMap<>();
    private final HashMap<Byte, Game> startedGames = new HashMap<>();

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

    public void addPlayerToGame(Player player, byte gameId) {
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

    public Game getGame(byte id) {
        if (startedGames.containsKey(id)) {
            return startedGames.get(id);
        }
        return notStartedGames.get(id);
    }

    public Game getNotStartedGame(byte id) {
        return notStartedGames.get(id);
    }

    public void addStartedGame(Game game) {
        notStartedGames.remove(game.getId());
        startedGames.put(game.getId(), game);
    }

    public void removeStartedGame(byte id) {
        startedGames.remove(id);
    }

    public void removeNotStartedGame(byte id) {
        notStartedGames.remove(id);
    }

    public boolean isPlayerConnected(String name) {
        return connectedPlayers.containsKey(name);
    }

    public boolean isStartedGame(byte id) {
        return startedGames.containsKey(id);
    }

    public boolean isNotStartedGame(byte id) {
        return notStartedGames.containsKey(id);
    }

    public int nbStartedGames() {
        return startedGames.size();
    }

    public byte nbNotStartedGames() {
        return (byte) notStartedGames.size();
    }

    public void forEachNotStartedGame(Consumer<Game> consumer) {
        notStartedGames.forEach((k, v) -> consumer.accept(v));
    }
}
