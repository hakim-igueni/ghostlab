package Server;

import java.net.InetAddress;
import java.util.*;

import static Server.Utils.sendMessageUDP;

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
    private static final Random random = new Random();
    // TODO: make sure rows and cols are < 1000
    private final short height; // number of lines
    private final short width;  // number of columns
    private final Cell[][] grid;
    private final ArrayList<Ghost> ghosts;
    private int nbNonWallCells = 0;
    private byte nbGhosts;
    //    private

    public Labyrinth() {
        this.height = (short) (Math.random() * (Labyrinth.MAX - Labyrinth.MIN) + Labyrinth.MIN);
        this.width = (short) (this.height * 0.75); // height to width ratio is 4:3
        this.grid = new Cell[height][width];
        generateGrid();
        print();
        ghosts = new ArrayList<>();
        createGhosts();
    }

    public void createGhosts() {
        nbGhosts = (byte) (Math.random() * (height * width) / 10);
        int max = (int) (nbNonWallCells * 0.5); // max = 50% of non-wall cells
        do {
            nbGhosts = (byte) (Math.random() * max);
        } while (nbGhosts == 0);
        int row, col;
        for (int i = 0; i < nbGhosts; i++) {
            row = random.nextInt(height);
            col = random.nextInt(width);
            ghosts.add(new Ghost(row, col));
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

        grid[x][y].setWall(); // set cell to path
        nbNonWallCells++;
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
            nbNonWallCells++;
        frontierCell.setWall();

        if (inBetweenCell.isWall)
            nbNonWallCells++;
        inBetweenCell.setWall();

        if (neighbour.isWall)
            nbNonWallCells++;
        neighbour.setWall();
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

    public void placePlayer(Player player) {
        // generate a random number
        int row, col;
        do {
            row = random.nextInt(height);
            col = random.nextInt(width);
        } while (grid[row][col].isWall);
        grid[row][col].containsPlayer = true;
        player.setPosition(row, col);
    }

    public void moveGhosts(InetAddress ipMulticast, int portMulticast) {
        for (Ghost ghost : ghosts) {
            int oldRow = ghost.getRow();
            int oldCol = ghost.getCol();
            int newRow, newCol, distance;
            while (true) {
                if (random.nextBoolean()) { // move horizontally
                    newRow = oldRow;
                    distance = random.nextInt(ghost.getSpeed() + 1);
                    newCol = oldCol + (random.nextBoolean() ? 1 : -1) * distance;
                } else { // move vertically
                    newCol = oldCol;
                    distance = random.nextInt(ghost.getSpeed() + 1);
                    newRow = oldRow + (random.nextBoolean() ? 1 : -1) * distance;
                }
                if (isValidPosition(newRow, newCol) && !grid[newRow][newCol].isWall && !grid[newRow][newCol].containsPlayer) {
                    grid[oldRow][oldCol].containsGhost = false;
                    ghost.setPosition(newRow, newCol);
                    grid[newRow][newCol].containsGhost = true;
                    sendMessageUDP(String.format("GHOST %03d %03d+++", newRow, newCol), ipMulticast, portMulticast);
                    break;
                }
            }
        }
    }

    public int captureGhost(int row, int col) {
        grid[row][col].containsGhost = false;
        int total = 0;
        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == row && ghost.getCol() == col) {
                total += ghost.getScore();
                ghosts.remove(ghost);
            }
        }
        return total;
    }

    static class Cell {
        private final int row, column;
        private boolean isWall;
        private volatile boolean containsGhost = false;
        private volatile boolean containsPlayer = false;

        public Cell(int row, int column, boolean isWall) {
            this.row = row;
            this.column = column;
            this.isWall = isWall;
        }

        private void setWall() {
            this.isWall = false;
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

