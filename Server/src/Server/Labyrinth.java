package Server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Adapted from the code
 * <a href="https://bitbucket.org/c0derepo/prime-algo-maze-generation/src/master/src/common">here</a>
 */
public class Labyrinth {
    private static final int[][] DIRECTIONS = { // distance of 2 to each side
            {0, -2}, // north
            {0, 2}, // south
            {2, 0}, // east
            {-2, 0}, // west
    };
    public static final int MAX = 200;
    public static final int MIN = 5;

    // TODO: make sure rows and cols are < 1000
    private final short height; // number of lines
    private final short width;  // number of columns

    private final Cell[][] cells;
    private final Random random;
    private byte nbGhosts;

    public Labyrinth() {
        this.height = (short) (Math.random() * (Labyrinth.MAX - Labyrinth.MIN) + Labyrinth.MIN);
        this.width = (short) ((this.height * 3) / 4); // width to height ratio is 4:3
        this.cells = new Cell[height][width];
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                Cell cell = new Cell(row, col);
                cells[row][col] = cell;
            }
        }
        random = new Random();
        generate();
        // todo: place ghosts randomly in non-wall cells
    }

    public static void main(String[] args) {
        Labyrinth m = new Labyrinth();
        m.print();
    }

    private void generate() {
        // Start with a grid full of cellModelViews in state wall (not a path).
        for (Cell[] cellsLine : cells)
            for (Cell cell : cellsLine)
                cell.setWall(true);


        // Pick a random cell
        int x = random.nextInt(this.height);
        int y = random.nextInt(width);

        cells[x][y].setWall(false); // set cell to path
        // Compute cell frontier and add it to a frontier collection
        Set<Cell> frontierCells = new HashSet<>(frontierCellsOf(cells[x][y]));

        while (!frontierCells.isEmpty()) {

            // Pick a random cell from the frontier collection
            Cell frontierCell = frontierCells.stream().skip(random.nextInt(frontierCells.size())).findFirst()
                    .orElse(null);

            // Get its neighbors: cells in distance 2 in state path (no wall)
            List<Cell> frontierNeighbors = passageCellsOf(frontierCell);

            if (!frontierNeighbors.isEmpty()) {
                // Pick a random neighbor
                Cell neighbor = frontierNeighbors.get(random.nextInt(frontierNeighbors.size()));
                // Connect the frontier cell with the neighbor
                assert frontierCell != null;
                connect(frontierCell, neighbor);
            }

            // Compute the frontier cells of the chosen frontier cell and add them to the
            // frontier collection
            frontierCells.addAll(frontierCellsOf(frontierCell));
            // Remove frontier cell from the frontier collection
            frontierCells.remove(frontierCell);
        }
    }

    // Frontier cells: wall cells in a distance of 2
    private List<Cell> frontierCellsOf(Cell cell) {

        return cellsAround(cell, true);
    }

    // Frontier cells: passage (no wall) cells in a distance of 2
    private List<Cell> passageCellsOf(Cell cell) {

        return cellsAround(cell, false);
    }

    private List<Cell> cellsAround(Cell cell, boolean isWall) {

        List<Cell> frontier = new ArrayList<>();
        for (int[] direction : DIRECTIONS) {
            int newRow = cell.getRow() + direction[0];
            int newCol = cell.getColumn() + direction[1];
            if (isValidPosition(newRow, newCol) && cells[newRow][newCol].isWall() == isWall) {
                frontier.add(cells[newRow][newCol]);
            }
        }

        return frontier;
    }

    // connects cells which are distance 2 apart
    private void connect(Cell frontierCellModelView, Cell neighbour) {
        int inBetweenRow = (neighbour.getRow() + frontierCellModelView.getRow()) / 2;
        int inBetweenCol = (neighbour.getColumn() + frontierCellModelView.getColumn()) / 2;
        frontierCellModelView.setWall(false);
        cells[inBetweenRow][inBetweenCol].setWall(false);
        neighbour.setWall(false);
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < this.height && col >= 0 && col < width;
    }

    public void print() {
        for (Cell[] cellsLine : cells) {
            for (Cell cell : cellsLine) {
                System.out.print(cell.isWall() ? "X" : " ");
            }
            System.out.println();
        }
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public byte getNbGhosts() {
        return nbGhosts;
    }

    public boolean isWall(int row, int col) {
        return cells[row][col].isWall();
    }

    static class Cell {
        private final int row, column;
        private boolean isWall;

        public Cell(int row, int column) {
            this(row, column, false);
        }

        public Cell(int row, int column, boolean isWall) {
            this.row = row;
            this.column = column;
            this.isWall = isWall;
        }

        /**
         * Get {@link #isWall}
         */
        public boolean isWall() {
            return isWall;
        }

        /**
         * Set {@link #isWall}
         */
        public void setWall(boolean isWall) {
            this.isWall = isWall;
        }

        /**
         * Get {@link #row}
         */
        public int getRow() {
            return row;
        }

        /**
         * Get {@link #column}
         */
        public int getColumn() {
            return column;
        }
    }
}

