package client.java.teamclient;

import client.java.communication.ClientGame;
import client.java.teamclient.TiZiiClasses.DirectionScorePair;
import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.StepsInDirection;
import client.java.teamclient.TiZiiClasses.TiZiiPoint;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *  Class name:     TeamClientAi
 *  Date:           12/2/2015
 */

// TODO: pay attention that all ifs must have else.

public class TeamClientAi extends ClientGame {
    public String getTeamName() { return "TiZii!"; }

    // Class Members.
    public Board gameBoard;                     // Contains given gameBoard.
    StaticsInfo staticsInfo;                    // Contains info about static object in the map.
    EnemiesInfo enemiesInfo;                    // Contains info about enemy players.
    AlliesInfo  alliesInfo;                     // Contains info about my players.
    public boolean firstTimeInitialize = false;         // Shows that global variables are Initilize or not.

    /**
     * Runs in each Cycle (main Function).
     */
    public void step() {
        initialize();

        staticsInfo.updateStaticBoard(getGolds());                                                          System.out.println(staticsInfo);
        enemiesInfo.updateEnemyBoard(getOpponentHunters(), getOpponentGoldMiners(), getOpponentSpies());    System.out.println(enemiesInfo);


        ArrayList<GoldMiner> goldMiners = getMyGoldMiners();
        ArrayList<Hunter> hunters = getMyHunters();
        ArrayList<Spy> spies = getMySpies();

        for (GoldMiner miner : goldMiners)  minerLogic(miner);
        for (Hunter hunter : hunters)       hunterLogic(hunter);
        for (Spy spy : spies)               spyLogic(spy);
    }

    /**
     * Initializes member variables.
     * Runs only in first Cycle.
     */
    private void initialize(){
        gameBoard = getBoard();
        if (!firstTimeInitialize) {
            staticsInfo = new StaticsInfo(gameBoard);
            enemiesInfo = new EnemiesInfo(gameBoard, staticsInfo);
            alliesInfo = new AlliesInfo(enemiesInfo, staticsInfo);
            TiZiiUtils.rows = gameBoard.getNumberOfRows();
            TiZiiUtils.cols = gameBoard.getNumberOfColumns();
            System.out.println("Initialized :D");
        } else {
            staticsInfo.gameBoard = gameBoard;
            enemiesInfo.gameBoard = gameBoard;
        }
        firstTimeInitialize = true;
    }

    /**
     * Defines how a miner should act.
     * Runs per each miner.
     */
    private void minerLogic(GoldMiner miner) {
        Gold gold = gameBoard.getGold(miner.getCell());
        if (gold == null) {
            TiZiiPoint u = new TiZiiPoint(miner.getCell());

            Integer assignedGold = alliesInfo.assignedGoldToPlayer.get(miner.getId());
            if (assignedGold != null && !staticsInfo.deadGolds.contains(assignedGold)){
                DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(assignedGold);
                moveOrRotate(miner, pair.direction);
            }
            else {
                DistanceDirectionPair minDistPair = null;
                for (Integer key : staticsInfo.goldBFSTable[u.i][u.j].keySet()){
                    DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(key);
                    if (minDistPair == null || pair.distance < minDistPair.distance) minDistPair = pair;
                }
                if (minDistPair != null){
                    moveOrRotate(miner, minDistPair.direction);
                } else randomMove(miner);
            }
        }
        // else just mine;
    }

    /**
     * Defines how a hunter should act.
     * Runs per each hunter.
     */
    private void hunterLogic(Hunter hunter) {
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
                randomRotate(hunter);
            }
        }
    }

    /**
     * Defines how a spy should act.
     * Runs per each spy.
     */
    private void spyLogic(Spy spy) {
        if (enemiesInfo.isEnemyHunterNearby(spy.getCell()))
            spy.setHidden(true);
        else spy.setHidden(false);
        DirectionScorePair[] scorePair = alliesInfo.getMovementScoresForSpy(spy);
        tiziiMove(spy, scorePair);
    }



    private void tiziiMove(Player player, DirectionScorePair[] scorePair) {
        Arrays.sort(scorePair); // TODO: Add a random value to not to take best direction
        for (DirectionScorePair pair : scorePair) {
            if (canGo(player, pair.direction)) {
                moveOrRotate(player, pair.direction);
                StepsInDirection entry = alliesInfo.prevDirections.get(player.getId());
                if (entry == null)
                    alliesInfo.prevDirections.put(player.getId(), new StepsInDirection(0, pair.direction));
                else if (entry.direction.equals(pair.direction))
                    entry.steps++;

                return;
            }
        }
    }


    private void moveOrRotate(Player player, Direction direction) {
        if (player.getMovementDirection().equals(direction))
            move(player);
        else rotate(player, direction);
    }

    private boolean canGo(Player player, Direction dir) {
        return player.getCell().getAdjacentCell(dir) != null &&
                player.getCell().getAdjacentCell(dir).isEmpty();
    }

    private void randomMove(Player player){
        Random rand;
        rand = new Random();
        int r = rand.nextInt(40);
        if (r < 35 && canGo(player, player.getMovementDirection())) {
            move(player);
        } else {
            randomRotate(player);
        }
    }

    private void randomRotate(Player player) {
        Random rand;
        rand = new Random();
        int r = rand.nextInt(4);
        Direction[] directions = Direction.values();

        for (int i = 0; i < 4; i++) {
            Direction dir = directions[(r + i) % 4];
            if (canGo(player, dir) && !dir.equals(player.getMovementDirection())) {
                rotate(player, dir); break;
            }
        }
    }
}
