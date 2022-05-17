package Server;

import java.util.*;

// adapted from the code in the link below
// https://jonathanzong.com/blog/2012/11/06/maze-generation-with-prims-algorithm
public class Labyrinth {
    // TODO: make sure rows and cols are < 1000
    private final short height; // number of lines
    private final short width;  // number of columns
    private final char[][] maze;
    private final Cell startNode = setStartNode();
    private byte nbGhosts;

    public Labyrinth(short height, short width) {
        this.height = height;
        this.width = width;
        this.maze = new char[height][];
        init();
        generate();
    }

    public short getHeight() {
        return height;
    }

    public short getWidth() {
        return width;
    }

    public byte getNbGhosts() {
        return nbGhosts;
    }

    private Cell setStartNode() {
        int stX, stY;
        stX = (int) (Math.random() * height);
        if (stX == 0 || stX == height - 1) stY = (int) (Math.random() * width);
        else stY = ((int) (Math.random() * 2)) * (width - 1);
        return new Cell(stX, stY, null);
    }

    private void init() {
        // build maze and initialize with only walls
        StringBuilder s = new StringBuilder(width);
        s.append("1".repeat(width));
        for (int x = 0; x < height; x++) maze[x] = s.toString().toCharArray();
    }

    private void generate() {
        // select random point and open as start node
        setStartNode();

        maze[this.startNode.r][this.startNode.c] = 'S';

        // iterate through direct neighbors of start node
        ArrayList <Cell> frontier = nonWallsNeighbours(this.startNode);

        Cell last = null;
        while (!frontier.isEmpty()) {
            // pick current node at random
            Cell cu = frontier.remove((int)(Math.random() * frontier.size()));
            Cell op = cu.opposite();
            try {
                // if both node and its opposite are walls
                if (this.maze[cu.r][cu.c] == '1') {
                    if (this.maze[op.r][op.c] == '1') {

                        // open path between the nodes
                        this.maze[cu.r][cu.c] = '0';
                        this.maze[op.r][op.c] = '0';

                        // store last node in order to mark it later
                        last = op;

                        // iterate through direct neighbors of node
                        frontier = nonWallsNeighbours(op);
                    }
                }
            } catch (Exception e) { // ignore NullPointer and ArrayIndexOutOfBounds
            }

            // if algorithm has resolved, mark end node
            if (frontier.isEmpty())
                this.maze[last.r][last.c] = 'E';
        }
    }

    private ArrayList <Cell> nonWallsNeighbours(Cell node) {
        // iterate through direct neighbors of node
        ArrayList <Cell> frontier = new ArrayList <Cell> ();
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0 || x != 0 && y != 0)
                    continue;
                try {
                    if (this.maze[node.r + x][node.c + y] == '0') continue;
                } catch (Exception e) { // ignore ArrayIndexOutOfBounds
                    continue;
                }
                // add eligible points to frontier
                frontier.add(new Cell(node.r + x, node.c + y, node));
            }
        return frontier;
    }

    public void print() {
        // print final maze
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++)
                System.out.print(this.maze[i][j]);
            System.out.println();
        }
    }

//    public static void main(String[] args) {
//        // dimensions of generated maze
//        int r = 10, c = 10;
//        Labyrinth l = new Labyrinth(r, c);
//        l.print();
//    }

    static class Cell {
        final Integer r; // row
        final Integer c; // column
        final Cell parent; // parent Cell
        public Cell(int x, int y, Cell p) {
            r = x;
            c = y;
            parent = p;
        }
        // compute opposite node given that it is in the other direction from the parent
        public Cell opposite() {
            if (this.r.compareTo(parent.r) != 0)
                return new Cell(this.r + this.r.compareTo(parent.r), this.c, this);
            if (this.c.compareTo(parent.c) != 0)
                return new Cell(this.r, this.c + this.c.compareTo(parent.c), this);
            return null;
        }
    }
}