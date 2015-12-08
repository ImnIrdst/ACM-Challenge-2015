package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.TiZiiBullet;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import common.board.Board;
import common.board.Cell;
import common.player.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

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
    // TODO: Doesn't Work Correctly Spy Doesn't Hide Correctly.
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
                }
//                } else if (mBoard[i][j] == Consts.UNSEEN && staticsInfo.mBoard[i][j] == Consts.EMPTY){  // updates unseen cells
//                    mBoard[i][j] = Consts.EMPTY;
//                } else if (mBoard[i][j] == Consts.UNSEEN && staticsInfo.mBoard[i][j] == Consts.BLOCK){  // updates unseen cells
//                    mBoard[i][j] = Consts.BLOCK;
//                }
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

		    int time = 0;
		    for (Cell cell = tBullet.cell ; cell != null ; cell = cell.getAdjacentCell(tBullet.direction)){
				int i = cell.getRowNumber();
			    int j = cell.getColumnNumber();
			    bulletHitTime[i][j].put(tBullet, time);
			    time++;
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
	            if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed) System.out.print((mBoard[i][j] < 10 ? "0" :"") + mBoard[i][j] + " ");
                if (Consts.isHUNTER_OR_SHADOW(mBoard[i][j])) return true;
            }
	        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed) System.out.println();
        }
        return false;
    }


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

        public static final int SHADOW_DUR = 10;      // Duration that shadow lives
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
