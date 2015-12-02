package common.player;

import common.board.Cell;
import common.board.Direction;

/**
 * Represents the bullet that has been fired by hunter.
 */
public class Bullet {

    private static int LAST_ID = 100;
    private Direction dir;
    private Cell cell;
    private int id;

    public void setCell(Cell bulletPosition) {
        this.cell = bulletPosition;
        bulletPosition.setBulletIsIn(this);
    }

    public Bullet(Direction direction, Cell initialPosition, int cycleFire) {
        this.dir = direction;
        this.cell = initialPosition;
        id = LAST_ID++;
    }

    public Direction getMovementDirection() {
        return dir;
    }

    /**
     * Returns current position of bullet.
     */
    public Cell getCell() {
        return cell;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
