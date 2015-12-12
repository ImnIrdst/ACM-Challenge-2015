package client.java.teamclient.TiZiiClasses;

import common.board.Cell;
import common.board.Direction;

/**
 * Created by iman on 12/3/15.
 * Simple (i, j) Coord on the map. (Comparable).
 */
public class TiZiiCoords implements Comparable<TiZiiCoords>{
    public int i;
    public int j;

    public TiZiiCoords(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public TiZiiCoords(Cell cell){
        this.i = cell.getRowNumber();
        this.j = cell.getColumnNumber();
    }
    public TiZiiCoords(TiZiiCoords coord){
        this.i = coord.i;
        this.j = coord.j;
    }

    public TiZiiCoords adjacent(Direction direction) {
        return new TiZiiCoords(i + direction.getDeltaRow(), j + direction.getDeltaCol());
    }

    @Override
    public String toString() {
        return "(" + i + "," + j + ")";
    }

    @Override
    public boolean equals(Object obj) {
        TiZiiCoords a = this, b = (TiZiiCoords)obj;
        return a.i == b.j && a.j == b.j;
    }

    @Override
    public int compareTo(TiZiiCoords b) {
        TiZiiCoords a = this;
        if (a.i != b.i) return Integer.compare(a.i, b.i);
        else            return Integer.compare(a.j, b.j);
    }
}
