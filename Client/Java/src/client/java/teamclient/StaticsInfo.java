package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiPoint;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;

import java.util.*;

/**
 * Class name:   StaticsInfo
 * Date:         12/2/2015
 * Description:  Contains Info About Static Objects (Gold, Blocks, Empties
 */
public class StaticsInfo {
    // Class Members
    public Board gameBoard;
    public int rows, cols;                          // Dimensions of the mBoard
    public int[][] mBoard;                          // Contains Golds, Blocks, Blanks, Objects That Does'nt Move.
    public TreeMap<Integer, DistanceDirectionPair>[][] goldBFSTable; // Maps Gold id with Direction to it
    public TreeSet<Integer> goldBFSCalculated;
    public TreeSet<Integer> prevGolds;              // for finding dead gold
    public TreeSet<Integer> deadGolds;
    public TreeMap<Integer, Cell> curGoldLocations;
    // constructor
    public StaticsInfo(Board board) {
        this.gameBoard = board;
        this.rows = board.getNumberOfRows();
        this.cols = board.getNumberOfColumns();
        this.mBoard = new int[rows][cols];
        this.prevGolds = new TreeSet<>();
        this.deadGolds = new TreeSet<>();
        this.curGoldLocations = new TreeMap<>();
        this.goldBFSCalculated = new TreeSet<>();
        this.goldBFSTable = new TreeMap[rows][cols];
        for (int i=0 ; i<rows ; i++){
            for(int j=0 ; j<cols ; j++){
                this.goldBFSTable[i][j] = new TreeMap<>();
            }
        }
    }

    /**
     * update gold locations and adds new in sight locations.
     * @param golds list of all cells that contains gold
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
        TreeSet<Integer> curGolds = new TreeSet<>();
        for (Gold gold : golds){
            curGolds.add(gold.getId());
            curGoldLocations.put(gold.getId(), gold.getCell());
        }
        if (prevGolds != null) {
            for (Integer goldId : prevGolds) {
                if (!curGolds.contains(goldId)) {
                    goldBFS(goldId, true);       // TODO: Debug this for null pointer exception
                    deadGolds.add(goldId);
                }
            }
        }
        for (Gold gold : golds) {
            setCell(gold.getCell(), Consts.Gold);
            curGoldLocations.put(gold.getId(), gold.getCell());
            if (!goldBFSCalculated.contains(gold.getId())) goldBFS(gold.getId(), false);
        }
        prevGolds = curGolds;

        // add Block and Empty Locations
        for(int i = 0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++) {
                if ( cells[i][j] == null || mBoard[i][j]!=Consts.UNSEEN) continue;
                if ( cells[i][j].getType().isBlock()) setCell(cells[i][j], Consts.BLOCK);
                if (!cells[i][j].getType().isBlock()) setCell(cells[i][j], Consts.EMPTY);
            }
        }
    }

    void goldBFS(Integer goldId, boolean isClear){
        goldBFSCalculated.add(goldId);
        TiZiiPoint s = new TiZiiPoint(curGoldLocations.get(goldId));
        Queue<TiZiiPoint> q = new LinkedList<>();
        int[][] vis = new int[rows][cols];
        for (int i=0 ; i<rows ; i++) Arrays.fill(vis[i], -1);
        q.add(s); vis[s.i][s.j] = 0;

        while (!q.isEmpty()){
            TiZiiPoint u = q.poll();
            for (Direction dir : Direction.values()){
                TiZiiPoint v = new TiZiiPoint(u.i + dir.getDeltaRow(), u.j + dir.getDeltaCol());
                if (TiZiiUtils.inRange(v.i, v.j) && vis[v.i][v.j] < 0){
                    q.add(v); vis[v.i][v.j] = vis[u.i][u.j] + 1;
                    if (!isClear) goldBFSTable[v.i][v.j].put(goldId
                                , new DistanceDirectionPair(vis[v.i][v.j], TiZiiUtils.getReverseDirection(dir)));
                    else          goldBFSTable[v.i][v.j].remove(goldId);
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

    public static class Consts{
        // for the static gameBoard
        public static final int UNSEEN = 0;
        public static final int EMPTY  = 1;
        public static final int BLOCK  = 2;
        public static final int BLIND  = 3;
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