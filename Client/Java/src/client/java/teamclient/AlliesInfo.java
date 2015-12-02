package client.java.teamclient;

import client.java.teamclient.TiZiiClasses.DirectionScorePair;
import client.java.teamclient.TiZiiClasses.StepsInDirection;
import common.board.Cell;
import common.board.Direction;
import common.player.Player;
import common.player.Spy;

import java.util.HashMap;

/**
 * Class name:   AlliesInfo
 * Date:         12/2/2015
 * Description:  Contains Info about my players.
 */
public class AlliesInfo {
    public EnemiesInfo enemiesInfo;
    public StaticsInfo staticsInfo;
    public HashMap<Integer, StepsInDirection> prevDirections;
    //public static HashMap<Integer, Integer> assignedToTarget; // TODO: Use This.

    public AlliesInfo(EnemiesInfo enemiesInfo, StaticsInfo staticsInfo) {
        this.enemiesInfo = enemiesInfo;
        this.staticsInfo = staticsInfo;
        this.prevDirections = new HashMap<>();
    }


    public DirectionScorePair[] getMovementScoresForSpy(Spy spy){
        DirectionScorePair[] pairs = new DirectionScorePair[4];
        for (int i=0 ; i<4; i++) pairs[i] = new DirectionScorePair(Direction.values()[i], 0);

        Direction forwardDir = spy.getMovementDirection();
        Direction backwardDir = Direction.values()[(TiZiiUtils.getDirectionID(forwardDir)+ 2)%4];
        StepsInDirection prevDir = prevDirections.get(spy.getId());

        Cell cell = spy.getCell();
        int ii = cell.getRowNumber();
        int jj = cell.getColumnNumber();

        for (DirectionScorePair pair : pairs){
            int di = pair.direction.getDeltaRow();
            int dj = pair.direction.getDeltaCol();

            int unSeenCount = 0;
            if (di == 0){
                for (int i=ii-1 ; i>=ii-Consts.WING_LENGTH ; i--){
                    if (!TiZiiUtils.inRange(i,jj)) break;
                    if (staticsInfo.mBoard[i][jj] == StaticsInfo.Consts.BLOCK) break;
                    for (int j=jj+dj ; true ; j+=dj){
                        if (!TiZiiUtils.inRange(i, j)) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.UNSEEN) unSeenCount++;
                    }
                }
                for (int i=ii ; i<=ii+Consts.WING_LENGTH ; i++){
                    if (!TiZiiUtils.inRange(i,jj)) break;
                    if (staticsInfo.mBoard[i][jj] == StaticsInfo.Consts.BLOCK) break;
                    for (int j=jj+dj ; true  ; j+=dj){
                        if (!TiZiiUtils.inRange(i, j)) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.UNSEEN) unSeenCount++;
                    }
                }
            }
            if (dj == 0){
                for (int j=jj-1 ; j>=jj-Consts.WING_LENGTH ; j--){
                    if (!TiZiiUtils.inRange(ii, j)) break;
                    if (staticsInfo.mBoard[ii][j] == StaticsInfo.Consts.BLOCK) break;
                    for (int i=ii+di ; TiZiiUtils.inRange(i,jj) ; i+=di){
                        if (!TiZiiUtils.inRange(i, j)) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.UNSEEN) unSeenCount++;
                    }
                }
                for (int j=jj ; j<=jj+Consts.WING_LENGTH ; j++){
                    if (!TiZiiUtils.inRange(ii, j)) break;
                    if (staticsInfo.mBoard[ii][j] == StaticsInfo.Consts.BLOCK) break;
                    for (int i=ii+di ; TiZiiUtils.inRange(i,jj) ; i+=di){
                        if (!TiZiiUtils.inRange(i, j)) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.BLOCK) break;
                        if (staticsInfo.mBoard[i][j] == StaticsInfo.Consts.UNSEEN) unSeenCount++;
                    }
                }
            }

            pair.score += unSeenCount*Consts.UNSEEN_CELL_SCORE;

            int iii = ii+di, jjj = jj+dj;
            if (!TiZiiUtils.inRange(iii,jjj) || cell.getAdjacentCell(pair.direction).getPlayerInside() != null
                    || staticsInfo.mBoard[iii][jjj] == StaticsInfo.Consts.BLOCK) pair.score += Consts.BLOCK_SCORE;


            if (prevDir != null && prevDir.direction == pair.direction){
                prevDir.steps++; //TODO: Check this in debug that updates reference in map
                pair.score += Consts.FORWARD_SCORE * prevDir.steps;
            }

            if (backwardDir == pair.direction) pair.score += Consts.BACKWARD_SCORE;
        }
        return pairs;
    }



    public static class Consts {
        public static final int BLOCK_SCORE = -1000;
        public static final int FORWARD_SCORE = 10;
        public static final int BACKWARD_SCORE  = -500;
        public static final int UNSEEN_CELL_SCORE = 10;

        public static final int WING_LENGTH = 2;
    }
}
