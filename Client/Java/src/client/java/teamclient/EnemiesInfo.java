package client.java.teamclient;

import common.board.Board;
import common.board.Cell;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Spy;

import java.util.ArrayList;

/**
 * Class name:   EnemiesInfo
 * Date:         12/2/2015
 * Description:  Contains Info about enemy players.
 */
public class EnemiesInfo {
    // Class Members
    public int rows, cols;                      // Dimensions of the mBoard
    public int[][] mBoard;                      // Member Board
    public Board gameBoard;
    public StaticsInfo staticsInfo;
    // constructor
    public EnemiesInfo(Board gameBoard, StaticsInfo staticsInfo) {
        this.gameBoard = gameBoard;
        this.rows = gameBoard.getNumberOfRows();
        this.cols = gameBoard.getNumberOfColumns();
        this.mBoard = new int[rows][cols];
        this.staticsInfo = staticsInfo;
    }

    /**
     * updates shadow locations and adds enemy players to mBoard
     * @param hunters list of enemy hunters
     * @param miners  list of enemy miners
     * @param spies   list of enemy spies
     */
    // TODO: Doesn't Work Correctly
    public void updateEnemyBoard(ArrayList<Hunter> hunters,
                                 ArrayList<GoldMiner> miners, ArrayList<Spy> spies){
        // add enemy players
        for (Hunter hunter : hunters)   setCell(hunter.getCell(), Consts.HUNTER);
        for (GoldMiner miner : miners)  setCell(miner.getCell(), Consts.MINER);
        for (Spy spy : spies)           setCell(spy.getCell(), Consts.SPY);

        // update shadows and Unseen Cells
        for (int i=0 ; i<rows; i++){
            for(int j=0 ; j<cols; j++){
                if (mBoard[i][j] >= Consts.HUNTER_SHADOW){       // if there is a shadow
                    mBoard[i][j]++;
                    if (Consts.isHUNTER_SHADOW(mBoard[i][j])) continue;
                    if (Consts.isMINER_SHADOW(mBoard[i][j])) continue;
                    if (Consts.isSPY_SHADOW(mBoard[i][j])) continue;
                    mBoard[i][j] = Consts.EMPTY;
                } else if (staticsInfo.mBoard[i][j] == Consts.EMPTY){  // updates unseen cells
                    mBoard[i][j] = Consts.EMPTY;
                }
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

    /**
     * Checks adjacent cells with radius == Consts.DANGER_RADIUS returns true if there is an enemy Hunter.
     */
    public boolean isEnemyHunterNearby(Cell cell) {
        int row = cell.getRowNumber();
        int col = cell.getColumnNumber();
        for(int i=row-Consts.DANGER_RADIUS; i<=row+Consts.DANGER_RADIUS; i++){
            for(int j=col-Consts.DANGER_RADIUS; j<col+Consts.DANGER_RADIUS; j++){
                if (!TiZiiUtils.inRange(i, j)) continue; // TODO: Add more details
                if (mBoard[i][j] == Consts.HUNTER || Consts.isHUNTER_SHADOW(mBoard[i][j])) return true;
            }
        }
        return false;
    }


    public static class Consts{
        public static final int UNSEEN = 0;
        public static final int EMPTY  = 1;
        public static final int HUNTER = 2;
        public static final int MINER  = 3;
        public static final int SPY    = 4;
        public static final int HUNTER_SHADOW = 20;
        public static final int MINER_SHADOW = 30;
        public static final int SPY_SHADOW = 40;

        public static boolean isHUNTER_SHADOW(int x){ return x>= HUNTER_SHADOW && x< HUNTER_SHADOW + SHADOW_DUR; }
        public static boolean isMINER_SHADOW(int x){ return x>= MINER_SHADOW && x< MINER_SHADOW + SHADOW_DUR; }
        public static boolean isSPY_SHADOW(int x){ return x>= SPY_SHADOW && x< SPY_SHADOW + SHADOW_DUR; }

        public static final int SHADOW_DUR = 3;      // Duration that shadow lives
        public static final int DANGER_RADIUS = 2;   // if a enemy is nearer than this to player, player is in danger
    }

    /**
     * prints mBoard
     * @return string that to be printed
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName()); sb.append("\n");
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