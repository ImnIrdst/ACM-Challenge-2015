package client.java.teamclient;

import client.java.communication.ClientGame;
import client.java.teamclient.TiZiiClasses.*;
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
import java.util.TreeMap;

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
        enemiesInfo.updateEnemyBoard(getOpponentHunters(), getOpponentGoldMiners(), getOpponentSpies(), getBullets());
        TiZiiUtils.update(alliesInfo);

        // logging
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)          System.out.println(staticsInfo);
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)          System.out.println(enemiesInfo);
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printBoard(gameBoard.getCells(), "Not Null Cells");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printBoard(staticsInfo.goldBFSTable, "Gold BFS TABLE");
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printGoldBfsDirections(staticsInfo.discoveryBFSTable, staticsInfo.discoveryIdToCoords, "Discovery BFS Directions");
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printGoldBfsDirections(staticsInfo.goldBFSTable, staticsInfo.goldIdToCoordMap, "Gold BFS Directions");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)       TiZiiUtils.printBoard(staticsInfo.discoveredAreas, "Discovered Areas.");
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printBulletsHitTime(getBullets(), "Bullets Hit Time");


        // Game Logic.
        ArrayList<GoldMiner> goldMiners = getMyGoldMiners();
        ArrayList<Hunter> hunters = getMyHunters();
        ArrayList<Spy> spies = getMySpies();

	    for (Hunter hunter : hunters)       hunterLogic(hunter);    // Must Come Before Miners and Spies
	    for (GoldMiner miner : goldMiners)  minerLogic(miner);
        for (Spy spy : spies)               spyLogic(spy);


	    // log targets
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed) {
		    for (Hunter hunter : hunters) {
			    System.out.print("Hunter " + new TiZiiCoords(hunter.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(hunter.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(hunter.getId()) + " Discovery");
			    } else System.out.println("Not Assigned");
			    System.out.println();
		    }
		    for (GoldMiner miner : goldMiners) {
			    System.out.print("Miner " + new TiZiiCoords(miner.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(miner.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(miner.getId()) + " Discovery");
			    } else if (alliesInfo.assignedPlayerToGold.containsKey(miner.getId())) {
				    System.out.println(staticsInfo.goldIdToCoordMap.get(alliesInfo.assignedPlayerToGold.get(miner.getId())) + " Gold");
			    } else System.out.println("Not Assigned");
			    System.out.println();
		    }
		    for (Spy spy : spies) {
			    System.out.print("Spy " + new TiZiiCoords(spy.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(spy.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(spy.getId()) + " Discovery");
			    } else System.out.println("Not Assigned");
			    System.out.println();
		    }
		    System.out.println();
	    }
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
	        TiZiiUtils.update(alliesInfo);
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
	    if (enemiesInfo.isInRangeOfBullets(miner)){
		    avoidBullets(miner); return;
	    }
        Gold gold = gameBoard.getGold(miner.getCell());
        if (gold == null) {
            TiZiiCoords u = new TiZiiCoords(miner.getCell());

            // if miner assigned to a gold and gold exists go there.
            Integer assignedGold = alliesInfo.assignedPlayerToGold.get(miner.getId());
            if (assignedGold != null && staticsInfo.goldIdToCoordMap.containsKey(assignedGold)){
                DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(assignedGold);
	            if (pair != null) moveOrRotate(miner, pair.direction); // TODO: Null Pointer Exception.
	            else              randomMove(miner);                   // TODO: This maybe fixed doe to considering neighboring cells.
            } else if (alliesInfo.idlePlayers.contains(miner.getId())){ // else pick the not assigned nearest target.
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
					discoveryMove(miner);
                }
            } else discoveryMove(miner);
        }
        // else just mine;
    }

    /**
     * Defines how a hunter should act.
     * Runs per each hunter.
     */
    private void hunterLogic(Hunter hunter) {
	    if (enemiesInfo.isInRangeOfBullets(hunter)){
		    avoidBullets(hunter); return;
	    }
        if (hunter.getVisibleEnemy().size() > 0 && hunter.canAttack()
		        && alliesInfo.noAlliesInsight(hunter)) {// can kill someone
            fire(hunter);
	        for (Cell cell : hunter.getAheadCells()){
		        alliesInfo.blockedCoords.add(new TiZiiCoords(cell));
	        }
        } else { // can not kill anyone
	        // discovery move.
	        DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(hunter);
	        Arrays.sort(scorePair);
	        scoreMove(hunter, scorePair);
        }
    }

    /**
     * Defines how a spy should act.
     * Runs per each spy.
     */
    private void spyLogic(Spy spy) {
	    //System.out.println("Spy : " + spy.isHidden());
	    if (enemiesInfo.isInRangeOfBullets(spy)){
		    avoidBullets(spy); return;
	    }
        if (enemiesInfo.isEnemyHunterNearby(spy.getCell()) && !spy.isHidden()) {
	        hide(spy); return;
        }
        else if (spy.isHidden()){
	        show(spy); return;
        }

        DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(spy);
        scoreMove(spy, scorePair);
    }

	/**
	 * @param player player that must avoid bullets
	 */
    private void avoidBullets(Player player){
	    System.out.println("avoid Bullets " + player.getCell());
	    TiZiiCoords tCoords = new TiZiiCoords(player.getCell());
	    TreeMap<TiZiiBullet, Integer> bulletHitTimeIJ = enemiesInfo.bulletHitTime[tCoords.i][tCoords.j];

	    for (TiZiiBullet tBullet :bulletHitTimeIJ.keySet()){
			Integer time = bulletHitTimeIJ.get(tBullet);
		    if (time == 1 && tBullet.direction != player.getMovementDirection()
				    && tBullet.direction == TiZiiUtils.getReverseDirection(player.getMovementDirection())){

			    if (canGo(player, player.getMovementDirection())) move(player);
			    else                                              randomMove(player); // player is dead. :(
		    }
		    if (time == 2){
			    // if Bullet Direction is Normal to Player Movement Direction.
			    if (canGo(player, player.getMovementDirection()) && tBullet.direction != player.getMovementDirection()
					    && tBullet.direction == TiZiiUtils.getReverseDirection(player.getMovementDirection())) {
					move(player); return;
			    }
				// else turn player to a direction that he can go and its Normal to Bullet Direction.
			    for (Direction direction : Direction.values()){
				    if (canGo(player, direction) && tBullet.direction != direction
						    && tBullet.direction == TiZiiUtils.getReverseDirection(direction)) {
					    move(player); return;
				    }
			    }
			    randomMove(player);
			    // else player is dead :(
		    }
	    }
    }

	/**
	 * Discovery Move for Player.
	 * @param player that must move.
	 */
	private void discoveryMove(Player player){
		TiZiiCoords u = new TiZiiCoords(player.getCell());
		TiZiiCoords assignedTarget = staticsInfo.assignedPlayerToDiscoveryTarget.get(player.getId());
		if (assignedTarget != null){
			int id = staticsInfo.discoveryCoordsToId.get(assignedTarget);
			DistanceDirectionPair pair = staticsInfo.discoveryBFSTable[u.i][u.j].get(id);
			if (pair != null) moveOrRotate(player, pair.direction); // TODO: Null Pointer Exception.
			else              randomMove(player);                   // TODO: This maybe fixed doe to considering neighboring cells.
		} else { // assign new target.
			Integer minDist = (int) 1e8;
			TiZiiCoords assignedCoords = null;
			TiZiiCoords playerCoords = new TiZiiCoords(player.getCell());
			for (int i = 0; i < staticsInfo.rows; i++) {
				for (int j = 0; j < staticsInfo.cols; j++) {
					TiZiiCoords thisCoords = new TiZiiCoords(i, j);
					if (staticsInfo.discoveredAreas[i][j] > 0) continue;
					if (staticsInfo.assignedDiscoveryTargetToPlayer.containsKey(thisCoords)) continue;

					boolean isEdgeCell = false;
					for (int ii = i - 1; ii <= i + 1; ii++) {
						for (int jj = j - 1; jj <= j + 1; jj++) {
							if (!TiZiiUtils.inRange(ii, jj)) continue;
							if (staticsInfo.discoveredAreas[ii][jj] != StaticsInfo.Consts.UNSEEN
									&& staticsInfo.discoveredAreas[ii][jj] != StaticsInfo.Consts.BLOCK)
								isEdgeCell = true;
						}
					}

					if (isEdgeCell) {
						if (minDist > TiZiiUtils.manhattanDistance(playerCoords, thisCoords)
								&& TiZiiUtils.canReach(thisCoords, playerCoords)) {
							minDist = TiZiiUtils.manhattanDistance(playerCoords, thisCoords);
							assignedCoords = thisCoords;
						}
					}
				}

			}

			if (assignedCoords != null) {
				int id; // Assign Id To Coord
				for (id = 0; true; id++) {
					if (!staticsInfo.discoveryIdToCoords.containsKey(id)) {
						staticsInfo.discoveryIdToCoords.put(id, assignedCoords);
						staticsInfo.discoveryCoordsToId.put(assignedCoords, id);
						break;
					}
				}
				TiZiiUtils.BFS(id, assignedCoords, staticsInfo.discoveryBFSTable, false);

				staticsInfo.assignedDiscoveryTargetToPlayer.put(assignedCoords, player.getId());
				staticsInfo.assignedPlayerToDiscoveryTarget.put(player.getId(), assignedCoords);
				alliesInfo.idlePlayers.remove(player.getId());

				discoveryMove(player); // call recursively after assigned to a target.
			} else {
				DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(player);
				Arrays.sort(scorePair);
				scoreMove(player, scorePair);
			}
		}
	}

	/**
	 * Move Player To a Direction With Highest Score. (Includes Some Randomness).
	 * @param player that needs to move.
	 * @param scorePairs Array DirectionScorePair that Contain Score for Each Player.
	 */
    private void scoreMove(Player player, DirectionScorePair[] scorePairs) {
        Arrays.sort(scorePairs);

        boolean isFirst = true;
        int rand = TiZiiUtils.getRandomNumber(10);
        for (DirectionScorePair pair : scorePairs) {
            if (isFirst && rand < 1){ isFirst = false; continue; } isFirst = false; // Skip Best Score sometimes.
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
        for (DirectionScorePair pair : scorePairs) {
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
            alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell().getAdjacentCell(direction)));
        } else if (player.getMovementDirection().equals(direction) && !canGo(player, direction)){
			randomMove(player);
        } else {
            rotate(player, direction);
            alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
        }

        // log
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded){
            TiZiiCoords coord = new TiZiiCoords(player.getCell());
            if (player instanceof Spy)
                System.out.println("Spy (" + coord.i + "," + coord.j + "): " + direction);
            if (player instanceof GoldMiner)
                System.out.println("Miner (" + coord.i + "," + coord.j + "): " + direction);
        }
    }

    private boolean canGo(Player player, Direction dir) {
        return player.getCell().getAdjacentCell(dir) != null
                && player.getCell().getAdjacentCell(dir).isEmpty()
                && !alliesInfo.blockedCoords.contains(new TiZiiCoords(player.getCell().getAdjacentCell(dir)));
    }

    private void randomMove(Player player){
        int rand = TiZiiUtils.getRandomNumber(100);
        if (rand <= 95 && canGo(player, player.getMovementDirection())) {
            move(player);
	        TiZiiCoords nextCoords = new TiZiiCoords(player.getCell().getAdjacentCell(player.getMovementDirection()));
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
