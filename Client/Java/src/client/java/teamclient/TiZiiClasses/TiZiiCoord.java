package client.java.teamclient.TiZiiClasses;

import common.board.Cell;

/**
 * Created by iman on 12/3/15.
 * Simple (i, j) Coord on the map. (Comparable).
 */
public class TiZiiCoord implements Comparable<TiZiiCoord>{
    public int i;
    public int j;

    public TiZiiCoord(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public TiZiiCoord(Cell cell){
        this.i = cell.getRowNumber();
        this.j = cell.getColumnNumber();
    }
    public TiZiiCoord(TiZiiCoord coord){
        this.i = coord.i;
        this.j = coord.j;
    }

    @Override
    public String toString() {
        return "(" + i + "," + j + ")";
    }

    @Override
    public boolean equals(Object obj) {
        TiZiiCoord a = this, b = (TiZiiCoord)obj;
        return a.i == b.j && a.j == b.j;
    }

    @Override
    public int compareTo(TiZiiCoord b) {
        TiZiiCoord a = this;
        if (a.i != b.i) return Integer.compare(a.i, b.i);
        else            return Integer.compare(a.j, b.j);
    }
}
