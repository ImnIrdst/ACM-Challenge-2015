package common.player;

import common.board.Cell;
import common.board.Direction;
import common.constant.Config;
import common.team.Team;

/**
 * Represents hunter. this player has the ability of shooting.
 */
public class Hunter extends Player {

    private int bulletCapicity;

    public Hunter(Team team, int id, Cell initialCell,
            Direction initialDirection) {
        super(team, id, initialCell, initialDirection);
        this.bulletCapicity = Config.BULLET_CAPICITY;
    }

    /**
     * Returns that hunter can attack or not. This is related to last attack
     * cycle.
     */
    public boolean canAttack() {
        return bulletCapicity > 0;
    }

    public int getBulletCapicity() {
        return bulletCapicity;
    }

}
