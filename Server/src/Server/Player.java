package Server;

public class Player {
    private String id;
    private String ipAddress;
    private int UDPPort;
    private Game game = null;

    public Player(String id, String ipAddress, int UDPPort) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.UDPPort = UDPPort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getUDPPort() {
        return UDPPort;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean unsubscribe() {
        if (game != null) {
            game.removePlayer(this);
            game = null;
            return true;
        }
        return false;
    }
}

