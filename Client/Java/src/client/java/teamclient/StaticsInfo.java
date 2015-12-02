package client.java.teamclient;

import common.board.Board;
import common.board.Cell;
import common.board.Gold;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;

import java.util.ArrayList;

import static client.java.teamclient.TiZiiUtils.inRange;

/**
 * Class name:   StaticsInfo
 * Date:         12/2/2015
 * Description:  Contains Info About Static Objects (Gold, Blocks, Empties
 */
public class StaticsInfo {
    // Class Members
    public Board gameBoard;
    public int rows, cols;                      // Dimensions of the mBoard
    public int[][] mBoard;                 // Contains Golds, Blocks, Blanks, Objects That Does'nt Move.

    // constructor
    public StaticsInfo(Board board) {
        this.gameBoard = board;
        this.rows = board.getNumberOfRows();
        this.cols = board.getNumberOfColumns();
        this.mBoard = new int[rows][cols];
    }

    /**
     * update gold locations and adds new in sight locations.
     * @param golds list of all cells that contains gold
     * @param cells all cells in the map (unseen cells are null)
     */
    public void updateStaticBoard(ArrayList<Gold> golds){
        Cell[][] cells = gameBoard.getCells();
        // Clear Gold Locations
        for(int i = 0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++) {
                if (mBoard[i][j] == Consts.Gold) mBoard[i][j] = Consts.UNSEEN;
            }
        }

        // add Gold Locations
        for (Gold gold : golds)
            setCell(gold.getCell(), Consts.Gold); // TODO: Use value of mining time

        // add Block and Empty Locations
        for(int i = 0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++) {
                if ( cells[i][j] == null || mBoard[i][j]!=Consts.UNSEEN) continue;
                if ( cells[i][j].getType().isBlock()) setCell(cells[i][j], Consts.BLOCK);
                if (!cells[i][j].getType().isBlock()) setCell(cells[i][j], Consts.EMPTY);
            }
        }
    }

    /**
     * sets a certain cell to a given value from Consts Class
     * @param cell given cell
     * @param value given value (from Consts Class)
     */
    public void setCell(Cell cell, int value){
        mBoard[cell.getRowNumber()][cell.getColumnNumber()] = value;
    }

    public static class Consts{
        // for the static gameBoard
        public static final int UNSEEN = 0;
        public static final int EMPTY  = 1;
        public static final int BLOCK  = 2;
        public static final int BLIND = 3;
        public static final int Gold   = 9;
    }

    /**
     * prints mBoard
     * @return string that to be printed
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for(int i=0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++){
                sb.append(mBoard[i][j]); sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

// Trash Can
//
//    public void addPlayerSight(Player player){
//        Cell cell = player.getCell();
//        for (Cell curCell : cell.getAroundCells()){
//            int i = curCell.getRowNumber();
//            int j = curCell.getColumnNumber();
//            if (!inRange(i, j) || mBoard[i][j] != Consts.UNSEEN) continue;
//            if (curCell.isEmpty()) setCell(curCell, Consts.SEEN);
//            if (curCell.getType().isBlock()) setCell(curCell, Consts.BLOCK);
//        }
//        for(int i=cell.getRowNumber() - 1; i<=cell.getRowNumber()+1 ; i++){
//            for(int j=cell.getColumnNumber() - 1; j<=cell.getColumnNumber()+1 ; j++){
//                if(inRange(i, j) && mBoard[i][j] == Consts.UNSEEN){
//                    Cell curCell = gameBoard.getCellAt(i, j);
//                    if (curCell.getType().isBlock()) mBoard[i][j] = Consts.SEEN;
//                }
//            }
//        }
//    }