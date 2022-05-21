package Server;

import java.net.InetAddress;
import java.util.*;

import static Server.Utils.sendMessageUDP;

/**
 * Adapted from the code
 * <a href="https://bitbucket.org/c0derepo/prime-algo-maze-generation/src/master/src/common">here</a>
 */
public class Labyrinth implements Runnable {
    public static final int MAX = 150; // todo: add max easy, medium, hard
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
    private final Game game;
    private final Cell[][] grid;
    private final ArrayList<Ghost> ghosts;
    private int nbNonWallCells = 0;

    public Labyrinth(Game game) {
        this.game = game;
        this.height = (short) (Math.random() * (Labyrinth.MAX - Labyrinth.MIN) + Labyrinth.MIN);
        this.width = (short) (this.height * 0.75); // height to width ratio is 4:3
        this.grid = new Cell[height][width];
        generateGrid();
        System.out.printf("Labyrinth of game %d: %d x %d\n", Byte.toUnsignedInt(game.getId()), height, width);
        print();
        ghosts = new ArrayList<>();
        createGhosts();
    }

    public void createGhosts() {
        int max = (int) (nbNonWallCells * 0.5); // max = 50% of non-wall cells
        int nbGhosts = ((int) (Math.random() * max) + 1) % 256;
        int row, col;
        for (int i = 0; i < nbGhosts; i++) {
            row = random.nextInt(height);
            col = random.nextInt(width);
            ghosts.add(new Ghost(row, col));
            grid[row][col].incrNbGhosts();
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
        return (byte) ghosts.size();
    }

    public boolean containsGhost(int row, int col) {
        return grid[row][col].nbGhosts > 0;
    }

    public boolean containsPlayer(int row, int col) {
        return grid[row][col].nbPlayers > 0;
    }

    public void incrNbPlayers(int row, int col) {
        grid[row][col].incrNbPlayers();
    }

    public void decrNbPlayers(int row, int col) {
        grid[row][col].decrNbPlayers();
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
                    grid[oldRow][oldCol].decrNbGhosts();
                    ghost.setPosition(newRow, newCol);
                    grid[newRow][newCol].incrNbGhosts();
                    sendMessageUDP(String.format("GHOST %03d %03d+++", newRow, newCol), ipMulticast, portMulticast);
                    break;
                }
            }
            try {
                Thread.sleep(1000); // sleep 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (this.game.isRunning()) {
            moveGhosts(this.game.getIpMulticast(), this.game.getPortMulticast());
        }
    }

    public void captureGhosts(int row, int col, Player player, InetAddress ipMulticast, int portMulticast) {
        int total = 0;
        String mess;
        Iterator<Ghost> it = ghosts.iterator();
        while (it.hasNext()) {
            Ghost ghost = it.next();
            if (ghost.getRow() == row && ghost.getCol() == col) {
                it.remove();
                player.addPoints(ghost.getReward());
                grid[row][col].decrNbGhosts();
                mess = String.format("SCORE %s %04d %03d %03d+++", player.getId(), player.getScore(), row, col);
                sendMessageUDP(mess, ipMulticast, portMulticast);
            }
        }
        if (ghosts.isEmpty()) {
            this.game.finishGame();
            this.game.setMaxScore();
            HashSet<String> winners = this.game.getWinners();
            for (String winner : winners) {
                sendMessageUDP(String.format("ENDGA %s %04d+++", winner, this.game.getMaxScore()), ipMulticast, portMulticast);
            }
        }
//        for (Ghost ghost : ghosts) {
//            if (ghost.getRow() == row && ghost.getCol() == col) {
//                total += ghost.getScore();
//                ghosts.remove(ghost);
//                grid[row][col].decrNbGhosts();
//            }
//        }
//        return total;
    }

    public boolean movePlayerUP(Player player, int d) throws Exception {
        //get position of the player
        int x = player.getRow();
        int y = player.getCol();

        int newX = x - d;
        if (newX < 0) {
            throw new Exception("The distance d=" + d + " can not be traversed");
        }
        int oldScore = player.getScore();
        for (int i = x; i >= newX; i--) {
            if (grid[i][y].isWall) {
                newX = i + 1;
                break;
            }
            if (containsGhost(i, y)) {
                captureGhosts(i, y, player, this.game.getIpMulticast(), this.game.getPortMulticast());
            }
        }
        decrNbPlayers(x, y);
        incrNbPlayers(newX, y);
        player.setRow(newX);
        return oldScore < player.getScore(); // return true if a message has been sent i.e. a ghost has been captured
    }

    public boolean movePlayerDOWN(Player player, int d) throws Exception {
        //get position of the player
        int x = player.getRow();
        int y = player.getCol();

        int newX = x + d;
        if (newX >= getHeight()) {
            throw new Exception("The distance" + d + "can not be traversed");
        }
        int oldScore = player.getScore();
        for (int i = x; i <= newX; i++) {
            if (grid[i][y].isWall) {
                newX = i - 1;
                break;
            }
            if (containsGhost(i, y)) {
                captureGhosts(i, y, player, this.game.getIpMulticast(), this.game.getPortMulticast());
            }
        }
        decrNbPlayers(x, y);
        incrNbPlayers(newX, y);
        player.setRow(newX);
        return oldScore < player.getScore(); // return true if a message has been sent i.e. a ghost has been captured
    }

    public boolean movePlayerLEFT(Player player, int d) throws Exception {
        //get position of the player
        int x = player.getRow();
        int y = player.getCol();

        int newY = y - d;
        if (newY < 0) {
            throw new Exception("The distance" + d + "can not be traversed");
        }
        int oldScore = player.getScore();
        for (int i = y; i >= newY; i--) {
            if (grid[x][i].isWall) {
                newY = i + 1;
                break;
            }
            if (containsGhost(x, i)) {
                captureGhosts(x, i, player, this.game.getIpMulticast(), this.game.getPortMulticast());
            }
        }
        decrNbPlayers(x, y);
        incrNbPlayers(x, newY);
        player.setCol(newY);
        return oldScore < player.getScore(); // return true if a message has been sent i.e. a ghost has been captured
    }

    public boolean movePlayerRIGHT(Player player, int d) throws Exception {
        //get position of the player
        int x = player.getRow();
        int y = player.getCol();

        int newY = y + d;
        if (newY >= getWidth()) {
            throw new Exception("The distance" + d + "can not be traversed");
        }
        int oldScore = player.getScore();
        for (int i = y; i <= newY; i++) {
            if (grid[x][i].isWall) {
                newY = i - 1;
                break;
            }
            if (containsGhost(x, i)) {
                captureGhosts(x, i, player, this.game.getIpMulticast(), this.game.getPortMulticast());
            }
        }
        decrNbPlayers(x, y);
        incrNbPlayers(x, newY);
        player.setCol(newY);
        return oldScore < player.getScore(); // return true if a message has been sent i.e. a ghost has been captured
    }

    static class Cell {
        private final int row, column;
        private boolean isWall;
        private volatile int nbGhosts = 0;
        private volatile int nbPlayers = 0;
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

        public synchronized void incrNbGhosts() {
            nbGhosts++;
        }

        public synchronized void decrNbGhosts() {
            nbGhosts--;
        }

        public synchronized void incrNbPlayers() {
            nbPlayers++;
        }

        public synchronized void decrNbPlayers() {
            nbPlayers--;
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

