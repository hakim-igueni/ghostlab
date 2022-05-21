package Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Utils {
    private Utils() {
    }

    public static String readRequest(InputStreamReader inSR) {
        StringBuilder sb = new StringBuilder();
        try {
            int nbConsecutiveStars = 0, c = inSR.read();
            if (c == -1) {
                return null;
            }
            char cc;
            do {
                cc = (char) c;
                sb.append(cc);
                if (cc == '*') {
                    nbConsecutiveStars++;
                    if (nbConsecutiveStars == 3) {
                        break;
                    }
                } else {
                    nbConsecutiveStars = 0;
                }
            } while ((c = inSR.read()) != -1);
        } catch (IOException e) {
            return null;
        }
        String req = sb.toString();
        if (req.length() > 3) {
            return req.subSequence(0, req.length() - 3).toString();
        }
        return "";
    }

    public static void sendMessageUDP(String message, InetAddress address, int portUDP) {
        try (DatagramSocket ds = new DatagramSocket()) {
            byte[] buf = message.getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, address, portUDP);
            ds.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isInvalidId(String id) {
        return !id.matches("^[\\da-zA-Z ]{8}$");
    }

    public static boolean isInvalidPort(String port) {
        return !port.matches("\\d{4}");
    }

    public static boolean isInvalidd(String d) {
        return !d.matches("\\d{3}");
    }

    public static boolean isInvalidmess(String mess) {
        if (mess.length() > 200) {
            return false;
        }
        return !mess.contains("+++");
    }
}
