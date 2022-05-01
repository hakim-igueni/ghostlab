package Server;

public class Player {
    private String id;
    private String ipAddress;
    private int UDPPort;
    private Game game = null;
    private boolean hasSentSTART = false;

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

    public boolean hasSentSTART() {
        return hasSentSTART;
    }

    public void sendSTART() {
        this.hasSentSTART = true;
    }


    public boolean unsubscribe() {
        if (this.game != null) {
            this.game.removePlayer(this);
            this.game = null;
            return true;
        }
        return false;
    }

    public int getPort(int port) {
        return UDPPort;
    }

    public void setPort(int port) {
        this.UDPPort = port;
    }

}

