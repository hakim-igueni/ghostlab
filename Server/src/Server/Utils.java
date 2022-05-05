package Server;

import java.io.IOException;
import java.io.InputStreamReader;

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
}
