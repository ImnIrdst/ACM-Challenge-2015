package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.TiZiiBullet;
import client.java.teamclient.TiZiiClasses.TiZiiCoords;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.player.Bullet;

import java.util.*;

/**
 * Created by iman on 12/2/15.
 *
 */
public class TiZiiUtils {
    static int rows, cols;
    static Board gameBoard;
    static AlliesInfo alliesInfo;
    static EnemiesInfo enemiesInfo;
    static StaticsInfo staticsInfo;

    // logging functionality
    static boolean needed = true;
    static boolean notNeeded = false;
    static boolean isLoggingEnabled = false;

    public static void update(TeamClientAi game){
        alliesInfo = game.alliesInfo;
        enemiesInfo = game.enemiesInfo;
        staticsInfo = game.staticsInfo;

        rows = game.getBoard().getNumberOfRows();
        cols = game.getBoard().getNumberOfColumns();
        gameBoard = game.getBoard();
    }

    /**
     * Runs a bfs for gold if two phases. 1. Clearing 2. not Clearing
     * @param itemId root of the bfs.
     * @param isClearing defines phase of the bfs.
     */
    public static void BFS(Integer itemId, TiZiiCoords itemCoords, TreeMap[][] bfsTable, boolean isClearing){
        TiZiiCoords s = itemCoords;
        Queue<TiZiiCoords> q = new LinkedList<>();

        int[][] vis = new int[rows][cols];
        for (int i=0 ; i<rows ; i++) Arrays.fill(vis[i], -1);

        q.add(s); vis[s.i][s.j] = 0;

        while (!q.isEmpty()){
            TiZiiCoords u = q.poll();
            for (Direction dir : Direction.values()){
                TiZiiCoords v = new TiZiiCoords(u.i + dir.getDeltaRow(), u.j + dir.getDeltaCol());

	            if (TiZiiUtils.inRange(v.i, v.j) && vis[v.i][v.j] < 0
			            && staticsInfo.mBoard[v.i][v.j] != StaticsInfo.Consts.UNSEEN
                        && staticsInfo.mBoard[v.i][v.j] != StaticsInfo.Consts.BLOCK) {

		            q.add(v);
		            vis[v.i][v.j] = vis[u.i][u.j] + 1;
						if (!isClearing)
							bfsTable[v.i][v.j].put(itemId,
									new DistanceDirectionPair(vis[v.i][v.j], TiZiiUtils.getReverseDirection(dir)));

						else if (bfsTable[v.i][v.j].containsKey(itemId))
							bfsTable[v.i][v.j].remove(itemId);

	            }
            }
        }
    }

	public static boolean canReach(TiZiiCoords s, TiZiiCoords t){
		//staticsInfo.goldBFSCalculated.add(itemId);

		Queue<TiZiiCoords> q = new LinkedList<>();

		int[][] vis = new int[rows][cols];
		for (int i=0 ; i<rows ; i++) Arrays.fill(vis[i], -1);

		q.add(s); vis[s.i][s.j] = 0;

		while (!q.isEmpty()){
			TiZiiCoords u = q.poll();
			for (Direction dir : Direction.values()){
				TiZiiCoords v = new TiZiiCoords(u.i + dir.getDeltaRow(), u.j + dir.getDeltaCol());
				if (TiZiiUtils.inRange(v.i, v.j) && vis[v.i][v.j] < 0 && staticsInfo.mBoard[v.i][v.j] == StaticsInfo.Consts.EMPTY) {
					q.add(v);
					vis[v.i][v.j] = vis[u.i][u.j] + 1;
					if (v.equals(t)) return true;
				}
			}
		}
		return false;
	}

    public static int getRandomNumber(int x){
        Random rand;
        rand = new Random();
        return rand.nextInt(x);
    }

    public static int getDirectionID(Direction direction){
        for (int i=0 ; i<4 ; i++){
            if (Direction.values()[i].equals(direction)) return i;
        }
        return 4;
    }

    public static Direction getReverseDirection(Direction direction){
        return Direction.values()[(getDirectionID(direction)+ 2)%4];
    }

    public static Direction getReversePositiveNormalDirection(Direction direction){
        return Direction.values()[(getDirectionID(direction)+ 1)%4];
    }

    public static Direction getReverseNegativeNormalDirection(Direction direction){
        return Direction.values()[(getDirectionID(direction)+ 3)%4];
    }

	public static Direction cellToCellDirection(Cell c1, Cell c2){
		if (c1.getRowNumber() == c2.getRowNumber() && c1.getColumnNumber() > c2.getColumnNumber()) return Direction.LEFT;
		if (c1.getRowNumber() == c2.getRowNumber() && c1.getColumnNumber() < c2.getColumnNumber()) return Direction.RIGHT;
		if (c1.getColumnNumber() == c2.getColumnNumber() && c1.getRowNumber() > c2.getRowNumber()) return Direction.DOWN;
		if (c1.getColumnNumber() == c2.getColumnNumber() && c1.getRowNumber() < c2.getRowNumber()) return Direction.UP;
		return Direction.LEFT;
	}

    public static int manhattanDistance(TiZiiCoords a, TiZiiCoords b){
	    return Math.abs(a.i - b.i) + Math.abs(a.j - b.j);
    }

    public static boolean inRange(int i, int j) {
        return (i<rows && i>=0 && j<cols && j>=0);
    }

    public static void printBoard(int[][] a, String title){
        System.out.println(title);
        for (int[] ai : a) {
            for (int aij : ai) {
                System.out.print(aij + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void printBoard(TreeMap[][] a, String title){
        System.out.println(title);
        for (TreeMap[] ai : a) {
            for (TreeMap aij : ai) {
                System.out.print(aij.size() + " ");
            }System.out.println();
        }
        System.out.println();
    }

    public static void printGoldBfsDirections(TreeMap[][] a, TreeMap<Integer, TiZiiCoords> keys, String title) {
        System.out.println(title);
        for (Integer key: keys.keySet()) {
	        System.out.println(keys.get(key));
            for (TreeMap[] ai : a) {
                for (TreeMap aij : ai) {
                    String str = "" + aij.get(key);
	                while (str.length() < 6) str+=" ";
                    System.out.print(str + " ");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

	public static void printBulletsHitTime(String title) {
		System.out.println(title);
		TreeSet<TiZiiBullet> tBullets= new TreeSet<>();
		for (int i=0 ; i<rows ; i++){
			for (int j=0 ; j<cols ; j++){
				for (TiZiiBullet bullet : enemiesInfo.bulletHitTime[i][j].keySet()){
					tBullets.add(bullet);
				}
			}
		}
		for (TiZiiBullet bullet: tBullets) {
			System.out.println("Bullet: " + bullet.id);
			for (TreeMap[] ai : enemiesInfo.bulletHitTime) {
				for (TreeMap aij : ai) {
					String str = (aij.get(bullet) == null ? "" + aij.get(bullet) : "(" + bullet+ "," + aij.get(bullet) +")");
					while (str.length() < 6) str+=" ";
					System.out.print(str + " ");
				}
				System.out.println();
			}
		}
		System.out.println();
	}

    public static void printBoard(Cell[][] a, String title){
        System.out.println(title);
        for (Cell[] ai : a) {
            for (Cell aij : ai) {
                System.out.print((aij != null ? 1 : 0) + " ");
            }System.out.println();
        }
        System.out.println();
    }

    public static class Consts{
        public static final int BFS_RADIUS = 10; // TODO: Not Used. (if Computations are intense Use These.)
	    public static final int MANHATTAN_LIMIT = Math.min(rows, cols)/2;
        public static final boolean isLogginEnabled = true;
	    public static final int HIDING_MOMENT = 15;
    }
}
