package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
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
    public TreeMap<Integer, DistanceDirectionPair>[][] goldBFSTable; // Maps GOLD id with Distance and Direction to it
	public TreeMap<Integer, DistanceDirectionPair>[][] discoveryBFSTable; // Maps Discovery Coords with Distance and Direction to it
    public TreeSet<Integer> goldBFSCalculated;            // All The Gold With BFS Calculated.
    public TreeSet<TiZiiCoords> curGoldLocations;          // Current Locations that contains gold.
    public TreeMap<Integer, TiZiiCoords> goldIdToCoordMap; // Maps Gold id with Coordination of it.
    public TreeMap<TiZiiCoords, Integer> coordToGoldIdMap; // Maps Coordination with Gold id
    public TreeMap<TiZiiCoords, Integer> assignedDiscoveryTargetToPlayer;
	public TreeMap<Integer, TiZiiCoords> assignedPlayerToDiscoveryTarget;
	public TreeMap<TiZiiCoords, Integer> discoveryCoordsToId;
	public TreeMap<Integer, TiZiiCoords> discoveryIdToCoords;

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

	    this.discoveryCoordsToId = new TreeMap<>();
	    this.discoveryIdToCoords = new TreeMap<>();
	    assignedDiscoveryTargetToPlayer = new TreeMap<>();
	    assignedPlayerToDiscoveryTarget = new TreeMap<>();

        this.goldBFSCalculated = new TreeSet<>();
        this.goldBFSTable = new TreeMap[rows][cols];
	    this.discoveryBFSTable = new TreeMap[rows][cols];
        for (int i=0 ; i<rows ; i++){
            for(int j=0 ; j<cols ; j++){
                this.goldBFSTable[i][j] = new TreeMap<>();
	            this.discoveryBFSTable[i][j] = new TreeMap<>();
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

	        // assign closest available miner to gold // TODO: Use Idle Players.
	        Integer minDist = (int)1e8;
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

		        // remove player from discovery.
		        if (assignedPlayerToDiscoveryTarget.containsKey(assignedMiner)){
			        TiZiiCoords coords = assignedPlayerToDiscoveryTarget.get(assignedMiner);
			        assignedDiscoveryTargetToPlayer.remove(coords);
			        assignedPlayerToDiscoveryTarget.remove(assignedMiner);

			        int targetId = discoveryCoordsToId.get(coords);   // Clearing BFS and All Assignments.
			        discoveryCoordsToId.remove(coords);
			        discoveryIdToCoords.remove(targetId);

			        TiZiiUtils.BFS(targetId, coords, discoveryBFSTable, true);
		        }
	        }
        }

        // Remove Digged Golds.
        for (TiZiiCoords coord : alliesInfo.curAroundCells){
            int i = coord.i, j = coord.j;
            if (coordToGoldIdMap.containsKey(coord)
                    && mBoard[i][j] == Consts.GOLD
                    && !curGoldLocations.contains(coord)) {

	            Integer goldId = coordToGoldIdMap.get(coord);
                TiZiiUtils.BFS(goldId, coord, goldBFSTable, true); // Clearing BFS
                goldIdToCoordMap.remove(goldId);
                coordToGoldIdMap.remove(coord); mBoard[i][j] = Consts.EMPTY;

	            Integer playerId = alliesInfo.assignedGoldToPlayer.get(goldId);
	            alliesInfo.assignedPlayerToGold.remove(playerId);
	            alliesInfo.assignedGoldToPlayer.remove(goldId);

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

	    // add discovered areas that doesn't contains gold.
	    for (Player player : players) {
		    for (Cell cell : player.getAroundCells()) {
				if (discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] == Consts.UNSEEN) {
					if (cell.getType().isBlock())
						 discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] = Consts.BLOCK;
					else discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] = Consts.EMPTY;
					TiZiiCoords coords = new TiZiiCoords(cell);
					if (discoveryCoordsToId.containsKey(coords)){
						int targetId = discoveryCoordsToId.get(coords);   // Clearing BFS and All Assignments.
						discoveryCoordsToId.remove(coords);
						discoveryIdToCoords.remove(targetId);
						int playerId = assignedDiscoveryTargetToPlayer.get(coords);
						assignedDiscoveryTargetToPlayer.remove(coords);
						assignedPlayerToDiscoveryTarget.remove(playerId);
						alliesInfo.idlePlayers.add(playerId);
						TiZiiUtils.BFS(targetId, coords, discoveryBFSTable, true);
					}
				}
		    }
	    }

	    for (int i=0 ; i<rows ; i++){
		    for (int j=0 ; j<cols ; j++){
			    try {
				    if (discoveredAreas[i][j] > 0) continue;
				    if (assignedDiscoveryTargetToPlayer.size() >= alliesInfo.idlePlayers.size()) break;
			    } catch (Exception e){
				    for (int d = 0 ; d<100 ; d+=2)
					    d+=2;
				    e.printStackTrace();
			    }

			    boolean isEdgeCell = false;
			    for (int ii=i-1 ; ii<=i+1; ii++){
				    for (int jj = j-1 ; jj<=j+1 ; jj++){
					    if (!TiZiiUtils.inRange(ii,jj)) continue;
						if (discoveredAreas[ii][jj] != Consts.UNSEEN
								&& discoveredAreas[ii][jj] != Consts.BLOCK) isEdgeCell = true;
				    }
			    }
				TiZiiCoords thisCoords = new TiZiiCoords(i, j);

			    if (isEdgeCell){
				    boolean isHigherThanManhattan = true;
				    for (TiZiiCoords coords : discoveryCoordsToId.keySet()){
						if (TiZiiUtils.manhattanDistance(thisCoords, coords) < TiZiiUtils.Consts.MANHATAN_DISTANCE){
							isHigherThanManhattan = false;
						}
				    }

				    if (isHigherThanManhattan){
					    int id; // Assign Id To Coord
					    for (id=0 ; true ; id++){
						    if (!discoveryIdToCoords.containsKey(id)){
							    discoveryIdToCoords.put(id, thisCoords);
							    discoveryCoordsToId.put(thisCoords, id);
							    break;
						    }
					    }
					    //if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)          System.out.println(this);
					    TiZiiUtils.BFS(discoveryCoordsToId.get(thisCoords), thisCoords, discoveryBFSTable, false);
					    //if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)       TiZiiUtils.printGoldBfsDirections(discoveryBFSTable, discoveryIdToCoords, "Discovery BFS Directions");

					    Integer minDist = (int)1e8;
					    Integer assignedPlayer = null;
					    for (Player player : players){
						    if (alliesInfo.idlePlayers.contains(player.getId())){
							    TiZiiCoords minerCoords = new TiZiiCoords(player.getCell());
							    DistanceDirectionPair pair = discoveryBFSTable[minerCoords.i][minerCoords.j].get(id);
							    if (pair != null && pair.distance < minDist
									    && alliesInfo.idlePlayers.contains(player.getId())){
								    minDist = pair.distance; assignedPlayer = player.getId();
							    }
						    }
					    }
					    if (assignedPlayer != null){
						    assignedDiscoveryTargetToPlayer.put(thisCoords, assignedPlayer);
						    assignedPlayerToDiscoveryTarget.put(assignedPlayer, thisCoords);
						    alliesInfo.idlePlayers.remove(assignedPlayer);
					    } else { // cannot be assigned.
						    discoveryIdToCoords.remove(id);
						    discoveryCoordsToId.remove(thisCoords);
					    }
				    }
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

	    public static int DISCOVERY_QTY;
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
