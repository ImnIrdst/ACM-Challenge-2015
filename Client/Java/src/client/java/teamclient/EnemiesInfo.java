package client.java.teamclient;

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
    public int[][] mBoard;                                  // Member Board
    public TreeMap<TiZiiBullet, Integer>[][] bulletHitTime; // Table of Bullets that hits a target in a Certain Time.

    public Board gameBoard;
    public StaticsInfo staticsInfo;
	public AlliesInfo alliesInfo;

	// constructor
    public EnemiesInfo(Board gameBoard, StaticsInfo staticsInfo) {
        this.gameBoard = gameBoard;
	    this.staticsInfo = staticsInfo;

	    this.rows = gameBoard.getNumberOfRows();
        this.cols = gameBoard.getNumberOfColumns();
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
     * @param bullets list of bullets in the Map
     */
    public void updateEnemyBoard(ArrayList<Hunter> hunters, ArrayList<GoldMiner> miners,
                                 ArrayList<Spy> spies, ArrayList<Bullet> bullets) {
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
                    mBoard[i][j] = Consts.BLOCK; // TODO: Maybe Must Be UnCommented.
                }
            }
        }
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
	    // Calculate Bullet Hit Time.
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
	    }
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
			if (time <= 2 && TiZiiUtils.cellToCellDirection(tBullet.cell, player.getCell()) == tBullet.direction){
				System.out.println();
				System.out.println(TiZiiUtils.cellToCellDirection(tBullet.cell, player.getCell()));
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
        public static boolean isMINER_OR_SHADOW(int x){ return x >= MINER && x< MINER + SHADOW_DUR; }
        public static boolean isSPY_OR_SHADOW(int x){ return x >= SPY && x< SPY + SHADOW_DUR; }
        public static boolean isHUNTER_SHADOW(int x){ return x > HUNTER && x< HUNTER + SHADOW_DUR; }
        public static boolean isMINER_SHADOW(int x){ return x > MINER && x< MINER + SHADOW_DUR; }
        public static boolean isSPY_SHADOW(int x){ return x > SPY && x< SPY + SHADOW_DUR; }

        public static final int SHADOW_DUR = 2;      // Duration that shadow lasts.
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
