package client.java.teamclient;

import common.board.Board;
import common.board.Cell;
import common.board.Direction;

import java.util.Random;
import java.util.TreeMap;

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
    static boolean isLoggingEnabled = true;

    public static void update(AlliesInfo argAlliesInfoArg){
        alliesInfo = argAlliesInfoArg;
        enemiesInfo = alliesInfo.enemiesInfo;
        staticsInfo = alliesInfo.staticsInfo;

        rows = staticsInfo.rows;
        cols = staticsInfo.cols;
        gameBoard = staticsInfo.gameBoard;
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

    public static boolean inRange(int i, int j) {
        return (i<rows && i>=0 && j<cols && j>=0);
    }

    public static void log(String string){
        if (isLoggingEnabled) System.out.println(string);
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

    public static void printGoldBfsDirections(TreeMap[][] a, String title) {
        System.out.println(title);
        for (Integer key: staticsInfo.goldIdToCoordMap.keySet()) {
            for (TreeMap[] ai : staticsInfo.goldBFSTable) {
                for (TreeMap aij : ai) {
                    String str = "" + aij.get(key);
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
        public static final int BFS_RADIUS = 10;

        public static final boolean isLogginEnabled = true;
    }
}
