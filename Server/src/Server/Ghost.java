package Server;

public class Ghost {
    private final int speed = (int) (Math.random() * 10);
    private final int score = speed * 2;
    private int row;
    private int col;

    public Ghost(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }


    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getScore() {
        return score;
    }

    public int getSpeed() {
        return speed;
    }

    public void setPosition(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }
}
