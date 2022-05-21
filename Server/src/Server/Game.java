package Server;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class Game {
    private static int nbGames = 0;
    private static InetAddress lastGivenMulticastAddress;
    private static int lastGivenMulticastPort;

    // this HashSet is used to store the available game ids and reuse them when a game is over
    static {
        try {
            lastGivenMulticastAddress = InetAddress.getByName("225.0.0.0");
            lastGivenMulticastPort = 1024;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private final HashMap<String, Player> players = new HashMap<>();
    private final HashMap<String, Player> playersWhoDidntSendSTART = new HashMap<>();
    private final Labyrinth labyrinth;
    private final byte id;
    private boolean started = false; // todo: does it need to be volatile?
    private volatile boolean finished = false; // todo: does it need to be volatile?
    private int maxScore = 0;
    private InetAddress ipMulticast;
    private int portMulticast;

    public Game() {
        this.id = Game.nextAvailableGameId();
        labyrinth = new Labyrinth(this);
    }

    public synchronized static byte nextAvailableGameId() {
//        if (availableGameIds.size() == 0) {
//            throw new RuntimeException("No more game ids available");
//        }
        byte id = (byte) nbGames;
        nbGames++;
        return id;
    }

    public int getMaxScore() {
        return maxScore;
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

    public void setMaxScore() {
        int maxScore = 0;
        for (Player player : players.values()) {
            if (player.getScore() > maxScore) {
                maxScore = player.getScore();
            }
        }
        this.maxScore = maxScore;
    }

    public HashSet<String> getWinners() {
        // get winners
        HashSet<String> winners = new HashSet<>();
        for (Player player : players.values()) {
            if (player.getScore() == maxScore) {
                winners.add(player.getId());
            }
        }
        return winners;
    }

    public synchronized void removeFromPlayersWhoDidntSendSTART(Player player) {
        playersWhoDidntSendSTART.remove(player.getId());
        if (playersWhoDidntSendSTART.size() == 0) {
            startGame();
        }
    }

    private int getNextMulticastPort() { // todo: check if the port is already used
        lastGivenMulticastPort++;
        if (lastGivenMulticastPort == 10000) {
            throw new RuntimeException("No more multicast ports available"); // TODO: treat this properly
        }
        return lastGivenMulticastPort;
    }

    /* Generate the multicast address for the game */
    private InetAddress getNextMulticastAddress() { // todo: check if the address is already used 	isReachable
        byte[] lastMCAdd = lastGivenMulticastAddress.getAddress();
        lastMCAdd[3]++;
        if (lastMCAdd[3] == 255) {
            lastMCAdd[2]++;
            lastMCAdd[3] = 0;
            if (lastMCAdd[2] == 255) {
                lastMCAdd[1]++;
                lastMCAdd[2] = 0;
                if (lastMCAdd[1] == 255) {
                    lastMCAdd[0]++;
                    if (lastMCAdd[0] == 232) {
                        lastMCAdd[0] += 2;
                    } else if (lastMCAdd[0] == 239) {
                        throw new RuntimeException("No more multicast addresses available"); // TODO: treat this properly
                    }
                    lastMCAdd[1] = 0;
                }
            }
        }
        try {
            lastGivenMulticastAddress = InetAddress.getByAddress(lastMCAdd);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e); // should never happen
        }
        return lastGivenMulticastAddress;
    }

    public synchronized void startGame() {
        this.started = true;
        ServerImpl.INSTANCE.startGame(this);
        ipMulticast = getNextMulticastAddress();
        portMulticast = getNextMulticastPort();
        sendWELCOtoAllPlayers();
        sendPOSITtoAllPlayers();
        Thread t = new Thread(labyrinth); // TODO: check if we really need an attribute for this
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
        // remove the game if it has no players left
        if (getNbPlayers() == 0) {
            ServerImpl.INSTANCE.removeGame(this);
            finished = true;
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

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public boolean isRunning() {
        return !finished;
    }

    public void finishGame() {
        finished = true;
        ServerImpl.INSTANCE.removeGame(this);
    }

    public boolean isFinished() {
        return finished;
    }

    public void sendWELCOtoAllPlayers() {
        forEachPlayer(player -> player.sendWELCO(ipMulticast, portMulticast));
    }

    public void sendPOSITtoAllPlayers() {
        // todo: make sure to respect the rules of the game (not place the player on a wall, not place the player on another player, ...)
        // send the POSIT message to all players
        forEachPlayer(player -> {
            getLabyrinth().placePlayer(player);
            player.sendPOSIT();
        });
    }

    public InetAddress getIpMulticast() {
        return ipMulticast;
    }

    public int getPortMulticast() {
        return portMulticast;
    }
}
