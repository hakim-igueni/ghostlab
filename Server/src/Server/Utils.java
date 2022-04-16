package Server;

import java.io.*;

public class Utils {
    private Utils() {
    }

    public static String readRequest(InputStreamReader inSR) {
        StringBuilder sb = new StringBuilder();
        try {
            int c, nbConsecutiveStars = 0;
            char cc;
            while ((c = inSR.read()) != -1) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
