package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DirectionScorePair;
import client.java.teamclient.TiZiiClasses.StepsInDirection;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.player.*;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class name:   AlliesInfo
 * Date:         12/2/2015
 * Description:  Contains Info about my players.
 */
public class AlliesInfo {
	public int rows, cols;
	public StaticsInfo staticsInfo;
	public EnemiesInfo enemiesInfo;
	public int[][] mBoard;
    public HashMap<Integer, StepsInDirection> prevDirections;       // for calculating FORWARD_SCORE.
    public TreeSet<TiZiiCoords> blockedCoords;                      // for avoiding collisions and my Bullets.
	public TreeSet<TiZiiCoords> nextPositions;                      // for detecting collisions.
    public TreeSet<TiZiiCoords> curAroundCells;                     // for removing golds
	public TreeSet<Integer> collidedPlayer;                         // for avoiding collisions
	public TreeSet<Integer> idlePlayers;
    public TreeMap<Integer, Integer> hunterBurstFire;               // Maps Hunter Id to A Number ( if greater than BURST_QTY fire else don't shoot).

    public TreeMap<Integer, Integer> assignedGoldToPlayer;          // Maps Each Gold to a player
    public TreeMap<Integer, Integer> assignedPlayerToGold;          // Maps Each Player to a Gold

    public AlliesInfo(TeamClientAi game) {
        this.enemiesInfo = game.enemiesInfo;
        this.staticsInfo = game.staticsInfo;

        this.prevDirections = new HashMap<>();
		this.collidedPlayer = new TreeSet<>();
	    this.hunterBurstFire = new TreeMap<>();

	    this.rows = game.getBoard().getNumberOfRows();
	    this.cols = game.getBoard().getNumberOfColumns();
	    this.mBoard = new int[rows][cols];
    }

    /**
     * updated sum members of the class
     */
    public void updateAlliesInfo(ArrayList<Player> myPlayers){
        this.blockedCoords  = new TreeSet<>();
        this.curAroundCells = new TreeSet<>();
	    this.nextPositions  = new TreeSet<>();
		this.idlePlayers    = new TreeSet<>();
        for (Player player : myPlayers){
	        idlePlayers.add(player.getId());

	        TiZiiCoords coords = new TiZiiCoords(player.getCell());
            for (Cell cell : player.getCell().getAroundCells()){
                curAroundCells.add(new TiZiiCoords(cell));
            }

	        if (player instanceof Hunter)       mBoard[coords.i][coords.j] = Consts.HUNTER;
	        if (player instanceof GoldMiner)    mBoard[coords.i][coords.j] = Consts.MINER;
	        if (player instanceof Spy)          mBoard[coords.i][coords.j] = Consts.SPY;
        }
    }

	/**
	 * returns 4 scores for each direction if we want to go for discovery.
	 * @param player player we want to Calculate scores for it.
	 * @return 4 scores for each direction.
	 */
    public DirectionScorePair[] getDiscoveryMovementScores(Player player){
        DirectionScorePair[] pairs = new DirectionScorePair[4];
        for (int i=0 ; i<4; i++) pairs[i] = new DirectionScorePair(Direction.values()[i], 0);

        Direction forwardDir = player.getMovementDirection();
        Direction backwardDir = TiZiiUtils.getReverseDirection(forwardDir);
        StepsInDirection prevDir = prevDirections.get(player.getId());

        Cell cell = player.getCell();
        int ii = cell.getRowNumber();
        int jj = cell.getColumnNumber();

        for (DirectionScorePair pair : pairs){
            int di = pair.direction.getDeltaRow();
            int dj = pair.direction.getDeltaCol();

            // unSeen Count Scores.
            int unSeenCount = countInWing(cell, pair.direction, StaticsInfo.Consts.UNSEEN);
            pair.score += unSeenCount*Consts.UNSEEN_CELL_SCORE;

            // Block Score.
            int iii = ii+di, jjj = jj+dj;
            Cell cellInDir = cell.getAdjacentCell(pair.direction);
            if (!TiZiiUtils.inRange(iii,jjj) || (cellInDir != null && cellInDir.getPlayerInside() != null)
                    || staticsInfo.mBoard[iii][jjj] == StaticsInfo.Consts.BLOCK
                    || blockedCoords.contains(new TiZiiCoords(cellInDir))){
                pair.score += Consts.BLOCK_SCORE;
            }

            // Forward Movement Score.
            if (prevDir != null && prevDir.direction == pair.direction){
                prevDir.steps++;
                pair.score += Consts.FORWARD_SCORE * prevDir.steps;
            }

            // Backward Movement Scores.
            if (backwardDir == pair.direction) pair.score += Consts.BACKWARD_SCORE;
        }
        return pairs;
    }

	/**
	 * Soroush wings Idea.
	 * @param cell Source Cell.
	 * @param direction Direction of Wing.
	 * @param subject Subject that we need to count.
	 * @return number of occurrence of subject.
	 */
    public int countInWing(Cell cell, Direction direction, int subject){
        int ii = cell.getRowNumber();
        int jj = cell.getColumnNumber();

        int di = direction.getDeltaRow();
        int dj = direction.getDeltaCol();

        int cnt = 0;
        if (di == 0){
            for (int i=ii-1 ; i>=ii-Consts.WING_LENGTH ; i--){
                if (!TiZiiUtils.inRange(i,jj)) break;
                if (staticsInfo.discoveredAreas[i][jj] == StaticsInfo.Consts.BLOCK) break;
                for (int j=jj+dj ; true ; j+=dj){
                    if (!TiZiiUtils.inRange(i, j)) break;
                    if (staticsInfo.discoveredAreas[i][j] == StaticsInfo.Consts.BLOCK) break;
                    if (staticsInfo.discoveredAreas[i][j] == subject) cnt++;
                }
            }
            for (int i=ii ; i<=ii+Consts.WING_LENGTH ; i++){
                if (!TiZiiUtils.inRange(i,jj)) break;
                if (staticsInfo.discoveredAreas[i][jj] == StaticsInfo.Consts.BLOCK) break;
                for (int j=jj+dj ; true  ; j+=dj){
                    if (!TiZiiUtils.inRange(i, j)) break;
                    if (staticsInfo.discoveredAreas[i][j] == StaticsInfo.Consts.BLOCK) break;
                    if (staticsInfo.discoveredAreas[i][j] == subject) cnt++;
                }
            }
        }
        if (dj == 0){
            for (int j=jj-1 ; j>=jj-Consts.WING_LENGTH ; j--){
                if (!TiZiiUtils.inRange(ii, j)) break;
                if (staticsInfo.discoveredAreas[ii][j] == StaticsInfo.Consts.BLOCK) break;
                for (int i=ii+di ; TiZiiUtils.inRange(i,jj) ; i+=di){
                    if (!TiZiiUtils.inRange(i, j)) break;
                    if (staticsInfo.discoveredAreas[i][j] == StaticsInfo.Consts.BLOCK) break;
                    if (staticsInfo.discoveredAreas[i][j] == subject) cnt++;
                }
            }
            for (int j=jj ; j<=jj+Consts.WING_LENGTH ; j++){
                if (!TiZiiUtils.inRange(ii, j)) break;
                if (staticsInfo.discoveredAreas[ii][j] == StaticsInfo.Consts.BLOCK) break;
                for (int i=ii+di ; TiZiiUtils.inRange(i,jj) ; i+=di){
                    if (!TiZiiUtils.inRange(i, j)) break;
                    if (staticsInfo.discoveredAreas[i][j] == StaticsInfo.Consts.BLOCK) break;
                    if (staticsInfo.discoveredAreas[i][j] == subject) cnt++;
                }
            }
        }
        return cnt;
    }

	/**
	 * Checks That No Allies inSight of Hunter. (before Shooting)
	 * @param hunter that we must do this for him.
	 * @return true if no Allies insight.
	 */
	public boolean noAlliesInsight(Hunter hunter) {
		for (Cell cell : hunter.getAheadCells()){
			Player playerInside = cell.getPlayerInside();
			if (playerInside != null && playerInside.getTeam().getId() == hunter.getTeam().getId()) return false;
		}
		return true;
	}

	/**
	 * Checks the player sight and around the gold. then pave the way.
	 * @param player that must do this check for.
	 * @return Direction that must be avoided. Not null if gold inSight (with a limit) and a miner is near that gold.
	 */
	public Direction isGoldOnSightAndMinerNearby(Player player) {
		int step = 0, limit = 3;
		Cell cell = player.getCell();
		while (step < limit && cell != null){
			int i = cell.getRowNumber();
			int j = cell.getColumnNumber();
			if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.GOLD){
				for (int ii = i-2 ; ii<i+2; ii++){
					for (int jj = j-2 ; jj<j+2 ; jj++){
						if (!TiZiiUtils.inRange(ii, jj)) continue;
						if (mBoard[ii][jj] == Consts.MINER){
							return TiZiiUtils.cellToCellDirection(player.getCell(), cell);
						}
					}
				}
			}
			step++;
			cell = cell.getAdjacentCell(player.getMovementDirection());
		}
		return null;
	}


	public static class Consts {
		public static final int HUNTER = 5;
		public static final int MINER = 6;
		public static final int SPY = 7;

        public static final int BLOCK_SCORE = -1000;
        public static final int FORWARD_SCORE = 2;
        public static final int BACKWARD_SCORE  = -500;
        public static final int UNSEEN_CELL_SCORE = 50;
        public static final int WING_LENGTH = 2;
        public static final int BURST_QTY = 2;
    }
}
