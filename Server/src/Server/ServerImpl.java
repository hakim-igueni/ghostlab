package Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class ServerImpl {
    // The server's state.
    public static final int MAX_GAMES = 256; // Maximum number of games
    public static final int MAX_PLAYERS = 256; // Maximum number of players per game
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

    public synchronized boolean addPlayerToGame(Player player, byte gameId) {
        Game game = notStartedGames.get(gameId);
        if (game != null) {
            if (game.getNbPlayers() < ServerImpl.MAX_PLAYERS) {
                game.addPlayer(player);
            } else {
                System.out.println("Too many players!!!!!!!!!!!!!");
                return false;
//                throw new RuntimeException("Too many players"); // TODO: treat this properly
            }
        }
        return true;
    }

    public synchronized void startGame(Game game) {
        notStartedGames.remove(game.getId());
        startedGames.put(game.getId(), game);
    }

    public synchronized boolean addNotStartedGame(Game game) {
//        if (notStartedGames.size() < 42) {
        if (notStartedGames.size() <= ServerImpl.MAX_GAMES) {
            notStartedGames.put(game.getId(), game);
        } else {
            System.out.println("Too many games!!!!!!!!!!!!!");
            return false;
//            throw new RuntimeException("Too many games"); // TODO: treat this properly
        }
        return true;
    }

    public void removeGame(Game game) {
        if (game.isStarted()) {
            startedGames.remove(game.getId());
        } else {
            notStartedGames.remove(game.getId());
        }
        Game.addAvailableGameId(game.getId());
        System.out.printf("Game %d removed.\n", Byte.toUnsignedInt(game.getId()));
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
