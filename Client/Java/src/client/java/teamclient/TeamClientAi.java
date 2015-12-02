package client.java.teamclient;

import client.java.communication.ClientGame;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;
import java.util.ArrayList;
import java.util.Random;

/**
 *  Class name: TeamClientAi
 *
 *  Date: 12/2/2015
 */

// TODO: pay attention that all ifs must have else.

public class TeamClientAi extends ClientGame {
    public String getTeamName() { return "TiZii!"; }

    // Member Variables
    public Board board;                         // Contains given grid
    public BoardTiZii boardTiZii;               // Contains info gathered from the map
    public boolean initialized = false;         // Shows that global variables are initialized or not

    /**
     * Runs in each Cycle (main Function)
     */
    public void step() {
        if(!initialized) initialize();

        System.out.println(boardTiZii);

        ArrayList<Gold> golds = getGolds();     // Add Golds to Tizi map
        for (Gold gold : golds) boardTiZii.setCell(gold.getCell(), BoardTiZii.Consts.Gold);


        ArrayList<GoldMiner> goldMiners = getMyGoldMiners();
        ArrayList<Hunter> hunters = getMyHunters();
        ArrayList<Spy> spies = getMySpies();
        for (GoldMiner miner : goldMiners)  minerLogic(miner);
        for (Hunter hunter : hunters)       hunterLogic(hunter);
        for (Spy spy : spies)               spyLogic(spy);
    }

    /**
     * Initializes member variables
     *
     * Runs only in first
     */
    private void initialize(){
        System.out.println("Initialize :D");
        board = getBoard();
        boardTiZii = new BoardTiZii(
                board.getNumberOfRows(),
                board.getNumberOfColumns());
        initialized = true;
    }

    private void minerLogic(GoldMiner miner) {
        boardTiZii.addPlayerSight(miner);
        if (board.getGold(miner.getCell()) == null) {// !isOnGold TODO
            Cell frontCell = miner.getCell().getAdjacentCell(miner.getMovementDirection());

            Random rand;
            rand = new Random();
            int r = rand.nextInt(40);
            if (r < 35 && frontCell!=null && frontCell.isEmpty()) {
                move(miner);
            } else {
                rotateRand(miner);
            }
        } else {
            //DO NOTHING. MINING
        }
    }

    private void hunterLogic(Hunter hunter) {
        boardTiZii.addPlayerSight(hunter);
        if (hunter.getVisibleEnemy().size() > 0) {// can kill someone
            fire(hunter);
        } else {// can not kill anyone
            Cell frontCell = hunter.getCell().getAdjacentCell(hunter.getMovementDirection());
            Random rand;
            rand = new Random();
            int r = rand.nextInt(4);
            if (r < 3 && frontCell != null && frontCell.isEmpty()) {
                move(hunter);

            } else {
                rotateRand(hunter);
            }
        }
    }

    private void spyLogic(Spy spy) {
        boardTiZii.addPlayerSight(spy);
        Random rand = new Random();
        int r = rand.nextInt(10);
        if (r < 2) {
            if (spy.isHidden()) {
                hide(spy);
            } else {
                show(spy);
            }
        } else {
            Cell frontCell = spy.getCell().getAdjacentCell(spy.getMovementDirection());
            rand = new Random();
            r = rand.nextInt(4);
            if (r < 3 && frontCell!=null && frontCell.isEmpty()) {
                move(spy);
            } else {
                rotateRand(spy);
            }
        }
    }

    private boolean playerCanGo(Player player, Direction dir) {
        if (player.getCell().getAdjacentCell(dir) != null &&
                player.getCell().getAdjacentCell(dir).isEmpty()) {
            return true;
        }
        return false;
    }

    private void rotateRand(Player player) {
        Random rand;
        rand = new Random();
        int r = rand.nextInt(4);
        Direction[] directions = Direction.values();

        for (int i = 0; i < 4; i++) {
            Direction dir = directions[(r + i) % 4];
            if (playerCanGo(player, dir)) {
                rotate(player, dir);
                break;
            }
        }
    }
}
