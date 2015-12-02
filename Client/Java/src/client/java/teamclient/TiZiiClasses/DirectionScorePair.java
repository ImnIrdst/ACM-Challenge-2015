package client.java.teamclient.TiZiiClasses;

import common.board.Direction;

/**
 * Class:           TiZiiClasses
 * Date:            12/2/2015
 * Description:     Pairs a Direction with a Score.
 */
public class DirectionScorePair implements Comparable<DirectionScorePair> {
    public Direction direction;
    public int score;

    public DirectionScorePair(Direction direction, int score) {
        this.direction = direction;
        this.score = score;
    }

    @Override
    public String toString() {
        return direction + " " + String.valueOf(score);
    }

    @Override
    public int compareTo(DirectionScorePair o) {
        return Integer.compare(o.score, this.score);
    }
}
