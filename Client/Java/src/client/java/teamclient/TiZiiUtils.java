package client.java.teamclient;

import common.board.Direction;

/**
 * Created by iman on 12/2/15.
 */
public class TiZiiUtils {
    static int rows, cols;

    public static int getDirectionID(Direction direction){
        for (int i=0 ; i<4 ; i++){
            if (Direction.values()[i].equals(direction)) return i;
        }
        return 4;
    }

    /**
     * checks if given i, j are in range of map or not.
     * @param i row number
     * @param j col number
     * @return true if cell(i,j) is in map
     */
    public static boolean inRange(int i, int j) {
        return (i<rows && i>=0 && j<cols && j>=0);
    }
}
