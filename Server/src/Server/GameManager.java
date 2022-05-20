package Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GameManager implements Runnable {
    // TODO: avoid multicast addresses that start with 224, 232, 233 et 239
    // TODO: make sure that a multicast address is a class D address (first byte is between 224 and 239)
    private static InetAddress lastGivenMulticastAddress;
    private static int lastGivenMulticastPort;

    static { // initialize the last given multicast address and port
        try {
            lastGivenMulticastAddress = InetAddress.getByName("225.0.0.0");
            lastGivenMulticastPort = 1024;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private final Game game;
    private final InetAddress ipMulticast;
    private final int portMulticast;

    public int getPortMulticast() {
        return portMulticast;
    }

    public InetAddress getIpMulticast() {
        return ipMulticast;
    }

    public GameManager(Game game) {
        this.game = game;
        this.ipMulticast = getNextMulticastAddress();
        this.portMulticast = getNextMulticastPort();
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

    public void sendWELCOtoAllPlayers() {
        game.forEachPlayer(player -> player.sendWELCO(ipMulticast, portMulticast));
    }

    public void sendPOSITtoAllPlayers() {
        // TODO: generate the positions of the players randomly
        // todo: make sure to respect the rules of the game (not place the player on a wall, not place the player on another player, ...)
        // generate a random number
        int x = (int) (Math.random() * game.getLabyrinthHeight());
        int y = (int) (Math.random() * game.getLabyrinthWidth());
        // send the POSIT message to all players
        game.forEachPlayer(player -> player.sendPOSIT(x, y));
    }

    @Override
    public void run() {
        // send [WELCO m h w f ip port***] to all players
        sendWELCOtoAllPlayers();
        // send [POSIT x y] to all players
        sendPOSITtoAllPlayers();
//        while (true) {
//            break;
//        }
    }
}
