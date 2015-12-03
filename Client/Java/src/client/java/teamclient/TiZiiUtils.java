package client.java.teamclient;

import common.board.Cell;
import common.board.Direction;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by iman on 12/2/15.
 *
 */
public class TiZiiUtils {
    static int rows, cols;

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

    public static void printBoard(int[][] a){
        for (int[] ai : a) {
            for (int aij : ai) {
                System.out.print(aij + " ");
            }
            System.out.println();
        }
    }

    public static void printBoard(TreeMap[][] a){
        for (TreeMap[] ai : a) {
            for (TreeMap aij : ai) {
                System.out.print(aij.size() + " ");
            }System.out.println();
        }
    }

    public static class Consts{
        public static final int BFS_RADIUS = 10;
    }
}
