package client.java.teamclient.TiZiiClasses;

import common.board.Cell;
import common.board.Direction;
import common.player.Bullet;

/**
 * Created by iman on 12/8/15.
 *
 */
public class TiZiiBullet implements Comparable<TiZiiBullet> {
	public int id;
	public Cell cell;
	public Direction direction;

	public TiZiiBullet(Bullet bullet) {
		this.id = bullet.getId();
		this.cell = bullet.getCell();
		this.direction = bullet.getMovementDirection();
	}

	@Override
	public int compareTo(TiZiiBullet o) {
		return Integer.compare(id, o.id);
	}

	@Override
	public String toString() {
		return direction.toString().substring(0,1);
	}
}
