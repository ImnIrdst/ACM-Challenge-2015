package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
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
	public TreeSet<Integer> curValidGolds;                        // All The Gold (ids) That Present On The Map.
    public TreeSet<TiZiiCoords> curGoldLocations;          // Current Locations that contains gold.
	public ArrayList<TiZiiCoords> curEdgeCells;            // Get The Current Map Edge Cells. (Undiscovered cells adjacent to discovered cells)
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

	    this.curValidGolds = new TreeSet<>();
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
		alliesInfo.assignedPlayerToGold = new TreeMap<>();
	    alliesInfo.assignedGoldToPlayer = new TreeMap<>();

        // Processing New Golds.
        curGoldLocations = new TreeSet<>();          // Current Cycle Visible Golds.
        for (Gold gold : golds){
	        TiZiiCoords coord = new TiZiiCoords(gold.getCell());
	        curValidGolds.add(gold.getId());
	        curGoldLocations.add(coord);
	        goldIdToCoordMap.put(gold.getId(), coord);
            coordToGoldIdMap.put(coord, gold.getId());
			discoveredAreas[coord.i][coord.j] = Consts.GOLD;
	        setCell(gold.getCell(), Consts.GOLD);                       // Calculate BFS.
	        if (!goldBFSCalculated.contains(gold.getId()))
		        TiZiiUtils.BFS(gold.getId(), coord, goldBFSTable, false);


        }

        // Remove Digged Golds.
        for (TiZiiCoords coord : alliesInfo.curAroundCells) {
	        int i = coord.i, j = coord.j;
	        if (coordToGoldIdMap.containsKey(coord)
			        && mBoard[i][j] == Consts.GOLD
			        && !curGoldLocations.contains(coord)) {

		        Integer goldId = coordToGoldIdMap.get(coord);
		        if (goldId != null) {
			        curValidGolds.remove(goldId);
			        TiZiiUtils.BFS(goldId, coord, goldBFSTable, true); // Clearing BFS

			        if (goldIdToCoordMap.containsKey(goldId)) goldIdToCoordMap.remove(goldId);
			        if (coordToGoldIdMap.containsKey(coord)) coordToGoldIdMap.remove(coord);

			        mBoard[i][j] = Consts.EMPTY;


			        // TODO: Not Used.
//			        Integer playerId = alliesInfo.assignedGoldToPlayer.get(goldId);
//			        if (playerId != null) {
//				        if (alliesInfo.assignedPlayerToGold.containsKey(playerId))
//					        alliesInfo.assignedPlayerToGold.remove(playerId);
//				        alliesInfo.assignedGoldToPlayer.remove(goldId);
//			        }
//			        discoveredAreas[coord.i][coord.j] = Consts.EMPTY;
		        }
	        }
        }

	    // assign closest available miner to gold.
	    for (Integer goldId : curValidGolds){
		    Integer minDist = (int)1e8;
		    Integer assignedMiner = null;
		    for (Player miner : players){
			    if (miner instanceof GoldMiner){
				    TiZiiCoords minerCoords = new TiZiiCoords(miner.getCell());
				    DistanceDirectionPair pair = goldBFSTable[minerCoords.i][minerCoords.j].get(goldId);
				    if (pair != null && pair.distance < minDist
						    && !alliesInfo.assignedPlayerToGold.containsKey(miner.getId())){
					    minDist = pair.distance; assignedMiner = miner.getId();
				    }
			    }
		    }
		    if (assignedMiner != null){
			    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed){
				    System.out.println("Miner " + assignedMiner + " is assigned to " + goldIdToCoordMap.get(goldId));
			    }
			    alliesInfo.assignedPlayerToGold.put(assignedMiner, goldId);
			    alliesInfo.assignedGoldToPlayer.put(goldId, assignedMiner);

			    // remove player from discovery.
			    if (assignedPlayerToDiscoveryTarget.containsKey(assignedMiner)){
				    TiZiiCoords coords = assignedPlayerToDiscoveryTarget.get(assignedMiner);
				    if (assignedDiscoveryTargetToPlayer.containsKey(coords))
					    assignedDiscoveryTargetToPlayer.remove(coords);
				    if (coords != null) assignedPlayerToDiscoveryTarget.remove(assignedMiner);

				    Integer targetId = discoveryCoordsToId.get(coords);   // Clearing BFS and All Assignments.
				    if (targetId != null) {
					    discoveryCoordsToId.remove(coords);
					    if (discoveryIdToCoords.containsKey(targetId))
						    discoveryIdToCoords.remove(targetId);
					    TiZiiUtils.BFS(targetId, coords, discoveryBFSTable, true);
				    }
			    }
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
					else
						discoveredAreas[cell.getRowNumber()][cell.getColumnNumber()] = Consts.EMPTY;

					TiZiiCoords coords = new TiZiiCoords(cell);
					if (discoveryCoordsToId.containsKey(coords)){
						Integer targetId = discoveryCoordsToId.get(coords);   // Clearing BFS and All Assignments.
						if (targetId != null) discoveryCoordsToId.remove(coords);
						if (discoveryIdToCoords.containsKey(targetId)) discoveryIdToCoords.remove(targetId);

						Integer playerId = assignedDiscoveryTargetToPlayer.get(coords);
						if (assignedDiscoveryTargetToPlayer.containsKey(coords))
							assignedDiscoveryTargetToPlayer.remove(coords);

						if (playerId != null) assignedPlayerToDiscoveryTarget.remove(playerId);
						alliesInfo.idlePlayers.add(playerId);
						if (targetId != null) TiZiiUtils.BFS(targetId, coords, discoveryBFSTable, true);
					}
					// TODO: Weird.
					if (assignedDiscoveryTargetToPlayer.containsKey(coords)){
						Integer playerId = assignedDiscoveryTargetToPlayer.get(coords);
						if (playerId != null) {
							assignedDiscoveryTargetToPlayer.remove(coords);
							if (assignedPlayerToDiscoveryTarget.containsKey(playerId))
								assignedPlayerToDiscoveryTarget.remove(playerId);
							alliesInfo.idlePlayers.add(playerId);
						}
					}
				}
		    }
	    }

	    for (int i=0 ; i<rows ; i++){
		    for (int j=0 ; j<cols ; j++){
			    if (discoveredAreas[i][j] > 0) continue;
			    if (assignedDiscoveryTargetToPlayer.size() >= alliesInfo.idlePlayers.size()) break;

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
						if (TiZiiUtils.manhattanDistance(thisCoords, coords) < TiZiiUtils.Consts.MANHATTAN_DISTANCE){
							isHigherThanManhattan = false;
						}
				    }

				    if (isHigherThanManhattan){
					    if (TiZiiUtils.getRandomNumber(100) > 85)
						    continue; // Try Some Randomness

					    int id; // Assign Id To Coord
					    for (id=0 ; true ; id++){
						    if (!discoveryIdToCoords.containsKey(id)){
							    discoveryIdToCoords.put(id, thisCoords);
							    discoveryCoordsToId.put(thisCoords, id);
							    break;
						    }
					    }
					    TiZiiUtils.BFS(id, thisCoords, discoveryBFSTable, false);

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
						    if (alliesInfo.idlePlayers.contains(assignedPlayer))
							    alliesInfo.idlePlayers.remove(assignedPlayer);
					    } else { // cannot be assigned.
						    TiZiiUtils.BFS(discoveryCoordsToId.get(thisCoords), thisCoords, discoveryBFSTable, true);
						    if (discoveryIdToCoords.containsKey(id)) discoveryIdToCoords.remove(id);
						    if (discoveryCoordsToId.containsKey(thisCoords)) discoveryCoordsToId.remove(thisCoords);
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

	/**
	 * Checks that is a cell contains gold or not.
	 * @param cell cell that we must check this for.
	 * @return true if this cell is contain gold.
	 */
	public boolean isGoldCell(Cell cell) {
		return mBoard[cell.getRowNumber()][cell.getColumnNumber()] == Consts.GOLD;
	}

	/**
	 * clearing a discovery target.
	 * @param target that must be cleared.
	 */
	public void clearDiscoveryTarget(TiZiiCoords target) {
		Integer targetId = discoveryCoordsToId.get(target);   // Clearing BFS and All Assignments.

		if (targetId != null)
			discoveryIdToCoords.remove(targetId);

		if (discoveryCoordsToId.containsKey(target))
			discoveryCoordsToId.remove(target);

		Integer playerId = assignedDiscoveryTargetToPlayer.get(target);

		if (assignedDiscoveryTargetToPlayer.containsKey(target))
			assignedDiscoveryTargetToPlayer.remove(target);

		if (playerId != null)
			assignedPlayerToDiscoveryTarget.remove(playerId);

		alliesInfo.idlePlayers.add(playerId);

		if (targetId != null )TiZiiUtils.BFS(targetId, target, discoveryBFSTable, true);
	}

	/**
	 * Get The Current Map Edge Cells. (Undiscovered cells adjacent to discovered cells)
	 * @return array of curEdgeCells.
	 */
	public ArrayList<TiZiiCoords> getCurEdgeCells() {
		if (curEdgeCells != null){
			Collections.shuffle(curEdgeCells);
			return curEdgeCells;
		}

		this.curEdgeCells = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				TiZiiCoords thisCoords = new TiZiiCoords(i, j);
				boolean isEdgeCell = false;
				for (int ii = i - 1; ii <= i + 1; ii++) {
					for (int jj = j - 1; jj <= j + 1; jj++) {
						if (!TiZiiUtils.inRange(ii, jj)) continue;
						if (discoveredAreas[ii][jj] != StaticsInfo.Consts.UNSEEN
								&& discoveredAreas[ii][jj] != StaticsInfo.Consts.BLOCK) {
							isEdgeCell = true;
						}
					}
				}
				if (isEdgeCell) curEdgeCells.add(thisCoords);
			}
		}
		Collections.shuffle(curEdgeCells);
		return curEdgeCells;
	}

	/**
	 * Checks That if Second Ahead Cell of Player is UnDiscovered or not.
	 * @param player that we must check this for him.
	 * @return true if conditions met.
	 */
	public boolean isSecondAheadCellsUnDiscovered(Player player) {
		Direction direction = player.getMovementDirection();
		TiZiiCoords coords = new TiZiiCoords(player.getCell());
		int ii = coords.i + (2 * direction.getDeltaRow());
		int jj = coords.j + (2 * direction.getDeltaCol());
		return TiZiiUtils.inRange(ii, jj) && discoveredAreas[ii][jj] == Consts.UNSEEN;
	}


	public static class Consts{
        public static final int UNSEEN = 0;
        public static final int EMPTY  = 1;
        public static final int BLOCK  = 2;
        public static final int BLIND  = 3;
        public static final int GOLD   = 9;

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
