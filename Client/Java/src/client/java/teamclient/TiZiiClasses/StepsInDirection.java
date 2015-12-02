package client.java.teamclient.TiZiiClasses;

import common.board.Direction;

/**
 * Class:           TiZiiClasses
 * Date:            12/2/2015
 * Description:     Pairs a Direction with number of steps.
 */
public class StepsInDirection{
    public int steps;
    public Direction direction;

    public StepsInDirection(int steps, Direction direction) {
        this.steps = steps;
        this.direction = direction;
    }
}