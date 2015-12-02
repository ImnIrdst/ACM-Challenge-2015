package common.board;

import java.util.ArrayList;

import common.player.Bullet;
import common.player.Player;
import common.team.Team;

/**
 * This class represents cell of the board.
 */
public class Cell {

    private CellType type;
    private int y;
    private int x;
    private transient Player playerInside;// show the player is in this cell!
    private transient Bullet bulletIsIn;// show the bullet is in this cell, if available!

    public Cell(int rowNumber, int columntNumber, CellType type) {
        this.y = rowNumber;
        this.x = columntNumber;
        this.type = type;
        // this.visualType = visualType;
    }

    public int getRowNumber() {
        return y;
    }

    public int getColumnNumber() {
        return x;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    /**
     * Return the cell is empty or not with current information.
     *
     * @return returns true if is empty, false otherwise
     */
    public boolean isEmpty() {
        if (getPlayerInside() != null) {
            return false;
        }
        return !getType().isBlock();
    }


        /**
     * Return adjacent cell of current cell in a specific direction
     *
     * @param dir direction of neighbor relate to current cell
     * @return
     */
    public Cell getAdjacentCell(Direction dir) {
        int rowNumber = getRowNumber();
        int colNumber = getColumnNumber();
        int deltaRow = dir.getDeltaRow();
        int deltaCol = dir.getDeltaCol();
        Board board = Board.getInstance();
        
        return board.getCellAt(rowNumber + deltaRow, colNumber + deltaCol);
    }


    /**
     * Return all adjacent cells of current cell
     *
     * @return
     */
    public ArrayList<Cell> getAroundCells() {
        ArrayList<Cell> view = new ArrayList<Cell>();
        int rowNumber = getRowNumber();
        int colNumber = getColumnNumber();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0)) {
                    Cell cell = Board.getInstance().getCellAt(rowNumber + i, colNumber + j);
                    if (cell != null)
                        view.add(cell);
                }
            }
        }
        return view;
    }

    /**
     * Return the cells in view of the cell, in an array!
     *
     * @return
     */
    public ArrayList<Cell> getAheadCells(Direction dir) {
        ArrayList<Cell> view = new ArrayList<Cell>();
        Board board = Board.getInstance();
        Cell cell = this.getAdjacentCell(dir);
        while (cell != null) {
            view.add(cell);
            cell = cell.getAdjacentCell(dir);
            if (cell!=null && cell.getType().isBlock()) {
                break;
            }
        }
        return view;
    }
    
    
    /**
     * If the cell is not empty and an player is in the cell, returns that
     * player otherwise return null.
     *
     * @return
     */
    public Player getPlayerInside() {
        return playerInside;
    }

    /**
     * If a player moves in a cell, this method is called. Attention: A team
     * client should not call this method and it is called by update method of
     * client automatically. calling this method could corrupt your game
     * information and it will not affect server!
     *
     */
    public void setPlayerInside(Player playerInside) {
        this.playerInside = playerInside;
    }

    public Bullet getBulletIsIn() {
        return bulletIsIn;
    }

    public void setBulletIsIn(Bullet bulletIsIn) {
        this.bulletIsIn = bulletIsIn;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(Cell.class)) {
            Cell cell = (Cell) obj;
            if (cell.getRowNumber() == y && cell.getColumnNumber() == x) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Cell{" + "y=" + y + ", x=" + x + '}';
    }

}
