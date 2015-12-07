package client.java.teamclient;

import client.java.communication.ClientGame;
import client.java.teamclient.TiZiiClasses.DirectionScorePair;
import client.java.teamclient.TiZiiClasses.DistanceDirectionPair;
import client.java.teamclient.TiZiiClasses.StepsInDirection;
import client.java.teamclient.TiZiiClasses.TiZiiCoord;
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

        // updating.
        alliesInfo.updateAlliesInfo(getMyPlayers(), getOpponentPlayers(), getBullets()); // must use this before updating staticsBoard
        staticsInfo.updateStaticBoard(getGolds(), getMyPlayers());
        enemiesInfo.updateEnemyBoard(getOpponentHunters(), getOpponentGoldMiners(), getOpponentSpies());
        TiZiiUtils.update(alliesInfo);

        // logging
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)       System.out.println(staticsInfo);
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)    System.out.println(enemiesInfo);
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)    TiZiiUtils.printBoard(gameBoard.getCells(), "Not Null Cells");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)    TiZiiUtils.printBoard(staticsInfo.goldBFSTable, "Gold BFS TABLE");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)       TiZiiUtils.printGoldBfsDirections(staticsInfo.goldBFSTable, "Gold BFS Directions");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)       TiZiiUtils.printBoard(staticsInfo.discoveredAreas, "Discovered Areas.");

        ArrayList<GoldMiner> goldMiners = getMyGoldMiners();
        ArrayList<Hunter> hunters = getMyHunters();
        ArrayList<Spy> spies = getMySpies();

	    for (Hunter hunter : hunters)       hunterLogic(hunter);    // Must Come Before Miners and Spies
	    for (GoldMiner miner : goldMiners)  minerLogic(miner);
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
            alliesInfo = new AlliesInfo(gameBoard, enemiesInfo, staticsInfo);
            staticsInfo.alliesInfo = alliesInfo;
            TiZiiUtils.rows = gameBoard.getNumberOfRows();
            TiZiiUtils.cols = gameBoard.getNumberOfColumns();
            System.out.println("Initialized :D");
        } else {
            staticsInfo.gameBoard = gameBoard;
            enemiesInfo.gameBoard = gameBoard;
            alliesInfo.gameBoard  = gameBoard;
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
            TiZiiCoord u = new TiZiiCoord(miner.getCell());

            // if miner assigned to a gold and gold exists go there.
            Integer assignedGold = alliesInfo.assignedPlayerToGold.get(miner.getId());
            if (assignedGold != null && staticsInfo.goldIdToCoordMap.containsKey(assignedGold)){
                DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(assignedGold);
	            if (pair != null) moveOrRotate(miner, pair.direction); // TODO: Null Pointer Exception.
	            else              randomMove(miner);                   // TODO: This maybe fixed doe to considering neighboring cells.
            } else { // else pick the not assigned nearest target.
                DistanceDirectionPair minDistPair = null; assignedGold = null;
                for (Integer goldId : staticsInfo.goldBFSTable[u.i][u.j].keySet()){
                    DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(goldId);
	                if (alliesInfo.assignedGoldToPlayer.containsKey(goldId)) continue;
                    if (minDistPair == null || pair.distance < minDistPair.distance){
                        minDistPair = pair; assignedGold = goldId;
                    }
                }
                if (minDistPair != null){
                    alliesInfo.assignedPlayerToGold.put(miner.getId(), assignedGold);
	                alliesInfo.assignedGoldToPlayer.put(assignedGold, miner.getId());
                    moveOrRotate(miner, minDistPair.direction);
                } else { // miner discovery move.
	                DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(miner);
	                Arrays.sort(scorePair);
	                tiziiMove(miner, scorePair);
                }
            }
        }
        // else just mine;
    }

    /**
     * Defines how a hunter should act.
     * Runs per each hunter.
     */
    private void hunterLogic(Hunter hunter) {
        if (hunter.getVisibleEnemy().size() > 0 && hunter.canAttack()
		        && alliesInfo.noAlliesInsight(hunter)) {// can kill someone
            fire(hunter);
	        for (Cell cell : hunter.getAheadCells()){
		        alliesInfo.blockedCoords.add(new TiZiiCoord(cell));
	        }
        } else { // can not kill anyone
	        // discovery move.
	        DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(hunter);
	        Arrays.sort(scorePair);
	        tiziiMove(hunter, scorePair);
        }
    }

    /**
     * Defines how a spy should act.
     * Runs per each spy.
     */
    private void spyLogic(Spy spy) {
        if (enemiesInfo.isEnemyHunterNearby(spy.getCell())) spy.setHidden(true);
        else spy.setHidden(false);
        DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(spy);
        tiziiMove(spy, scorePair);
    }

    private void tiziiMove(Player player, DirectionScorePair[] scorePair) {
        Arrays.sort(scorePair); // TODO: Add a random value to not to take best direction

        boolean isFirst = true;
        int rand = TiZiiUtils.getRandomNumber(5);
        for (DirectionScorePair pair : scorePair) {
            if (isFirst && rand < 1){ isFirst = false; continue; } isFirst = false; // Don't Use The Best answer sometimes.
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
        if (player.getMovementDirection().equals(direction) && canGo(player, direction)) {
            move(player);
            alliesInfo.blockedCoords.add(new TiZiiCoord(player.getCell().getAdjacentCell(direction)));
        } else if (player.getMovementDirection().equals(direction) && !canGo(player, direction)){
			randomMove(player);
        } else {
            rotate(player, direction);
            alliesInfo.blockedCoords.add(new TiZiiCoord(player.getCell()));
        }

        // log
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded){
            TiZiiCoord coord = new TiZiiCoord(player.getCell());
            if (player instanceof Spy)
                System.out.println("Spy (" + coord.i + "," + coord.j + "): " + direction);
            if (player instanceof GoldMiner)
                System.out.println("Miner (" + coord.i + "," + coord.j + "): " + direction);
        }
    }

    private boolean canGo(Player player, Direction dir) {
        return player.getCell().getAdjacentCell(dir) != null
                && player.getCell().getAdjacentCell(dir).isEmpty()
                && !alliesInfo.blockedCoords.contains(new TiZiiCoord(player.getCell().getAdjacentCell(dir)));
    }

    private void randomMove(Player player){
        int rand = TiZiiUtils.getRandomNumber(40);
        if (rand < 35 && canGo(player, player.getMovementDirection())) {
            move(player);
	        TiZiiCoord nextCoords = new TiZiiCoord(player.getCell().getAdjacentCell(player.getMovementDirection()));
	        alliesInfo.blockedCoords.add(nextCoords);
        } else {
	        rand = TiZiiUtils.getRandomNumber(4);
	        Direction[] directions = Direction.values();

	        for (int i = 0; i < 4; i++) {
		        Direction dir = directions[(rand + i) % 4];
		        if (canGo(player, dir) && !dir.equals(player.getMovementDirection())) {
			        rotate(player, dir); break;
		        }
	        }
        }
    }
}
