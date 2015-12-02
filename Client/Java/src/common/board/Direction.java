package common.board;

/**
 * This class represents main direction that is used for obtaining adjacent
 * cells.
 */
public enum Direction {

    UP(-1, 0, -1),
    RIGHT(+1, +1, 0),
    DOWN(+1, 0, +1),
    LEFT(-1, -1, 0),
    NONE(0, 0, 0);
    private int directionNumber;
    private int deltaRow;
    private int deltaCol;

    private Direction(int directionNumber, int deltaCol, int deltaRow) {
        this.setDirectionNumber(directionNumber);
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    public int getDirectionNumber() {
        return directionNumber;
    }

    public void setDirectionNumber(int directionNumber) {
        this.directionNumber = directionNumber;
    }

    public int getDeltaRow() {
        return deltaRow;
    }

    public int getDeltaCol() {
        return deltaCol;
    }
}
