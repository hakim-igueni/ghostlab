package Server;

public class Labyrinth {
    private final short height; //le nombre de lignes
    private final short width; //le nombre de colonnes
    private char[][] tab;   //[hauteur][largeur]

    public Labyrinth(short height, short width) {
        this.height = height;
        this.width = width;
        creerlabyrinth(height, width);
    }

    public short getHeight() {
        return height;
    }

    public short getWidth() {
        return width;
    }

    public void creerlabyrinth(int height, int width) {
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
                System.out.print(tab[i][j]);
            }
            System.out.println();
        }
    }
}
