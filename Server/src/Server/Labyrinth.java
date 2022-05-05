package Server;

public class Labyrinth {
    private final short height; //le nombre de lignes
    private final short width; //le nombre de colonnes
    private char[][] tab; //le tableau de caractères
    private byte nbGhosts; //le nombre de fantômes

    public Labyrinth(short height, short width) {
        this.height = 6;
        this.width = 7;
//        creerlabyrinth(height, width);
        tab = new char[height][width];
        tab[0] = "1011111".toCharArray();
        tab[1] = "1010000".toCharArray();
        tab[2] = "1011011".toCharArray();
        tab[3] = "0010011".toCharArray();
        tab[4] = "1000001".toCharArray();
        tab[5] = "1111101".toCharArray();
//        afficheLabyrinthe();
    }

    public short getHeight() {
        return height;
    }

    public short getWidth() {
        return width;
    }

    private void creerlabyrinth(int height, int width) {
        this.tab = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tab[i][j] = ' ';
            }
        }
    }

    public void afficheLabyrinthe() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(tab[i][j] + " ");
            }
            System.out.println();
        }
    }

    public byte getNbGhosts() {
        return nbGhosts;
    }
}

