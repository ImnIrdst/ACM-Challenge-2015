package common.player;

import common.board.Cell;
import common.board.Direction;
import common.constant.Config;
import common.team.Team;

public class Spy extends Player {

    private boolean isHidden = false;

    private int hidenessCapacity;

    public Spy(Team team, int id, Cell initialCell, Direction initialDirection) {
        super(team, id, initialCell, initialDirection);
        hidenessCapacity = Config.SPY_HIDENESS_CAPACITY;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public int getHidenessCapacity() {
        return hidenessCapacity;
    }
}
