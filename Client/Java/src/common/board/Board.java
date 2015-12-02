package common.board;

import java.util.ArrayList;

import common.player.Player;

/**
 * This class represents grid of the game.
 */
public class Board {

    private static Board instance = null;
    
    private Cell[][] cells;
    private int y; //row number
    private int x; // column number
    private ArrayList<Gold> golds = new ArrayList<Gold>();

    /**
     * This method create grid from text file!
     */
    public Board() {
    }

    public Board(int rowNumber, int colNumber) {
        this.x = colNumber;
        this.y = rowNumber;
        cells = new Cell[rowNumber][colNumber];
    }
    
    public static Board getInstance() {
        if (Board.instance == null) {
            Board.instance = new Board();
        }
        return Board.instance;
    }
    
    public static void setInstance(Board board) {
        Board.instance = board;
    }

    /**
     * @param rowNumber
     * @param colNumber
     * @return returns the cell in position rowNumber &
     * colNumber! if position is not valid returns null
     */
    public Cell getCellAt(int rowNumber, int colNumber) {
        if (isValidCell(rowNumber, colNumber)) {
            return cells[rowNumber][colNumber];
        } else {
            return null;
        }
    }
    
    /**
     * @definition this method check the cell is in grid. rowNumber and
     * colNumber
     * @param rowNumber
     * @param colNumber
     * @return
     */
    public boolean isValidCell(int rowNumber, int colNumber) {
        return ((rowNumber >= 0) && (rowNumber < y)
                && (colNumber >= 0) && (colNumber < x) && cells[rowNumber][colNumber] != null);
    }

 
    /**
     * Return the list of golds in the neighbor cells.
     *
     * @param player the player who is searching for gold in neighbor cells.
     * @return returns a list of neighbor golds. this list will be empty if
     * there is no gold.
     */
    public ArrayList<Gold> getNeighborGolds(Player player) {
        ArrayList<Gold> golds = new ArrayList<Gold>();
        Cell cell = player.getCell();
        ArrayList<Cell> view = cell.getAroundCells();
        view.add(player.getCell());
        for (Cell adjCell : view) {
            Gold g = getGold(adjCell);
            if (g != null) {
                golds.add(g);
            }
        }
        return golds;
    }

    /**
     * @definition This method returns the gold of the cell if found. Returns
     * null otherwise.
     * @param cell
     * @return
     */
    public Gold getGold(Cell cell) {
        if (cell == null) {
            return null;
        }
        for (Gold gold : golds) {
            if (gold.getCell().equals(cell)) {
                return gold;
            }
        }
        return null;
    }

    /**
     * Return all known golds.
     *
     * @return
     */
    public Cell[][] getBoard() {
        return cells;
    }

    public int getNumberOfRows() {
        return y;
    }

    public int getNumberOfColumns() {
        return x;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    public ArrayList<Gold> getGolds() {
        return golds;
    }

    public void setGolds(ArrayList<Gold> golds) {
        this.golds = golds;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.y = numberOfRows;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.x = numberOfColumns;
    }

    public void addCellToBoard(Cell cell) {
        cells[cell.getRowNumber()][cell.getColumnNumber()] = cell;
    }

    public void addNewGolds(ArrayList<Gold> newGolds) {
        for (Gold gold : newGolds) {
            if (!golds.contains(gold)) {
                golds.add(gold);
            }
        }
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < getNumberOfRows(); i++) {
            for (int j = 0; j < getNumberOfColumns(); j++) {
                res += cells[i][j] + " ";
            }
            res += "\n";
        }
        return res;
    }

}
