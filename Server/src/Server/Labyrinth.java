package Server;

import java.util.*;

/**
 * Adapted from the code
 * <a href="https://bitbucket.org/c0derepo/prime-algo-maze-generation/src/master/src/common">here</a>
 */
public class Labyrinth {
    public static final int MAX = 999; // todo: add max easy, medium, hard
    public static final int MIN = 12;
    private static final int[][] DIRECTIONS = { // distance of 2 to each side
            {0, -2}, // north
            {0, 2}, // south
            {2, 0}, // east
            {-2, 0}, // west
    };
    // TODO: make sure rows and cols are < 1000
    private final short height; // number of lines
    private final short width;  // number of columns
    private final Random random = new Random();
    private final Cell[][] grid;
    private final Ghost[] ghosts;
    private final HashSet<Cell> nonWallCells = new HashSet<>();
    private byte nbGhosts;
    //    private

    public Labyrinth() {
        this.height = (short) (Math.random() * (Labyrinth.MAX - Labyrinth.MIN) + Labyrinth.MIN);
        this.width = (short) ((this.height * 3) / 4); // width to height ratio is 4:3
        this.grid = new Cell[height][width];
        generateGrid();
        print();
        ghosts = new Ghost[nbGhosts];
        createGhosts();
    }

    public void createGhosts() {
        nbGhosts = (byte) (Math.random() * (height * width) / 10);
        int nbNonWallCells = nonWallCells.size();
        int max = (int) (nbNonWallCells * 0.5); // max = 50% of non-wall cells
        do {
            nbGhosts = (byte) (Math.random() * max);
        } while (nbGhosts == 0);
        int row, col;
        for (int i = 0; i < nbGhosts; i++) {
            row = random.nextInt(height);
            col = random.nextInt(width);
            ghosts[i] = new Ghost(row, col);
            grid[row][col].containsGhost = true;
        }
    }

    private void generateGrid() {
        // Start with a grid full of cells in state wall (not a path).
        for (int row = 0; row < this.height; row++)
            for (int col = 0; col < this.width; col++)
                grid[row][col] = new Cell(row, col, true); // a cell is by default a wall

        // Pick a random cell
        int x = random.nextInt(this.height);
        int y = random.nextInt(this.width);

        grid[x][y].setWall(false); // set cell to path
        nonWallCells.add(grid[x][y]);
        // Compute cell frontier and add it to a frontier collection
        Set<Cell> frontierCells = new HashSet<>(frontierCellsOf(grid[x][y]));

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

            // Compute the frontier cells of the chosen frontier cell and add them to the frontier collection
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
            if (isValidPosition(newRow, newCol) && grid[newRow][newCol].isWall == isWall) {
                frontier.add(grid[newRow][newCol]);
            }
        }
        return frontier;
    }

    // connects cells which are distance 2 apart
    private void connect(Cell frontierCell, Cell neighbour) {
        int inBetweenRow = (neighbour.getRow() + frontierCell.getRow()) / 2;
        int inBetweenCol = (neighbour.getColumn() + frontierCell.getColumn()) / 2;
        Cell inBetweenCell = grid[inBetweenRow][inBetweenCol];
        if (frontierCell.isWall)
            nonWallCells.add(frontierCell);
        frontierCell.setWall(false);

        if (inBetweenCell.isWall)
            nonWallCells.add(inBetweenCell);
        inBetweenCell.setWall(false);

        if (neighbour.isWall)
            nonWallCells.add(neighbour);
        neighbour.setWall(false);
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < this.height && col >= 0 && col < width;
    }

    public void print() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(grid[i][j].isWall ? "#" : " ");
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

    public boolean containsGhost(int row, int col) {
        return grid[row][col].containsGhost;
    }

    public boolean containsPlayer(int row, int col) {
        return grid[row][col].containsPlayer;
    }

    public boolean isWall(int row, int col) {
        return grid[row][col].isWall;
    }

    static class Cell {
        private final int row, column;
        private boolean isWall;
        private boolean containsGhost = false;
        private boolean containsPlayer = false;

        public Cell(int row, int column, boolean isWall) {
            this.row = row;
            this.column = column;
            this.isWall = isWall;
        }

        public void setWall(boolean isWall) {
            this.isWall = isWall;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return row == cell.row && column == cell.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }
}

