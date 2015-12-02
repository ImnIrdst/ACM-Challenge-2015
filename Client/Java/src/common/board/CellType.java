package common.board;

/**
 * This class represent types of cells.
 */
public enum CellType {

    SOLID(false, '1'),
    ROCK(true, '0');

    private final boolean isBlock;
    private final char mapFileSymbol;

    CellType(boolean isBlock, char mapFileSymbol) {
        this.isBlock = isBlock;
        this.mapFileSymbol = mapFileSymbol;
    }

    /**
     * Returns is this cell type is a block or not. A player can not move into a
     * block cell.
     *
     * @return true if is block, false if solid
     */
    public boolean isBlock() {
        return this.isBlock;
    }

    public char getMapFileSymbol() {
        return mapFileSymbol;
    }
}
