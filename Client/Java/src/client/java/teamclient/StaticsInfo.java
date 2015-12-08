package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import common.board.Board;
import common.board.Cell;
import common.board.Gold;
import common.player.GoldMiner;
import common.player.Player;

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
    public int[][] discoveredAreas;
    public TreeMap<Integer, DistanceDirectionPair>[][] goldBFSTable; // Maps GOLD id with Direction to it
    public TreeSet<Integer> goldBFSCalculated;            // All The Gold With BFS Calculated.
    public TreeSet<TiZiiCoords> curGoldLocations;          // Current Locations that contains gold.
    public TreeMap<Integer, TiZiiCoords> goldIdToCoordMap; // Maps Gold id with Coordination of it.
    public TreeMap<TiZiiCoords, Integer> coordToGoldIdMap; // Maps Coordination with Gold id

    public Board gameBoard;
    public AlliesInfo alliesInfo;

    // constructor
    public StaticsInfo(Board board) {
        this.gameBoard = board;
        this.rows = board.getNumberOfRows();
        this.cols = board.getNumberOfColumns();
        this.mBoard = new int[rows][cols];
	    this.discoveredAreas = new int[rows][cols];
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
    public void updateStaticBoard(ArrayList<Gold> golds, ArrayList<Player> players){
        Cell[][] cells = gameBoard.getCells();

        // Processing New Golds.
        curGoldLocations = new TreeSet<>();          // Current Cycle Visible Golds.
        for (Gold gold : golds){
	        TiZiiCoords coord = new TiZiiCoords(gold.getCell());
            curGoldLocations.add(coord);
            goldIdToCoordMap.put(gold.getId(), coord);
            coordToGoldIdMap.put(coord, gold.getId());
			discoveredAreas[coord.i][coord.j] = Consts.GOLD;
	        setCell(gold.getCell(), Consts.GOLD);                       // Calculate BFS.
	        if (!goldBFSCalculated.contains(gold.getId()))
		        TiZiiUtils.BFS(gold.getId(), coord, goldBFSTable, false);
	        if (alliesInfo.assignedGoldToPlayer.containsKey(gold.getId())) continue;

	        // assign closest available miner to gold
	        Integer minDist = (int) 1e8;
	        Integer assignedMiner = null;
	        for (Player miner : players){
				if (miner instanceof GoldMiner){
					TiZiiCoords minerCoords = new TiZiiCoords(miner.getCell());
					DistanceDirectionPair pair = goldBFSTable[minerCoords.i][minerCoords.j].get(gold.getId());
					if (pair != null && pair.distance < minDist
							&& !alliesInfo.assignedPlayerToGold.containsKey(miner.getId())){
						minDist = pair.distance; assignedMiner = miner.getId();
					}
				}
			}
	        if (assignedMiner != null){
		        alliesInfo.assignedPlayerToGold.put(assignedMiner, gold.getId());
		        alliesInfo.assignedGoldToPlayer.put(gold.getId(), assignedMiner);
	        }
        }

        // Remove Digged Golds.
        for (TiZiiCoords coord : alliesInfo.curAroundCells){
            int i = coord.i, j = coord.j;
            if (coordToGoldIdMap.containsKey(coord)
                    && mBoard[i][j] == Consts.GOLD
                    && !curGoldLocations.contains(coord)) {

                TiZiiUtils.BFS(coordToGoldIdMap.get(coord), coord, goldBFSTable, true); // Clearing BFS
                goldIdToCoordMap.remove(coordToGoldIdMap.get(coord));
                coordToGoldIdMap.remove(coord); mBoard[i][j] = Consts.EMPTY;
	            discoveredAreas[coord.i][coord.j] = Consts.EMPTY;
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

	    // add discovered areas that doe'snt contains gold.
	    for (Player player : players) {
		    for (Cell cell : player.getAroundCells()) {
				if (discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] == Consts.UNSEEN) {
					if (cell.getType().isBlock())
						 discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] = Consts.BLOCK;
					else discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] = Consts.EMPTY;
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
