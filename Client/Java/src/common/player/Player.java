package common.player;

import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.team.Team;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents player. every gold miner, hunter and spy is a player.
 */
public class Player {

    public static int LAST_ID = 1000;
    private Cell cell;
    private int id;
    private Direction dir;
    private transient Team team;

    /**
     * @param id, is a identifier for player!
     * @param initialCell initial position of the player.
     */
    public Player(Team team, int id, Cell initialCell, Direction initialDirection) {
        this.id = id;
        this.cell = initialCell;
        this.team = team;
        setMovementDirection(initialDirection);
        initialCell.setPlayerInside(this);
        Player.LAST_ID++;
    }

    public int getId() {
        return id;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }

    public Direction getMovementDirection() {
        return dir;
    }

    public void setMovementDirection(Direction movementDirection) {
        this.dir = movementDirection;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
    
    public ArrayList<Cell> getAroundCells() {
        return getCell().getAroundCells();
    }

    public ArrayList<Cell> getAheadCells() {
        return getCell().getAheadCells(getMovementDirection());
    }

    public Cell getAheadCell() {
        return getCell().getAdjacentCell(getMovementDirection());
    }
    /**
     * @definition this method return the cells that player see!
     * @return
     */
    public ArrayList<Cell> getView() {
        HashSet<Cell> view = new HashSet<Cell>();
        view.addAll(getAroundCells());
        view.addAll(getAheadCells());
        
        ArrayList<Cell> result = new ArrayList<Cell>(view);
        return result;
    }


    public ArrayList<Player> getVisibleEnemy() {
        HashSet<Player> enemiesPlayer = new HashSet<>();
        for (Cell c : getAroundCells()) {
            Player pl = c.getPlayerInside();
            if (pl != null
                    && pl.getTeam().getId() != this.getTeam().getId()) {
                enemiesPlayer.add(pl);
            }
        }
        for (Cell c : getAheadCells()) {
            Player pl = c.getPlayerInside();
            if (pl != null
                    && !(pl instanceof Spy)
                    && pl.getTeam().getId() != this.getTeam().getId()) {
                enemiesPlayer.add(pl);
            }
        }
        
        ArrayList<Player> result = new ArrayList<Player>(enemiesPlayer);
        return result;
    }    
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Player other = (Player) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
