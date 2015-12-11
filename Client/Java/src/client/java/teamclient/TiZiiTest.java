package client.java.teamclient;

import common.board.Direction;

/**
 * Created by iman on 12/8/15.
 *
 */
public class TiZiiTest {

	public static void testTiZiaUtilsDirections(){
		for (Direction direction : Direction.values()){
			//System.out.println(direction == TiZiiUtils.getReverseDirection(TiZiiUtils.getReverseDirection(direction)));
			System.out.println("Cur Direction: " + direction);
			System.out.println("Rev Direction: " + TiZiiUtils.getReverseDirection(direction));
			System.out.println("PosNormal Direction: " + TiZiiUtils.getReversePositiveNormalDirection(direction));
			System.out.println("NegNormal Direction: " + TiZiiUtils.getReverseNegativeNormalDirection(direction));
			System.out.println();
		}
	}
	public static void main(String[] args){

		testTiZiaUtilsDirections();
	}
}
