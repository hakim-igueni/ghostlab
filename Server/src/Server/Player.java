package Server;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;

public class Player {
    private final PrintWriter out;
    private final InputStreamReader in;
    private String id;
    private int UDPPort;
    private Game game = null;
    private boolean hasSentSTART = false;
    private int score = 0;
    private int col;
    private int row;

    public Player(InputStreamReader in, PrintWriter out) {
        this.out = out;
        this.in = in;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void pressSTART() {
        this.hasSentSTART = true;
        this.game.removeFromPlayersWhoDidntSendSTART(this);
    }

    public int getPort(int port) {
        return UDPPort;
    }

    public void setPort(int port) {
        this.UDPPort = port;
    }

    public void sendWELCO(InetAddress ipMulticast, int portMulticast) {
        // send [WELCO m h w f ip port***]
        short h = this.game.getLabyrinthWidth();
        short w = this.game.getLabyrinthHeight();
        byte h0, h1, w0, w1, f, m;
        m = this.game.getId();
        f = this.game.getNbGhosts();
        h0 = (byte) h; // lowest weight byte
        h1 = (byte) (h >> 8); // strongest weight byte
        w0 = (byte) w; // lowest weight byte
        w1 = (byte) (w >> 8); // strongest weight byte
        String ipMulticastStr = ipMulticast.getHostAddress();
        String ip = ipMulticastStr + "#".repeat(15 - ipMulticastStr.length());
        this.out.printf("WELCO %c %c%c %c%c %c %s %04d***", m, h0, h1, w0, w1, f, ip, portMulticast);
    }

    public void sendPOSIT(int x, int y) {
        // send [POSIT id x y***]
        this.out.printf("POSIT %s %03d %03d***", this.id, x, y);
    }

    public void sendPLAYR(PrintWriter dest) {
        dest.printf("PLAYR %s***", this.id);
    }

    public boolean unsubscribe() {
        if (this.game != null) {
            this.game.removePlayer(this);
            // remove the game if it has no players left
            if (this.game.getNbPlayers() == 0) {
                ServerImpl.INSTANCE.removeGame(this.game);
            }
            this.game = null;
            return true;
        }
        return false;
    }


}

