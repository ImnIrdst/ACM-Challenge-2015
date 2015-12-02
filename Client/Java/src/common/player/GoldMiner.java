package common.player;

import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.constant.Config;
import common.team.Team;

/**
 * Represents gold miner player. This player has the ability of dig the cell to
 * reach the gold.
 */
public class GoldMiner extends Player {

    private Gold miningGold;// show the current gold that player is mining.

    public GoldMiner(Team team, int id, Cell initialCell,
            Direction initialDirection) {
        super(team, id, initialCell, initialDirection);
    }

    /**
     * Returns gold that have been mining by the gold miner.
     */
    public Gold getMiningGold() {
        return miningGold;
    }

    /**
     * Set the gold that is mining by the player. Attention: Team client should
     * not call this method directly. This method is called
     * automatically.Calling it could corrupt your game information and it will
     * not affect server!
     */
    public void setMiningGold(Gold miningGold) {
        this.miningGold = miningGold;
    }

}
