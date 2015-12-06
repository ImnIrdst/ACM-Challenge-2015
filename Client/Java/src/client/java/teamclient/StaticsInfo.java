package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiCoord;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;

import java.util.*;

/**
 * Class name:   StaticsInfo
 * Date:         12/2/2015
 * Description:  Contains Info About Static Objects (GOLD, Blocks, Empties
 */
public class StaticsInfo {
    // Class Members
    public int rows, cols;                          // Dimensions of the mBoard

    public int[][] mBoard;                          // Contains Golds, Blocks, Blanks, Objects That Does'nt Move.
    public TreeMap<Integer, DistanceDirectionPair>[][] goldBFSTable; // Maps GOLD id with Direction to it
    public TreeSet<Integer> goldBFSCalculated;            // All The Gold With BFS Calculated.
    public TreeSet<TiZiiCoord> curGoldLocations;          // Current Locations that contains gold.
    public TreeMap<Integer, TiZiiCoord> goldIdToCoordMap; // Maps Gold id with Coordination of it.
    public TreeMap<TiZiiCoord, Integer> coordToGoldIdMap; // Maps Coordination with Gold id

    public Board gameBoard;
    public AlliesInfo alliesInfo;

    // constructor
    public StaticsInfo(Board board) {
        this.gameBoard = board;
        this.rows = board.getNumberOfRows();
        this.cols = board.getNumberOfColumns();
        this.mBoard = new int[rows][cols];
        this.goldIdToCoordMap = new TreeMap<>();
        this.coordToGoldIdMap = new TreeMap<>();
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

        // Processing New Golds.
        curGoldLocations = new TreeSet<>();          // Current Cycle Visible Golds.
        for (Gold gold : golds){
            curGoldLocations.add(new TiZiiCoord(gold.getCell()));
            goldIdToCoordMap.put(gold.getId(), new TiZiiCoord(gold.getCell()));
            coordToGoldIdMap.put(new TiZiiCoord(gold.getCell()), gold.getId());

            setCell(gold.getCell(), Consts.GOLD);                       // Calculate BFS.
            if (!goldBFSCalculated.contains(gold.getId())) goldBFS(gold.getId(), false);
        }

        // Remove Digged Golds.
        for (TiZiiCoord coord : alliesInfo.curAroundCells){
            int i = coord.i, j = coord.j;
            if (coordToGoldIdMap.containsKey(new TiZiiCoord(i,j))
                    && mBoard[i][j] == Consts.GOLD
                    && !curGoldLocations.contains(new TiZiiCoord(i, j))) {
                goldBFS(coordToGoldIdMap.get(new TiZiiCoord(i, j)), true); // Clearing BFS
                goldIdToCoordMap.remove(coordToGoldIdMap.get(new TiZiiCoord(i, j)));
                coordToGoldIdMap.remove(new TiZiiCoord(i,j)); mBoard[i][j] = Consts.EMPTY;
            }
        }

        // add Block and Empty Locations
        for(int i = 0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++) {
                if ( cells[i][j] == null || mBoard[i][j] != Consts.UNSEEN) continue;
                if ( cells[i][j].getType().isBlock()) setCell(cells[i][j], Consts.BLOCK);
                if (!cells[i][j].getType().isBlock()) setCell(cells[i][j], Consts.EMPTY);
            }
        }
    }

    /**
     * Runs a bfs for gold if two phases. 1. Clearing 2. not Clearing
     * @param goldId root of the bfs.
     * @param isClearing defines phase of the bfs.
     */
    void goldBFS(Integer goldId, boolean isClearing){
        goldBFSCalculated.add(goldId);

        TiZiiCoord s = new TiZiiCoord(goldIdToCoordMap.get(goldId));
        Queue<TiZiiCoord> q = new LinkedList<>();

        int[][] vis = new int[rows][cols];
        for (int i=0 ; i<rows ; i++) Arrays.fill(vis[i], -1);

        q.add(s); vis[s.i][s.j] = 0;

        while (!q.isEmpty()){
            TiZiiCoord u = q.poll();
            for (Direction dir : Direction.values()){
                TiZiiCoord v = new TiZiiCoord(u.i + dir.getDeltaRow(), u.j + dir.getDeltaCol());
                if (TiZiiUtils.inRange(v.i, v.j) && vis[v.i][v.j] < 0 && mBoard[v.i][v.j] == Consts.EMPTY){
                    q.add(v); vis[v.i][v.j] = vis[u.i][u.j] + 1;

                    if (!isClearing)
                        goldBFSTable[v.i][v.j].put(goldId,
                                new DistanceDirectionPair(vis[v.i][v.j],
                                        TiZiiUtils.getReverseDirection(dir)));

                    else
                        goldBFSTable[v.i][v.j].remove(goldId);
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
        public static final int UNSEEN = 0;
        public static final int EMPTY  = 1;
        public static final int BLOCK  = 2;
        public static final int BLIND  = 3;
        public static final int GOLD = 9;
    }

    /**
     * prints mBoard
     * @return string that to be printed
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName() + "\n");
        for(int i=0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++){
                sb.append(mBoard[i][j]); sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
