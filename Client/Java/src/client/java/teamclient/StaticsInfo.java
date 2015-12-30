package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
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
	public TreeSet<Integer> validGolds;                 // All The Gold (ids) That Present On The Map.
    public TreeSet<TiZiiCoords> curGoldLocations;          // Current Locations that contains gold.
	public ArrayList<TiZiiCoords> curEdgeCells;            // Get The Current Map Edge Cells. (Undiscovered cells adjacent to discovered cells)
    public TreeMap<Integer, TiZiiCoords> goldIdToCoordMap; // Maps Gold id with Coordination of it.
    public TreeMap<TiZiiCoords, Integer> coordToGoldIdMap; // Maps Coordination with Gold id

	public TreeSet<TiZiiCoords> validDiscoveryTargets;          // All Current Discovery Targets.
	public TreeMap<TiZiiCoords, Integer> assignedDiscoveryTargetToPlayer;
	public TreeMap<Integer, TiZiiCoords> assignedPlayerToDiscoveryTarget;
	public TreeMap<TiZiiCoords, Integer> discoveryCoordsToId;
	public TreeMap<Integer, TiZiiCoords> discoveryIdToCoords;

    public AlliesInfo alliesInfo;
	public EnemiesInfo enemiesInfo;

    // constructor
    public StaticsInfo(TeamClientAi game) {
       // this.gameBoard = game.getBoard();
	    this.alliesInfo = game.alliesInfo;
	    this.enemiesInfo = game.enemiesInfo;

        this.rows = game.getBoard().getNumberOfRows();
        this.cols = game.getBoard().getNumberOfColumns();
        this.mBoard = new int[rows][cols];
	    this.discoveredAreas = new int[rows][cols];

	    this.validGolds = new TreeSet<>();
	    this.goldIdToCoordMap = new TreeMap<>();
        this.coordToGoldIdMap = new TreeMap<>();

	    this.validDiscoveryTargets = new TreeSet<>();
	    this.discoveryCoordsToId = new TreeMap<>();
	    this.discoveryIdToCoords = new TreeMap<>();
    }

	/**
	 * Update Golds Info.
	 * @param golds list of all cells that contains gold
	 * @param players list of all players in the game.
	 */
	public void updateGoldInfo(ArrayList<Gold> golds, ArrayList<Player> players, Cell[][] cells){
		//Cell[][] cells = gameBoard.getCells();

		// Use Dynamic Target Assigning and online BFS.
		this.alliesInfo.assignedPlayerToGold = new TreeMap<>();
		this.alliesInfo.assignedGoldToPlayer = new TreeMap<>();

		this.goldBFSTable = new TreeMap[rows][cols];
		for (int i=0 ; i<rows ; i++){
			for(int j=0 ; j<cols ; j++){
				this.goldBFSTable[i][j] = new TreeMap<>();
			}
		}


		// Processing New Golds.
		curGoldLocations = new TreeSet<>();          // Current Cycle Visible Golds.
		for (Gold gold : golds){
			TiZiiCoords coord = new TiZiiCoords(gold.getCell());

			curGoldLocations.add(coord);
			discoveredAreas[coord.i][coord.j] = Consts.GOLD;

			addToValidGolds(gold);
			setCell(gold.getCell(), Consts.GOLD);                       // Calculate BFS.
		}

		// Remove Digged Golds.
		for (TiZiiCoords coord : alliesInfo.curAroundCells) {
			int i = coord.i, j = coord.j;
			if (coordToGoldIdMap.containsKey(coord)
					&& mBoard[i][j] == Consts.GOLD
					&& !curGoldLocations.contains(coord)) {

				Integer goldId = coordToGoldIdMap.get(coord);
				if (goldId != null) {
					validGolds.remove(goldId);

					goldIdToCoordMap.remove(goldId);
					if (coordToGoldIdMap.containsKey(coord)) coordToGoldIdMap.remove(coord);

					mBoard[i][j] = Consts.EMPTY;
					discoveredAreas[i][j] = Consts.EMPTY;
				}
			}
		}
		if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed) {
			System.out.println("***************************");
			System.out.println("Valid Coords:");
			for (Integer goldId : validGolds) {
				TiZiiCoords goldCoords = goldIdToCoordMap.get(goldId);
				System.out.println("" + goldCoords);
			}
			System.out.println("***************************");
			System.out.println();
		}
		// assign closest available miner to gold.
		for (Integer goldId : validGolds){
			TiZiiCoords goldCoords = goldIdToCoordMap.get(goldId);
			if (goldCoords == null)
				continue;

			// Check if Enemy Hunter Around the gold.
			if (enemiesInfo.isEnemyAroundCell(goldCoords, 0)) {
				enemiesInfo.addToHuntingTargets(goldCoords);
				continue;
			}

			// Online BFS.
			TiZiiUtils.BFS(goldId, goldCoords, goldBFSTable, false);

			// assigning operation
			Integer minDist = (int)1e8;
			Integer assignedMiner = null;
			for (Player miner : players){
				if (miner instanceof GoldMiner && alliesInfo.idlePlayers.contains(miner.getId())){
					TiZiiCoords minerCoords = new TiZiiCoords(miner.getCell());
					DistanceDirectionPair pair = goldBFSTable[minerCoords.i][minerCoords.j].get(goldId);
					if (pair != null && pair.distance < minDist
							&& !alliesInfo.assignedPlayerToGold.containsKey(miner.getId())){
						minDist = pair.distance; assignedMiner = miner.getId();
					}
				}
			}
			if (assignedMiner != null){
				if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded){
					System.out.println("******************************************************************************");
					System.out.println("Miner " + assignedMiner + " is assigned to Gold" + goldIdToCoordMap.get(goldId));
					System.out.println("******************************************************************************");
				}
				alliesInfo.assignedPlayerToGold.put(assignedMiner, goldId);
				alliesInfo.assignedGoldToPlayer.put(goldId, assignedMiner);

				alliesInfo.idlePlayers.remove(assignedMiner);
			}
		}
	}
    /**
     * update gold locations and adds new in sight locations.
     * @param players list of all players in the game.
     */
    public void updateStaticBoard(ArrayList<Player> players, Cell[][] cells){
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
			    if (cell == null) continue;
			    TiZiiCoords coords = new TiZiiCoords(cell);
				if (discoveredAreas[coords.i][coords.j] == Consts.UNSEEN) {
					if (cell.getType().isBlock())
						discoveredAreas[coords.i][coords.j] = Consts.BLOCK;
					else
						discoveredAreas[coords.i][coords.j] = Consts.EMPTY;
					// Remove From Discovery Targets.

					if (validDiscoveryTargets.contains(coords)) {
						if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed){
							System.out.println("*******************************");
							System.out.println("Target " + coords + " Removed!");
							System.out.println("*******************************");
						}
						validDiscoveryTargets.remove(coords);
					}
				}
		    }
	    }
    }

	/**
	 * Update Discovery Targets.
	 */
	public void updateDiscoveryInfo(ArrayList<Player> players){

		// Dynamic Target Assigning
		this.curEdgeCells = null;
		this.assignedDiscoveryTargetToPlayer = new TreeMap<>();
		this.assignedPlayerToDiscoveryTarget = new TreeMap<>();
		this.discoveryBFSTable = new TreeMap[rows][cols];
		for (int i=0 ; i<rows ; i++){
			for(int j=0 ; j<cols ; j++){
				this.discoveryBFSTable[i][j] = new TreeMap<>();
			}
		}

		ArrayList<TiZiiCoords> mustBeRemoved = new ArrayList<>();
		for (TiZiiCoords target : validDiscoveryTargets){
			if (enemiesInfo.isEnemyMinerAroundCell(target, 1)) mustBeRemoved.add(target);
		}

		for (int manhattanLimit = 1 ;
		     manhattanLimit<=Math.max(rows, cols)
				     && validDiscoveryTargets.size() > Math.max(0,alliesInfo.idlePlayers.size() - 2) ;
		     manhattanLimit++ ) {

			for (TiZiiCoords thisCoords : validDiscoveryTargets){

				boolean isHigherThanManhattan = true;
				for (TiZiiCoords coords : validDiscoveryTargets ){
					if (thisCoords.compareTo(coords) != 0) continue;
					if (TiZiiUtils.manhattanDistance(thisCoords, coords) < manhattanLimit){
						isHigherThanManhattan = false;
					}
				}

				if (isHigherThanManhattan){
					// try some Randomness
					mustBeRemoved.add(thisCoords);
				}
			}
		}

		for (TiZiiCoords temp : mustBeRemoved){
			removeFromValidDiscoveryCoords(temp);
		}

		for (int manhattanLimit = Math.max(rows, cols) ;
		     manhattanLimit>=0 && validDiscoveryTargets.size() < alliesInfo.idlePlayers.size() ;
		     manhattanLimit-- ) {

			ArrayList<TiZiiCoords> edgeCells = getCurShuffledEdgeCells();

			for (TiZiiCoords thisCoords : edgeCells){

				boolean isHigherThanManhattan = true;
				for (TiZiiCoords coords : validDiscoveryTargets){
					if (TiZiiUtils.manhattanDistance(thisCoords, coords) < manhattanLimit){
						isHigherThanManhattan = false;
					}
				}

				if (isHigherThanManhattan){
					// try some Randomness
					if (TiZiiUtils.getRandomNumber(90) < 85) {
						addToValidDiscoveryCoords(thisCoords);
					}
				}
			}
		}
		for (TiZiiCoords thisCoords : validDiscoveryTargets){
			Integer targetId = discoveryCoordsToId.get(thisCoords);
			TiZiiUtils.BFS(targetId, thisCoords, discoveryBFSTable, false);

			Integer minDist = (int)1e8;
			Integer assignedPlayer = null;
			for (Player player : players){
				if (!alliesInfo.idlePlayers.contains(player.getId())) continue;

				TiZiiCoords minerCoords = new TiZiiCoords(player.getCell());
				DistanceDirectionPair pair = discoveryBFSTable[minerCoords.i][minerCoords.j].get(targetId);
				if (pair != null && pair.distance < minDist){
					minDist = pair.distance; assignedPlayer = player.getId();
				}
			}
			if (assignedPlayer != null){
				assignedDiscoveryTargetToPlayer.put(thisCoords, assignedPlayer);
				assignedPlayerToDiscoveryTarget.put(assignedPlayer, thisCoords);

				alliesInfo.idlePlayers.remove(assignedPlayer);
			}
		}
	}

	/**
	 * Add A Coordination to the Golds and assign an ID to it.
	 * @param gold Contains location and id of gold.
	 */
	public void addToValidGolds(Gold gold){
		TiZiiCoords goldCoords = new TiZiiCoords(gold.getCell());

		validGolds.add(gold.getId());
		goldIdToCoordMap.put(gold.getId(), goldCoords);
		coordToGoldIdMap.put(goldCoords, gold.getId());
	}

	/**
	 * Add A Coordination to the ValidDiscoveryCoords and assign an ID to it.
	 * @param coords coordination that must be added.
	 */
	public void addToValidDiscoveryCoords(TiZiiCoords coords){
		validDiscoveryTargets.add(coords);

		// Assign Id To Coord
		for (int id=0 ; true ; id++){
			if (!discoveryIdToCoords.containsKey(id)){
				discoveryIdToCoords.put(id, coords);
				discoveryCoordsToId.put(coords, id);
				break;
			}
		}
	}

	/**
	 * Removes A Coordination to the ValidDiscoveryCoords and assign an ID to it.
	 * @param coords coordination that must be removed.
	 */
	public void removeFromValidDiscoveryCoords(TiZiiCoords coords){
		if (!validDiscoveryTargets.contains(coords)) return;
		validDiscoveryTargets.remove(coords);
		Integer id = discoveryCoordsToId.get(coords);
		if (id == null) return;
		discoveryCoordsToId.remove(coords);
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
		return cell != null && mBoard[cell.getRowNumber()][cell.getColumnNumber()] == Consts.GOLD;
	}

	/**
	 * Get The Current Map Edge Cells. (Undiscovered cells adjacent to discovered cells)
	 * @return array of curEdgeCells.
	 */
	public ArrayList<TiZiiCoords> getCurShuffledEdgeCells() {
		if (curEdgeCells != null){
			Collections.shuffle(curEdgeCells);
			return curEdgeCells;
		}

		this.curEdgeCells = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (discoveredAreas[i][j] != StaticsInfo.Consts.UNSEEN) continue;

				TiZiiCoords thisCoords = new TiZiiCoords(i, j);
				boolean isEdgeCell = false;
				for (Direction dir : Direction.values()){
					if (dir == Direction.NONE) continue;
					int ii = i + dir.getDeltaRow();
					int jj = j + dir.getDeltaCol();
					if (!TiZiiUtils.inRange(ii, jj)) continue;
					if (discoveredAreas[ii][jj] != StaticsInfo.Consts.UNSEEN
							&& discoveredAreas[ii][jj] != StaticsInfo.Consts.BLOCK) {
						isEdgeCell = true;
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
	public boolean isSecondAheadCellUndiscovered(Player player) {
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


// Trash Can

/**
 * clearing a discovery target.
 * @param target that must be cleared.
 */
//	public void clearDiscoveryTarget(TiZiiCoords target) {
//		Integer targetId = discoveryCoordsToId.get(target);   // Clearing BFS and All Assignments.
//
//		if (targetId != null)
//			discoveryIdToCoords.remove(targetId);
//
//		if (discoveryCoordsToId.containsKey(target))
//			discoveryCoordsToId.remove(target);
//
//		Integer playerId = assignedDiscoveryTargetToPlayer.get(target);
//
//		if (assignedDiscoveryTargetToPlayer.containsKey(target))
//			assignedDiscoveryTargetToPlayer.remove(target);
//
//		if (playerId != null)
//			assignedPlayerToDiscoveryTarget.remove(playerId);
//
//		alliesInfo.idlePlayers.add(playerId);
//
//		if (targetId != null )TiZiiUtils.BFS(targetId, target, discoveryBFSTable, true);
//	}