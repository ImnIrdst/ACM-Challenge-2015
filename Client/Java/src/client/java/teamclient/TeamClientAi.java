package client.java.teamclient;

import client.java.communication.ClientGame;
import client.java.teamclient.TiZiiClasses.*;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;

import java.util.*;

/**
 *  Class name:     TeamClientAi
 *  Date:           12/2/2015
 */

// TODO: pay attention that all ifs must have else. (Checked)

public class TeamClientAi extends ClientGame {
    public String getTeamName() { return "TiZii"; }

    // Class Members.
    StaticsInfo staticsInfo;                    // Contains info about static object in the map.
    EnemiesInfo enemiesInfo;                    // Contains info about enemy players.
    AlliesInfo  alliesInfo;                     // Contains info about my players.
    public boolean firstTimeInitialize = false;         // Shows that global variables are Initialize or not.

    /**
     * Runs in each Cycle (main Function).
     */
    public void step() {
	    try {

	    long startTime = System.currentTimeMillis();
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



        // updating.
	    alliesInfo.updateAlliesInfo(getMyPlayers());
	    staticsInfo.updateStaticBoard(getMyPlayers(), getBoard().getCells());
	    enemiesInfo.updateEnemyBoard(getOpponentHunters(), getOpponentGoldMiners(), getOpponentSpies());
		enemiesInfo.updateBulletHitTimes(getBullets());
	    enemiesInfo.updateBlockedCells(getOpponentHunters());

	    staticsInfo.updateGoldInfo(getGolds(), getMyPlayers(), getBoard().getCells());
		enemiesInfo.updateHuntingTargets(getOpponentHunters());
	    staticsInfo.updateDiscoveryInfo(getMyPlayers());
	    TiZiiUtils.update(this);

        // logging
        if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
	        System.out.println(staticsInfo);

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
	        System.out.println(enemiesInfo);

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
	        TiZiiUtils.printBoard(getBoard().getCells(), "Not Null Cells");

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
	        TiZiiUtils.printBoard(staticsInfo.goldBFSTable, "Gold BFS TABLE");

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
		    TiZiiUtils.printGoldBfsDirections(staticsInfo.discoveryBFSTable, staticsInfo.discoveryIdToCoords, "Discovery BFS Directions");

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
		    TiZiiUtils.printGoldBfsDirections(staticsInfo.goldBFSTable, staticsInfo.goldIdToCoordMap, "Gold BFS Directions");

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)
	        TiZiiUtils.printBoard(staticsInfo.discoveredAreas, "Discovered Areas.");

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.notNeeded)
		    TiZiiUtils.printBulletsHitTime("Bullets Hit Time");


        // Game Logic.
	    for (Hunter hunter : hunters)       hunterLogic(hunter);    // Must Come Before Miners and Spies
	    for (GoldMiner miner : miners)      minerLogic(miner);
        for (Spy spy : spies)               spyLogic(spy);


	    // log targets.
	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed) {
		    for (Hunter hunter : hunters) {

			    enemiesInfo.hunterAssignedToTarget.containsKey(hunter.getId());
			    System.out.print("Hunter " + new TiZiiCoords(hunter.getCell()) + ":\t\t");
			    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(hunter.getId())) {
				    System.out.println(staticsInfo.assignedPlayerToDiscoveryTarget.get(hunter.getId()) + " Discovery");
			    }else if (enemiesInfo.hunterAssignedToTarget.containsKey(hunter.getId())) {
				    System.out.println(enemiesInfo.hunterAssignedToTarget.get(hunter.getId()) + " Hunt");
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
		    System.out.println("Cycle: " + getCycleNumber() + ", TiZii: " + getMyScore() + ", Opponent: " + getOpponentScore());
		    long finishTime = System.currentTimeMillis(); // First Cycle 43 ms, others 3 ms
		    System.out.println("Time in Millis: " + (finishTime - startTime));
	    }


	    } catch (Exception e){
		    System.out.println("Just Keep Moving Forward!");
		    e.printStackTrace();
	    }
    }

    /**
     * Initializes member variables.
     * Runs only in first Cycle.
     */
    private void initialize(){
	    if (!firstTimeInitialize) {
            staticsInfo = new StaticsInfo(this);
            enemiesInfo = new EnemiesInfo(this);
            alliesInfo = new AlliesInfo(this);

	        staticsInfo.alliesInfo = alliesInfo;
	        staticsInfo.enemiesInfo = enemiesInfo;

	        enemiesInfo.alliesInfo = alliesInfo;
	        enemiesInfo.staticsInfo = staticsInfo;

	        alliesInfo.staticsInfo = staticsInfo;
	        alliesInfo.enemiesInfo = enemiesInfo;

            TiZiiUtils.rows = getBoard().getNumberOfRows();
            TiZiiUtils.cols = getBoard().getNumberOfColumns();
	        TiZiiUtils.update(this);
            System.out.println("Initialized :D");
		    System.out.println("Final Version.");
        }
        firstTimeInitialize = true;
    }

    /**
     * Defines how a miner should act.
     * Runs per each miner.
     */
    private void minerLogic(GoldMiner miner) {
	    boolean moved = highPriorityMoves(miner); if (moved) return;

        Gold gold = getBoard().getGold(miner.getCell());
        if (gold == null) {
            TiZiiCoords u = new TiZiiCoords(miner.getCell());

            // if miner assigned to a gold and gold exists go there.
            Integer assignedGold = alliesInfo.assignedPlayerToGold.get(miner.getId());
            if (assignedGold != null && staticsInfo.goldIdToCoordMap.containsKey(assignedGold)){
                DistanceDirectionPair pair = staticsInfo.goldBFSTable[u.i][u.j].get(assignedGold);
	            if (pair != null) moveOrRotate(miner, pair.direction);
	            else              randomMove(miner);
	            return;
            } // else pick the not assigned nearest target.
            discoveryMove(miner);
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
		    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(hunter.getId()))
		        staticsInfo.removeFromValidDiscoveryCoords(staticsInfo.assignedPlayerToDiscoveryTarget.get(hunter.getId()));
		    randomMoveAndAvoidDirectionAndGold(hunter, avoidDirection); return;
	    }

	    Integer burstFireNumber = alliesInfo.hunterBurstFire.get(hunter.getId());
	    if (burstFireNumber != null){
		    alliesInfo.hunterBurstFire.put(hunter.getId(), burstFireNumber - 1);
		    if (burstFireNumber == 0) alliesInfo.hunterBurstFire.remove(hunter.getId());
	    }

        if (enemiesInfo.isEnemyAhead(hunter) && hunter.canAttack()
		        && alliesInfo.noAlliesInsight(hunter)
		        && (burstFireNumber == null || burstFireNumber > AlliesInfo.Consts.BURST_QTY)) {// can kill someone
	        // burst fire condition.
	        if (burstFireNumber == null){
		        alliesInfo.hunterBurstFire.put(hunter.getId(), AlliesInfo.Consts.BURST_QTY * 2 - 1);
	        }

	        for (TiZiiCoords coords = new TiZiiCoords(hunter.getCell()) ;
	            TiZiiUtils.inRange(coords.i, coords.j) ;
	             coords = coords.adjacent(hunter.getMovementDirection()) ){
		        alliesInfo.blockedCoords.add(coords);
	        }

	        fire(hunter); return;
        }
		boolean rotated = turnTowardEnemy(hunter); if (rotated) return;

	    discoveryMove(hunter);
    }

    /**
     * Defines how a spy should act.
     * Runs per each spy.
     */
    private void spyLogic(Spy spy) {
	    boolean moved = highPriorityMoves(spy); if (moved) return;

	    Direction avoidDirection = alliesInfo.isGoldOnSightAndMinerNearby(spy);
	    if (avoidDirection != null) {
		    if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(spy.getId()))
		        staticsInfo.removeFromValidDiscoveryCoords(staticsInfo.assignedPlayerToDiscoveryTarget.get(spy.getId()));
		    randomMoveAndAvoidDirectionAndGold(spy, avoidDirection); return;
	    }

	    if ((enemiesInfo.isEnemyHunterNearby(spy.getCell()) || TiZiiUtils.getRandomNumber(100) > 85)
			    && !spy.isHidden() && getCycleNumber() > TiZiiUtils.Consts.HIDING_MOMENT && spy.getHidenessCapacity() > 0) {
	        show(spy); return; // TODO: Bug! Must Be Changed To Hide.
        }
        if ((!enemiesInfo.isEnemyHunterNearby(spy.getCell()) || TiZiiUtils.getRandomNumber(100) > 85)
			    && spy.isHidden() && getCycleNumber() < TiZiiUtils.Consts.HIDING_MOMENT){
	        hide(spy); return; // TODO: Bug! Must Be Changed to Show
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

//		if (alliesInfo.prevRotatedPlayers.contains(player.getId())
//				&& canGo(player, player.getMovementDirection())){
//			move(player); return true;
//		}
//		// if player avoided collision in prev cycle.
//		if (alliesInfo.collidedPlayer.containsKey(player.getId())
//				&& canGo(player, player.getMovementDirection())
//				&& !(player instanceof GoldMiner) && (TiZiiUtils.getRandomNumber(86) < 85)){
//			StepsInDirection pair = alliesInfo.collidedPlayer.get(player.getId());
//
//			pair.steps--;
//			if (pair.steps == 0) alliesInfo.collidedPlayer.remove(player.getId());
//			else alliesInfo.collidedPlayer.put(player.getId(), pair);
//			if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(player.getId())) {
//				staticsInfo.removeFromValidDiscoveryCoords(staticsInfo.assignedPlayerToDiscoveryTarget.get(player.getId()));
//			}
//			move(player); return true;
//		}
//		if (alliesInfo.collidedPlayer.containsKey(player.getId())
//				&& !canGo(player, player.getMovementDirection())
//				&& !(player instanceof GoldMiner) && (TiZiiUtils.getRandomNumber(86) < 85)){
//			StepsInDirection pair = alliesInfo.collidedPlayer.get(player.getId());
//
//			pair.steps--;
//			if (pair.steps == 0) alliesInfo.collidedPlayer.remove(player.getId());
//			else alliesInfo.collidedPlayer.put(player.getId(), pair);
//			if (staticsInfo.assignedPlayerToDiscoveryTarget.containsKey(player.getId())) {
//				staticsInfo.removeFromValidDiscoveryCoords(staticsInfo.assignedPlayerToDiscoveryTarget.get(player.getId()));
//			}
//			randomMoveAndAvoidDirectionAndGold(player, pair.direction); return true;
//		}
		// if player is a miner don't use randomness.
		if (player instanceof GoldMiner) return false;

		// try some randomness
		if (TiZiiUtils.getRandomNumber(90) > 85){ randomMove(player); return true; }

		return false;
	}

	/**just avoid bullets.
	 * @param player player that must avoid bullets
	 */
    private void avoidBullets(Player player){

	    if (TiZiiUtils.isLoggingEnabled && TiZiiUtils.needed)
		    System.out.println("avoid Bullets " + player.getCell());


	    TiZiiCoords tCoords = new TiZiiCoords(player.getCell());
	    TreeMap<TiZiiBullet, Integer> bulletHitTimeIJ = enemiesInfo.bulletHitTime[tCoords.i][tCoords.j];

	    for (TiZiiBullet tBullet : bulletHitTimeIJ.keySet()){
			Integer time = bulletHitTimeIJ.get(tBullet);
		    if (time == 1 && tBullet.direction != player.getMovementDirection()
				    && tBullet.direction != TiZiiUtils.getReverseDirection(player.getMovementDirection())){

			    if (canGo(player, player.getMovementDirection())) move(player);
			    else                                              discoveryMove(player); // player is dead. :(

			    return;
		    }
		    if (time == 2){
			    // if Bullet Direction is Normal to Player Movement Direction.
			    if (canGo(player, player.getMovementDirection()) && tBullet.direction != player.getMovementDirection()
					    && tBullet.direction != TiZiiUtils.getReverseDirection(player.getMovementDirection())) {
					move(player); return;
			    }
				// else turn player to a direction that he can go and its Normal to Bullet Direction.
			    for (Direction direction : Direction.values()){
				    if (direction == Direction.NONE) continue;
				    if (canGo(player, direction) && tBullet.direction != direction
						    && tBullet.direction != TiZiiUtils.getReverseDirection(direction)) {
					    moveOrRotate(player, direction); return;
				    }
			    }
			    discoveryMove(player);
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
		if (staticsInfo.isSecondAheadCellUndiscovered(player)
				&& canGo(player, player.getMovementDirection())
				&&TiZiiUtils.getRandomNumber(88) < 85){
			move(player); return;
		}

		/**
		 * Let me tell you something you already know. The world ain't all sunshine and rainbows.
		 * It's a very mean and nasty place and I don't care how tough you are it will beat you to
		 * your knees and keep you there permanently if you let it. You, me, or nobody is gonna hit as hard as life.
		 * But it ain't about how hard ya hit. It's about how hard you can get hit and keep moving forward.
		 * How much you can take and keep moving forward. That's how winning is done! ― Rocky Balboa
		 */

		TiZiiCoords u = new TiZiiCoords(player.getCell());
		TiZiiCoords assignedTarget = staticsInfo.assignedPlayerToDiscoveryTarget.get(player.getId());

		if (assignedTarget != null ) {
			int id = staticsInfo.discoveryCoordsToId.get(assignedTarget);
			DistanceDirectionPair pair = staticsInfo.discoveryBFSTable[u.i][u.j].get(id);

			if (pair != null) moveOrRotate(player, pair.direction);
			else randomMove(player);

			return;
		}
		DirectionScorePair[] scorePair = alliesInfo.getDiscoveryMovementScores(player);
		Arrays.sort(scorePair);
		scoreMove(player, scorePair);
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

	private boolean turnTowardEnemy(Hunter hunter){
		for (Direction dir : Direction.values()){
			if (dir.equals(hunter.getMovementDirection()) || dir.equals(Direction.NONE)) continue;
			for (TiZiiCoords thisCoords = new TiZiiCoords(hunter.getCell()) ;
					TiZiiUtils.inRange(thisCoords.i, thisCoords.j)
							&& staticsInfo.mBoard[thisCoords.i][thisCoords.j] != StaticsInfo.Consts.BLOCK;
					thisCoords = thisCoords.adjacent(dir)){
					if (enemiesInfo.mBoard[thisCoords.i][thisCoords.j] >= EnemiesInfo.Consts.PLAYER
							&& canGo(hunter, dir)){
						rotate(hunter, dir); return true;
					}
			}
		}
		return false;
	}


    private void moveOrRotate(Player player, Direction direction) {
        if (player.getMovementDirection().equals(direction) && canGo(player, player.getMovementDirection())) {
	        alliesInfo.nextPositions.add(new TiZiiCoords(player.getCell().getAdjacentCell(direction)));
	        alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell().getAdjacentCell(direction)));
	        move(player); return;
        }

	    if (player.getMovementDirection().equals(direction) && !canGo(player, player.getMovementDirection())){
//	        alliesInfo.collidedPlayer.put(player.getId(), new StepsInDirection(2, player.getMovementDirection()));
			randomMove(player); return;
        }

	    if (canGo(player, direction)){
		    alliesInfo.nextPositions.add(new TiZiiCoords(player.getCell()));
		    alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
		    if (canGo(player, player.getMovementDirection()))
		        alliesInfo.curRotatedPlayers.add(player.getId());
		    rotate(player, direction); return;
        }
	    randomMove(player);
    }

    private boolean canGo(Player player, Direction dir) {
	    TiZiiCoords curCell = new TiZiiCoords(player.getCell());
	    TiZiiCoords nextCell = curCell.adjacent(dir);
        return TiZiiUtils.inRange(nextCell.i, nextCell.j)
                && enemiesInfo.mBoard[nextCell.i][nextCell.j] != EnemiesInfo.Consts.HUNTER
		        && enemiesInfo.mBoard[nextCell.i][nextCell.j] != EnemiesInfo.Consts.MINER
		        && enemiesInfo.mBoard[nextCell.i][nextCell.j] != EnemiesInfo.Consts.SPY
		        && staticsInfo.mBoard[nextCell.i][nextCell.j] != StaticsInfo.Consts.BLOCK
                && !alliesInfo.blockedCoords.contains(nextCell);
    }

    private void randomMove(Player player){
        int rand = TiZiiUtils.getRandomNumber(100);
	    Cell aheadCell = player.getCell().getAdjacentCell(player.getMovementDirection());
        if (rand <= 60 && canGo(player, player.getMovementDirection())) {
	        TiZiiCoords nextCoords = new TiZiiCoords(aheadCell);
	        alliesInfo.blockedCoords.add(nextCoords);
	        move(player); return;
        }
        randomRotate(player);
    }

	private void randomRotate(Player player){
		Integer rand = TiZiiUtils.getRandomNumber(4);
		Direction[] directions = Direction.values();

		for (int i = 0; i < 4; i++) {
			Direction dir = directions[(rand + i) % 4];
			if (dir == Direction.NONE) continue;
			if (canGo(player, dir) && !dir.equals(player.getMovementDirection())) {
				alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
				if (canGo(player, player.getMovementDirection()))
					alliesInfo.curRotatedPlayers.add(player.getId());
				rotate(player, dir); return;
			}
		}
	}

	private void rotateCW(Player player){
		int offset = TiZiiUtils.getDirectionID(player.getMovementDirection()) + 1;

		Direction[] directions = Direction.values();
		for (int i = 0; i < 4; i++) {
			Direction dir = directions[(offset + i) % 4];
			if (canGo(player, dir) && !dir.equals(player.getMovementDirection())) {
				alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
				if (canGo(player, player.getMovementDirection()))
					alliesInfo.curRotatedPlayers.add(player.getId());
				rotate(player, dir); return;
			}
		}
	}

	/**
	 * Move Player Randomly and avoid given direction.
	 * @param player target player.
	 * @param avoidDirection target direction.
	 */
	private void randomMoveAndAvoidDirectionAndGold(Player player, Direction avoidDirection) {
		int rand = TiZiiUtils.getRandomNumber(100);
		Cell aheadCell = player.getCell().getAdjacentCell(player.getMovementDirection());
		if (rand <= 60 && canGo(player, player.getMovementDirection())
				&& !player.getMovementDirection().equals(avoidDirection) && aheadCell != null
				&& !(staticsInfo.isGoldCell(aheadCell) && !(player instanceof GoldMiner))) {
			TiZiiCoords nextCoords = new TiZiiCoords(aheadCell);
			alliesInfo.blockedCoords.add(nextCoords);
			move(player); return;
		}
		if (rand <= 85){
			rand = TiZiiUtils.getRandomNumber(4);
			Direction[] directions = Direction.values();

			for (int i = 0; i < 4; i++) {
				Direction dir = directions[(rand + i) % 4];
				if (canGo(player, dir) && !dir.equals(player.getMovementDirection()) && !dir.equals(avoidDirection)
						&& aheadCell != null && !(staticsInfo.isGoldCell(aheadCell) && !(player instanceof GoldMiner))) {
					alliesInfo.blockedCoords.add(new TiZiiCoords(player.getCell()));
					alliesInfo.curRotatedPlayers.add(player.getId());
					rotate(player, dir); return;
				}
			}
		}
		randomMove(player);
	}
}


// Trash Can


//	    added = new TreeSet<>();
//	    ArrayList<Player> players = new ArrayList<>();
//	    for (Player player : getMyPlayers()) {
//		    if (!added.contains(player.getId())){
//			    players.add(player); added.add(player.getId());
//		    }
//	    }
//	    Collections.shuffle(players);

/**
 * Because Clearing Discovered Areas Function Does'nt Work Correctly I Added This to Remove Discovered Targets.
 * @param assignedTarget Target That must be validated.
 * @return true if target is Already Discovered.
 */
//	private boolean isTargetValid(Player player, TiZiiCoords assignedTarget){
//		TiZiiCoords playerCoords = new TiZiiCoords(player.getCell());
//		Integer targetId = staticsInfo.discoveryCoordsToId.get(assignedTarget);
//		if ((targetId == null || !staticsInfo.discoveryBFSTable[playerCoords.i][playerCoords.j].containsKey(targetId))
//				&& staticsInfo.discoveredAreas[assignedTarget.i][assignedTarget.j] != StaticsInfo.Consts.UNSEEN){
//			staticsInfo.clearDiscoveryTarget(assignedTarget); return false;
//		}
//		return true;
//	}