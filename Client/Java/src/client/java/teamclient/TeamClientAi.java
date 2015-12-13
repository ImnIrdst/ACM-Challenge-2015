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

import java.lang.annotation.Target;
import java.util.*;

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

	    // shuffle game players for different priorities.
	    TreeSet<Integer> added = new TreeSet<>();
	    ArrayList<Hunter> hunters = new ArrayList<>();
	    for (Hunter hunter : getMyHunters()) {
		    if (!added.contains(hunter.getId())){
			    hunters.add(hunter); added.add(hunter.getId());
		    }
	    }
	    Collections.shuffle(hunters);

	    added = new TreeSet<>();
	    ArrayList<GoldMiner> miners = new ArrayList<>();
	    for (GoldMiner miner : getMyGoldMiners()) {
		    if (!added.contains(miner.getId())){
			    miners.add(miner); added.add(miner.getId());
		    }
	    }
	    Collections.shuffle(miners);

	    added = new TreeSet<>();
	    ArrayList<Spy> spies = new ArrayList<>();
	    for (Spy spy : getMySpies()) {
		    if (!added.contains(spy.getId())){
			    spies.add(spy); added.add(spy.getId());
		    }
	    }
	    Collections.shuffle(spies);


	    added = new TreeSet<>();
	    ArrayList<Player> players = new ArrayList<>();
	    for (Player player : getMyPlayers()) {
		    if (!added.contains(player.getId())){
			    players.add(player); added.add(player.getId());
		    }
	    }
	    Collections.shuffle(players);

        // updating.
        alliesInfo.updateAlliesInfo(players, getOpponentPlayers(), getBullets()); // must use this before updating staticsBoard
        staticsInfo.updateStaticBoard(getGolds(),players);
        enemiesInfo.updateEnemyBoard(getOpponentHunters(), getOpponentGoldMiners(), getOpponentSpies(), getBullets());
	    enemiesInfo.alliesInfo = alliesInfo;
        TiZiiUtils.update(alliesInfo);

        // logging
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)          System.out.println(staticsInfo);
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)          System.out.println(enemiesInfo);
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printBoard(gameBoard.getCells(), "Not Null Cells");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printBoard(staticsInfo.goldBFSTable, "Gold BFS TABLE");
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printGoldBfsDirections(staticsInfo.discoveryBFSTable, staticsInfo.discoveryIdToCoords, "Discovery BFS Directions");
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)       TiZiiUtils.printGoldBfsDirections(staticsInfo.goldBFSTable, staticsInfo.goldIdToCoordMap, "Gold BFS Directions");
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)       TiZiiUtils.printBoard(staticsInfo.discoveredAreas, "Discovered Areas.");
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)           TiZiiUtils.printBulletsHitTime("Bullets Hit Time");


        // Game Logic.
	    for (Hunter hunter : hunters)       hunterLogic(hunter);    // Must Come Before Miners and Spies
	    for (GoldMiner miner : miners)      minerLogic(miner);
        for (Spy spy : spies)               spyLogic(spy);


	    // log targets.
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed) {
		    for (Hunter hunter : hunters) {
			    System.out.print("Hunter " + new TiZiiCoords(hunter.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(hunter.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(hunter.getId()) + " Discovery");
			    } else System.out.println("Not Assigned");
		    }
		    for (GoldMiner miner : miners) {
			    System.out.print("Miner " + new TiZiiCoords(miner.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(miner.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(miner.getId()) + " Discovery");
			    } else if (alliesInfo.assignedPlayerToGold.containsKey(miner.getId())) {
				    System.out.println(staticsInfo.goldIdToCoordMap.get(alliesInfo.assignedPlayerToGold.get(miner.getId())) + " Gold");
			    } else System.out.println("Not Assigned");
		    }
		    for (Spy spy : spies) {
			    System.out.print("Spy " + new TiZiiCoords(spy.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(spy.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(spy.getId()) + " Discovery");
			    } else System.out.println("Not Assigned");
		    }
		    System.out.println();
	    }
	    // log score.
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed){
		    System.out.println("TiZii: " + getMyScore() + ", Opponent: " + getOpponentScore());
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
	    boolean moved = highPriorityMoves(miner); if (moved) return;

        Gold gold = gameBoard.getGold(miner.getCell());
        if (gold == null) {
            TiZiiCoords u = new TiZiiCoords(miner.getCell());

            // if miner assigned to a gold and gold exists go there.
            Integer assignedGold = alliesInfo.assignedPlayerToGold.get(miner.getId());
            if (assignedGold != null && staticsInfo.goldIdToCoordMap.containsKey(assignedGold)){
                DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(assignedGold);
	            if (pair != null) moveOrRotate(miner, pair.direction); // TODO: Null Pointer Exception.
	            else              randomMove(miner);                   // TODO: This maybe fixed doe to considering neighboring cells.
            } // else pick the not assigned nearest target.
//            else if (alliesInfo.idlePlayers.contains(miner.getId())){
//                DistanceDirectionPair minDistPair = null; assignedGold = null;
//                for (Integer goldId : staticsInfo.goldBFSTable[u.i][u.j].keySet()){
//                    DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(goldId);
//	                if (alliesInfo.assignedGoldToPlayer.containsKey(goldId)) continue;
//                    if (minDistPair == null || pair.distance < minDistPair.distance){
//                        minDistPair = pair; assignedGold = goldId;
//                    }
//                }
//                if (minDistPair != null){
//                    alliesInfo.assignedPlayerToGold.put(miner.getId(), assignedGold);
//	                alliesInfo.assignedGoldToPlayer.put(assignedGold, miner.getId());
//                    moveOrRotate(miner, minDistPair.direction);
//                } else { // miner discovery move.
//					discoveryMove(miner);
//                }
//            }
            else discoveryMove(miner);
        }
        // else just mine;
    }

    /**
     * Defines how a hunter should act.
     * Runs per each hunter.
     */
    private void hunterLogic(Hunter hunter) {
	    boolean moved = highPriorityMoves(hunter); if (moved) return;

	    Direction avoidDirection = alliesInfo.isGoldOnSightAndMinerNearby(hunter);
	    if (avoidDirection != null){
		    System.out.println("*************************************************");
		    System.out.println("Hunter " + hunter.getCell() + ": Avoid Direction!");
		    System.out.println("*************************************************");
		    randomMoveAndAvoidDirectionAndGold(hunter, avoidDirection);
	    }

	    Integer burstFireNumber = alliesInfo.hunterBurstFire.get(hunter.getId());
        if (enemiesInfo.isEnemyAhead(hunter) && hunter.canAttack()
		        && alliesInfo.noAlliesInsight(hunter)
		        && (burstFireNumber == null || burstFireNumber > AlliesInfo.Consts.BURST_QTY)) {// can kill someone
            fire(hunter);
	        // burst fire condition.
	        if (burstFireNumber == null){
		        alliesInfo.hunterBurstFire.put(hunter.getId(), AlliesInfo.Consts.BURST_QTY * 2 - 1);
	        }

	        for (Cell cell : hunter.getAheadCells()){
		        alliesInfo.blockedCoords.add(new TiZiiCoords(cell));
	        }
        } else { // can not kill anyone
	        discoveryMove(hunter);
        }
	    if (burstFireNumber != null){
		    alliesInfo.hunterBurstFire.put(hunter.getId(), burstFireNumber - 1);
		    if (burstFireNumber == 0) alliesInfo.hunterBurstFire.remove(hunter.getId());
	    }
    }

    /**
     * Defines how a spy should act.
     * Runs per each spy.
     */
    private void spyLogic(Spy spy) {
	    //System.out.println("Spy : " + spy.isHidden());
	    boolean moved = highPriorityMoves(spy); if (moved) return;

	    Direction avoidDirection = alliesInfo.isGoldOnSightAndMinerNearby(spy);
	    if (avoidDirection != null){
		    System.out.println("*************************************************");
		    System.out.println("Spy " + spy.getCell() + ": Avoid Direction!");
		    System.out.println("*************************************************");
		    randomMoveAndAvoidDirectionAndGold(spy, avoidDirection);
	    }


	    if (enemiesInfo.isEnemyHunterNearby(spy.getCell()) && !spy.isHidden()) {
	        hide(spy); return;
        }
        if (spy.isHidden()){
	        show(spy); return;
        }
	    discoveryMove(spy);
    }



	/**
	 * high priority moves that each player must do before doing his duty.
	 * @param player that must do this type of moves
	 * @return true if player moves.
	 */
	private boolean highPriorityMoves(Player player){
		// if player in range of bullets.
		if (enemiesInfo.isInRangeOfBullets(player)){
			avoidBullets(player); return true;
		}
		// if player avoided collision in prev cycle.
		if (alliesInfo.collidedPlayer.contains(player.getId())
				&& canGo(player, player.getMovementDirection())){
			alliesInfo.collidedPlayer.remove(player.getId());

			// if player is on a discovery target. drop the target.
			if (!alliesInfo.assignedPlayerToGold.containsKey(player.getId())){
				if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(player.getId())){
					staticsInfo.clearDiscoveryTarget(staticsInfo.assignedPlayerToDiscoveryTarget.get(player.getId()));
				}
			}
			move(player); return true;
		}
		// if player is a miner don't use randomness.
		if (player instanceof GoldMiner) return false;

		// try some randomness
		int rand = TiZiiUtils.getRandomNumber(100);
		if (rand > 85){ randomMove(player); return true; }

		return false;
	}

	/**just avoid bullets.
	 * @param player player that must avoid bullets
	 */
    private void avoidBullets(Player player){
	    System.out.println("avoid Bullets " + player.getCell());
	    TiZiiCoords tCoords = new TiZiiCoords(player.getCell());
	    TreeMap<TiZiiBullet, Integer> bulletHitTimeIJ = enemiesInfo.bulletHitTime[tCoords.i][tCoords.j];

	    for (TiZiiBullet tBullet : bulletHitTimeIJ.keySet()){
			Integer time = bulletHitTimeIJ.get(tBullet);
		    if (time == 1 && tBullet.direction != player.getMovementDirection()
				    && tBullet.direction != TiZiiUtils.getReverseDirection(player.getMovementDirection())){

			    if (canGo(player, player.getMovementDirection())) move(player);
			    else                                              randomMove(player); // player is dead. :(
		    }
		    if (time == 2){
			    // if Bullet Direction is Normal to Player Movement Direction.
			    if (canGo(player, player.getMovementDirection()) && tBullet.direction != player.getMovementDirection()
					    && tBullet.direction != TiZiiUtils.getReverseDirection(player.getMovementDirection())) {
					move(player); return;
			    }
				// else turn player to a direction that he can go and its Normal to Bullet Direction.
			    for (Direction direction : Direction.values()){
				    if (canGo(player, direction) && tBullet.direction != direction
						    && tBullet.direction != TiZiiUtils.getReverseDirection(direction)) {
					    move(player); return;
				    }
			    }
			    randomMove(player);
			    // else player is dead :(
		    }
	    }
    }

	/**
	 * Just Discover.
	 * @param player that must Discover.
	 */
	private void discoveryMove(Player player){
		// Going forward is the best discovery move.
		if (staticsInfo.isSecondAheadCellsUnDiscovered(player)
				&& canGo(player, player.getMovementDirection())
				&& TiZiiUtils.getRandomNumber(90) < 85 ){
			move(player); return;
		}

		/**
		 * You, me, or nobody is gonna hit as hard as life.
		 * But it ain't about how hard ya hit. It's about how hard you can get hit and keep moving forward.
		 * How much you can take and keep moving forward. That's how winning is done!” ― Rocky Balboa
		 */

		TiZiiCoords u = new TiZiiCoords(player.getCell());
		TiZiiCoords assignedTarget = staticsInfo.assignedPlayerToDiscoveryTarget.get(player.getId());
		if (assignedTarget != null && isTargetValid(player, assignedTarget)){
			int id = staticsInfo.discoveryCoordsToId.get(assignedTarget);
			DistanceDirectionPair pair = staticsInfo.discoveryBFSTable[u.i][u.j].get(id);
			if (pair != null) moveOrRotate(player, pair.direction); // TODO: Null Pointer Exception.
			else              randomMove(player);                   // TODO: This maybe fixed doe to considering neighboring cells.
		} else { // assign new target.
			Integer minDist = (int) 1e8;
			TiZiiCoords assignedCoords = null;
			TiZiiCoords playerCoords = new TiZiiCoords(player.getCell());
			for (TiZiiCoords coords : staticsInfo.getCurEdgeCells()) {
				int i = coords.i, j = coords.j;
				TiZiiCoords thisCoords = new TiZiiCoords(i, j);
				if (staticsInfo.discoveredAreas[i][j] > 0) continue;
				if (staticsInfo.assignedDiscoveryTargetToPlayer.containsKey(thisCoords)) continue;

				if (TiZiiUtils.getRandomNumber(100) > 85) continue;
				if (minDist > TiZiiUtils.manhattanDistance(playerCoords, thisCoords)
						&& TiZiiUtils.canReach(thisCoords, playerCoords)) {
					minDist = TiZiiUtils.manhattanDistance(playerCoords, thisCoords);
					assignedCoords = thisCoords;
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

				if (alliesInfo.idlePlayers.contains(player.getId()))
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
	 * Because Clearing Discovered Areas Function Does'nt Work Correctly I Added This to Remove Discovered Targets.
	 * @param assignedTarget Target That must be validated.
	 * @return true if target is Already Discovered.
	 */
	private boolean isTargetValid(Player player, TiZiiCoords assignedTarget){
		TiZiiCoords playerCoords = new TiZiiCoords(player.getCell());
		Integer targetId = staticsInfo.discoveryCoordsToId.get(assignedTarget);
		if ((targetId == null || !staticsInfo.discoveryBFSTable[playerCoords.i][playerCoords.j].containsKey(targetId))
				&& staticsInfo.discoveredAreas[assignedTarget.i][assignedTarget.j] != StaticsInfo.Consts.UNSEEN){
			staticsInfo.clearDiscoveryTarget(assignedTarget); return false;
		}
		return true;
	}

	/**
	 * Move Player To a Direction With Highest Score. (Includes Some Randomness).
	 * @param player that needs to move.
	 * @param scorePairs Array DirectionScorePair that Contain Score for Each Player.
	 */
    private void scoreMove(Player player, DirectionScorePair[] scorePairs) {
        Arrays.sort(scorePairs);

        boolean isFirst = true;
        int rand = TiZiiUtils.getRandomNumber(100);
        for (DirectionScorePair pair : scorePairs) {
            if (isFirst && rand > 85){ isFirst = false; continue; } isFirst = false; // Skip Best Score sometimes.
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
	        alliesInfo.nextPositions.add(new TiZiiCoords(player.getCell().getAdjacentCell(direction)));
            alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell().getAdjacentCell(direction)));
        } else if (player.getMovementDirection().equals(direction) && !canGo(player, direction)){
	        alliesInfo.collidedPlayer.add(player.getId()); // TODO: maybe must check that player next position in some other player next position else remove the next position tree set.
			randomMove(player);
        } else {
            rotate(player, direction);
	        alliesInfo.nextPositions.add(new TiZiiCoords(player.getCell()));
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
	    Cell aheadCell = player.getCell().getAdjacentCell(player.getMovementDirection());
        if (rand <= 60 && canGo(player, player.getMovementDirection())) {
            move(player);
	        TiZiiCoords nextCoords = new TiZiiCoords(aheadCell);
	        alliesInfo.blockedCoords.add(nextCoords);
        } else if (rand <= 85){
	        rand = TiZiiUtils.getRandomNumber(4);
	        Direction[] directions = Direction.values();

	        for (int i = 0; i < 4; i++) {
		        Direction dir = directions[(rand + i) % 4];
		        if (canGo(player, dir) && !dir.equals(player.getMovementDirection())) {
			        alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
			        rotate(player, dir); break;
		        }
	        }
        } // else don't move.
    }

	/**
	 * Move Player Randomly and avoid given direction.
	 * @param player target player.
	 * @param avoidDirection target direction.
	 * @return true if player moves.
	 */
	private void randomMoveAndAvoidDirectionAndGold(Player player, Direction avoidDirection) {
		int rand = TiZiiUtils.getRandomNumber(100);
		Cell aheadCell = player.getCell().getAdjacentCell(player.getMovementDirection());
		if (rand <= 60 && canGo(player, player.getMovementDirection())
				&& !player.getMovementDirection().equals(avoidDirection)
				&& !(staticsInfo.isGoldCell(aheadCell) && !(player instanceof GoldMiner))) {
			TiZiiCoords nextCoords = new TiZiiCoords(aheadCell);
			alliesInfo.blockedCoords.add(nextCoords);
			move(player);
		} else if (rand <= 85){
			rand = TiZiiUtils.getRandomNumber(4);
			Direction[] directions = Direction.values();

			for (int i = 0; i < 4; i++) {
				Direction dir = directions[(rand + i) % 4];
				if (canGo(player, dir) && !dir.equals(player.getMovementDirection()) && !dir.equals(avoidDirection)
						&& !(staticsInfo.isGoldCell(aheadCell) && !(player instanceof GoldMiner))) {
					alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
					rotate(player, dir); move(player);
				}
			}
		} else randomMove(player);
	}
}
