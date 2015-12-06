package client.java.teamclient.TiZiiClasses;

import common.board.Direction;

/**
 * Created by iman on 12/3/15.
 *
 */
public class DistanceDirectionPair {
    public int distance;
    public Direction direction;

    public DistanceDirectionPair(int distance, Direction direction) {
        this.distance = distance;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return ("(" + direction.toString().charAt(0) + "," + String.valueOf(distance) + ")");
    }
}
