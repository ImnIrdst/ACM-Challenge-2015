package client.java.teamclient.TiZiiClasses;

import common.board.Cell;

/**
 * Created by iman on 12/3/15.
 */
public class TiZiiPoint {
    public int i;
    public int j;

    public TiZiiPoint(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public TiZiiPoint(Cell cell){
        this.i = cell.getRowNumber();
        this.j = cell.getColumnNumber();
    }
}
