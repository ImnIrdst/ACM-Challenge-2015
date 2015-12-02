package common.board;

/**
 * This class represents gold.
 */
public class Gold {

    private int id;
    private Cell cell;
    private int timeOfMining;

    public Gold(int id, Cell cell) {
        this.id = id;
        this.cell = cell;
        this.timeOfMining = 0;
    }

    /**
     * Returns the gold id. Every gold has a unique id.
     *
     * @return
     */
    public int getId() {
        return id;
    }

    public int incTimeOfMining() {
        timeOfMining++;
        return timeOfMining;
    }

    public int getTimeOfMining() {
        return timeOfMining;
    }

    /**
     * Returns the cell that contains this gold.
     */
    public Cell getCell() {
        return cell;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gold) {
            Gold gold = (Gold) obj;
            if (this == gold) {
                return true;
            } else if (id == gold.getId()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
