package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiBullet;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.player.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class name:   EnemiesInfo
 * Date:         12/2/2015
 * Description:  Contains Info about enemy players.
 */
public class EnemiesInfo {
    // Class Members
    public int rows, cols;                                  // Dimensions of the mBoard
    public int[][] mBoard;                                  // Member Board Contains HUNTER, MINER, SPY or SHADOW.
    public TreeMap<TiZiiBullet, Integer>[][] bulletHitTime; // Table of Bullets that hits a target in a Certain Time.

    public StaticsInfo staticsInfo;
	public AlliesInfo alliesInfo;

	public TreeSet<TiZiiCoords> huntingTargets;                  // Targets That Must Be Hunted.
	public TreeMap<Integer, TiZiiCoords> targetIdToCoord;        // Maps Target ids to Coords
	public TreeMap<TiZiiCoords, Integer> huntingTargetIds;       // Hunting Target ids
	public TreeMap<TiZiiCoords, Integer> targetAssignedToHunter; // Map Targets to Hunters.
	public TreeMap<Integer, TiZiiCoords> hunterAssignedToTarget; // Map Hunters to Targets.
	public TreeMap<Integer, DistanceDirectionPair>[][] huntingBFSTable;    // Table of Bullets that hits a target in a Certain Time.

	// constructor
    public EnemiesInfo(TeamClientAi game) {
	    this.staticsInfo = game.staticsInfo;
		this.alliesInfo = game.alliesInfo;

	    this.rows = game.getBoard().getNumberOfRows();
        this.cols = game.getBoard().getNumberOfColumns();
        this.mBoard = new int[rows][cols];
	    this.bulletHitTime = new TreeMap[rows][cols];
		for (int i=0 ; i<rows ; i++){
			for (int j=0 ; j<cols ; j++){
				this.bulletHitTime[i][j] = new TreeMap<>();
			}
		}
    }

    /**
     * updates shadow locations and adds enemy players to mBoard
     * @param hunters list of enemy hunters
     * @param miners  list of enemy miners
     * @param spies   list of enemy spies
     */
    public void updateEnemyBoard(ArrayList<Hunter> hunters, ArrayList<GoldMiner> miners, ArrayList<Spy> spies) {
	    this.huntingTargets = new TreeSet<>();
	    this.huntingTargetIds = new TreeMap<>();
	    this.targetIdToCoord = new TreeMap<>();

        // add enemy players
        for (Hunter hunter : hunters)   setCell(hunter.getCell(), Consts.HUNTER-1);
        for (GoldMiner miner : miners)  setCell(miner.getCell(), Consts.MINER-1);
        for (Spy spy : spies)           setCell(spy.getCell(), Consts.SPY-1);

        // update shadows and Unseen Cells
        for (int i=0 ; i<rows; i++){
            for(int j=0 ; j<cols; j++){
                if (mBoard[i][j] >= Consts.PLAYER) {       // if there is a shadow
                    mBoard[i][j]++;
                    if (Consts.isHUNTER_OR_SHADOW(mBoard[i][j])) continue;
                    if (Consts.isMINER_OR_SHADOW(mBoard[i][j])) continue;
                    if (Consts.isSPY_OR_SHADOW(mBoard[i][j])) continue;
                    mBoard[i][j] = Consts.EMPTY;
                } else if (mBoard[i][j] == Consts.UNSEEN && staticsInfo.mBoard[i][j] == Consts.EMPTY){  // updates unseen cells
                    mBoard[i][j] = Consts.EMPTY;
                } else if (mBoard[i][j] == Consts.UNSEEN && staticsInfo.mBoard[i][j] == Consts.BLOCK){  // updates unseen cells
                    mBoard[i][j] = Consts.BLOCK;
                }
            }
        }
    }

	/**
	 * updating bullet hit times.
	 * @param bullets contains all the bullets that's visible in the map.
	 */
	void updateBulletHitTimes(ArrayList<Bullet> bullets) {
		// Update Bullet Hit Time.
		for (int i=0 ; i<rows ; i++){
			for(int j=0 ; j<cols ; j++){
				ArrayList<TiZiiBullet> mustBeRemoved = new ArrayList<>();
				for (TiZiiBullet tBullet : bulletHitTime[i][j].keySet()){
					Integer time = bulletHitTime[i][j].get(tBullet);
					time--;  bulletHitTime[i][j].put(tBullet, time);
					if (time < 0) mustBeRemoved.add(tBullet);
				}
				for (TiZiiBullet tBullet : mustBeRemoved){
					bulletHitTime[i][j].remove(tBullet);
				}
			}
		}
		// Calculate Actual Bullets Hit Time.
		for (Bullet bullet : bullets){
			TiZiiBullet tBullet = new TiZiiBullet(bullet);
			TiZiiCoords coords = new TiZiiCoords(tBullet.cell);

			int time = 0;
			while (TiZiiUtils.inRange(coords.i, coords.j)){
				bulletHitTime[coords.i][coords.j].put(tBullet, time);
				coords = coords.adjacent(tBullet.direction);
				if (TiZiiUtils.inRange(coords.i, coords.j))
					bulletHitTime[coords.i][coords.j].put(tBullet, time);
				coords = coords.adjacent(tBullet.direction); time++;
			}

			TiZiiBullet shadowBullet = new TiZiiBullet(bullet);
			coords = new TiZiiCoords(tBullet.cell);

			time = 1; shadowBullet.id *= 100;
			while (TiZiiUtils.inRange(coords.i, coords.j)){
				bulletHitTime[coords.i][coords.j].put(shadowBullet, time);
				coords = coords.adjacent(shadowBullet.direction);
				if (TiZiiUtils.inRange(coords.i, coords.j))
					bulletHitTime[coords.i][coords.j].put(shadowBullet, time);
				coords = coords.adjacent(shadowBullet.direction); time++;
			}
		}
	}


	/**
	 * Adding some coords to blocked coords.
	 * @param enemyHunters . we must add enemy hunters view to the blocked coords.s
	 */
	void updateBlockedCells(ArrayList<Hunter> enemyHunters){
		// add the targets with hitTime == 1 to Blocked Cells.
		TreeSet<TiZiiBullet> tBullets= new TreeSet<>();
		for (int i=0 ; i<rows ; i++) {
			for (int j = 0; j < cols; j++) {
				for (TiZiiBullet bullet : bulletHitTime[i][j].keySet()) {
					tBullets.add(bullet);
				}
			}
		}
		for (TiZiiBullet bullet: tBullets) {
			for (int i=0 ; i<rows ; i++){
				for (int j=0 ; j<cols ; j++){
					Integer hitTime = bulletHitTime[i][j].get(bullet);
					if ( hitTime != null && hitTime <= 2){
						alliesInfo.blockedCoords.add(new TiZiiCoords(i, j));
					}
				}
			}
		}

		// add view of hunters to the blocked Coords.
		for (Hunter hunter : enemyHunters){
			if (!hunter.canAttack()) continue;
			TiZiiCoords coords = new TiZiiCoords(hunter.getCell());
			for (;!TiZiiUtils.inRange(coords.i, coords.j) ; coords = coords.adjacent(hunter.getMovementDirection())){
				alliesInfo.blockedCoords.add(coords);
			}
		}
	}

	public void updateHuntingTargets(ArrayList<Hunter> hunters){


		this.hunterAssignedToTarget = new TreeMap<>();
		this.targetAssignedToHunter = new TreeMap<>();
		this.huntingBFSTable = new TreeMap[rows][cols];
		for (int i=0 ; i<rows ; i++){
			for (int j=0 ; j<cols ; j++){
				this.huntingBFSTable[i][j] = new TreeMap<>();
			}
		}
		if (huntingTargets.isEmpty()) return;

		// assign hunting targets to hunters.
		for (TiZiiCoords targetCoords : huntingTargets){
			// Online BFS.
			Integer targetId = huntingTargetIds.get(targetCoords);

			if (targetId == null) continue;

			TiZiiUtils.BFS(targetId, targetCoords, huntingBFSTable, false);

			// assigning operation
			Integer minDist = (int)1e8;
			Integer assignedHunter = null;
			for (Hunter hunter : hunters){
				if (!alliesInfo.idlePlayers.contains(hunter.getId())) continue;

				TiZiiCoords minerCoords = new TiZiiCoords(hunter.getCell());
				DistanceDirectionPair pair = huntingBFSTable[minerCoords.i][minerCoords.j].get(targetId);
				if (pair != null && pair.distance < minDist
						&& !hunterAssignedToTarget.containsKey(hunter.getId())){
					minDist = pair.distance; assignedHunter = hunter.getId();
				}
			}
			if (assignedHunter != null){
				if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed){
					System.out.println("Hunter " + assignedHunter + " is assigned to " + targetCoords);
				}
				hunterAssignedToTarget.put(assignedHunter, targetCoords);
				targetAssignedToHunter.put(targetCoords, assignedHunter);

				alliesInfo.idlePlayers.remove(assignedHunter);
			}
		}
	}

	public void addToHuntingTargets(TiZiiCoords coords){
		huntingTargets.add(coords);
		for(int id = 0; true; id++){
			if (!targetIdToCoord.containsKey(id)){
				targetIdToCoord.put(id, coords);
				huntingTargetIds.put(coords, id);
				return;
			}
		}
	}

	public void removeFromHuntingTargets(TiZiiCoords coords){
		huntingTargets.remove(coords);
		Integer id = huntingTargetIds.get(coords);
		if (id == null) return;
		targetIdToCoord.remove(id);
		huntingTargetIds.remove(coords);
	}

	/**
	 * @param player : player must be checked
	 * @return true if player is in Range of a bullet.
	 */
	public boolean isInRangeOfBullets(Player player){
		TiZiiCoords tCoords = new TiZiiCoords(player.getCell());
		TreeMap<TiZiiBullet, Integer> bulletHitTimeIJ = bulletHitTime[tCoords.i][tCoords.j];

		for (TiZiiBullet tBullet : bulletHitTimeIJ.keySet()){
			Integer time = bulletHitTimeIJ.get(tBullet);
			if (time <= 2){  // && TiZiiUtils.cellToCellDirection(tBullet.cell, player.getCell()) == tBullet.direction
				return true;
			}
		}
		return false;
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
                if (Consts.isHUNTER_OR_SHADOW(mBoard[i][j])) return true;
            }
            if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded) {
                for (int j = col - Consts.DANGER_RADIUS; j < col + Consts.DANGER_RADIUS; j++) {
	                if (!TiZiiUtils.inRange(i, j)) continue;
                    System.out.print((mBoard[i][j] < 10 ? "0" : "") + mBoard[i][j] + " ");
                }
                System.out.println();
            }
        }
        return false;
    }

	/**
	 * is Enemy Ahead of Hunter. (Used for Shooting).
	 * @param hunter that we need to calculate this for him.
	 * @return true if Enemy Ahead.
	 */
    public boolean isEnemyAhead(Hunter hunter) { // TODO: Improve This. (By Considering Direction of Enemies.)
        return countInWing(hunter.getCell(), hunter.getMovementDirection(), 1) != 0;
    }

	/**
	 * Soroush wings Idea.
	 * @param cell Source Cell.
	 * @param direction Direction of Wing.
	 * @param WING_LENGTH Length We Need to covered by wing.
	 * @return number of occurrence of subject.
	 */
	public int countInWing(Cell cell, Direction direction, int WING_LENGTH){
		int ii = cell.getRowNumber();
		int jj = cell.getColumnNumber();

		int di = direction.getDeltaRow();
		int dj = direction.getDeltaCol();

		int lim = 0;
		for (int i = ii, j = jj ; true ; i += di, j += dj){
			if (!TiZiiUtils.inRange(i, j)) break;
			if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
			lim++;
		}


		int cnt = 0;
		if (di == 0){
			for (int i=ii-1 ; i>=ii-WING_LENGTH ; i--){
				if (!TiZiiUtils.inRange(i,jj)) break;
				for (int j=jj+dj, step = 0 ; step < lim ; j+=dj , step++){
					if (!TiZiiUtils.inRange(i, j)) break;
					if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
					if (mBoard[i][j] >= Consts.PLAYER) cnt++;
				}
			}
			for (int i=ii ; i<=ii+WING_LENGTH ; i++){
				if (!TiZiiUtils.inRange(i,jj)) break;
				for (int j=jj+dj, step = 0 ; step < lim ; j+=dj , step++){
					if (!TiZiiUtils.inRange(i, j)) break;
					if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
					if (mBoard[i][j] >= Consts.PLAYER) cnt++;
				}
			}
		}
		if (dj == 0){
			for (int j=jj-1 ; j>=jj-WING_LENGTH ; j--){
				if (!TiZiiUtils.inRange(ii, j)) break;
				for (int i=ii+di, step = 0 ; TiZiiUtils.inRange(i,jj) && step < lim; i+=di, step++){
					if (!TiZiiUtils.inRange(i, j)) break;
					if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
					if (mBoard[i][j] >= Consts.PLAYER) cnt++;
				}
			}
			for (int j=jj ; j<=jj+WING_LENGTH ; j++){
				if (!TiZiiUtils.inRange(ii, j)) break;
				for (int i=ii+di, step = 0 ; TiZiiUtils.inRange(i,jj) && step < lim; i+=di, step++){
					if (!TiZiiUtils.inRange(i, j)) break;
					if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
					if (mBoard[i][j] >= Consts.PLAYER) cnt++;
				}
			}
		}
		return cnt;
	}

	public boolean isEnemyAroundCell(TiZiiCoords coords, int radius) {
		for (int i=coords.i - radius ; i <= coords.i + radius ; i++) {
			for (int j = coords.j - radius; j <= coords.j + radius; j++) {
				if (TiZiiUtils.inRange(i,j) && mBoard[i][j] >= Consts.PLAYER) return true;
			}
		}
		return false;
	}

	public boolean isEnemyMinerAroundCell(TiZiiCoords coords, int radius) {
		for (int i=coords.i - radius ; i <= coords.i + radius ; i++) {
			for (int j = coords.j - radius; j <= coords.j + radius; j++) {
				if (TiZiiUtils.inRange(i,j) && Consts.isMINER_OR_SHADOW(mBoard[i][j])) return true;
			}
		}
		return false;
	}


	/**
	 * Constants for Enemies info Class.
	 */
    public static class Consts{
        public static final int UNSEEN = 0;
        public static final int EMPTY  = 1;
        public static final int BLOCK  = 2;
        public static final int PLAYER = 20;
        public static final int HUNTER = 30;
        public static final int MINER  = 40;
        public static final int SPY    = 50;

        public static boolean isHUNTER_OR_SHADOW(int x){ return x >= HUNTER && x< HUNTER + SHADOW_DUR; }
        public static boolean isMINER_OR_SHADOW(int x){ return x >= MINER && x < MINER + SHADOW_DUR; }
        public static boolean isSPY_OR_SHADOW(int x){ return x >= SPY && x < SPY + SHADOW_DUR; }
        public static boolean isHUNTER_SHADOW(int x){ return x > HUNTER && x < HUNTER + SHADOW_DUR; }
        public static boolean isMINER_SHADOW(int x){ return x > MINER && x < MINER + SHADOW_DUR; }
        public static boolean isSPY_SHADOW(int x){ return x > SPY && x < SPY + SHADOW_DUR; }

        public static final int SHADOW_DUR = 3;      // Duration that shadow lasts.
        public static final int DANGER_RADIUS = 3;   // if a enemy is nearer than this to player, player is in danger
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
                if (mBoard[i][j]/10 == 0) sb.append("0"); // leading zero
                sb.append(mBoard[i][j]); sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
