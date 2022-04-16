package Server;

public class Player {
    private int id;
    private String ipAddress;
    private int UDPPort;
    private Game game = null;

    public Player(int id, String ipAddress, int UDPPort) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.UDPPort = UDPPort;
    }

    public int getId() {
        return id;
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
}

