package common.net.commands;

import java.util.HashMap;

import common.board.Direction;
import common.net.Command;
import common.player.Player;
import common.player.Spy;

public class ClientToServerPack extends Command {

    public static final String kFire = "fire";
    public static final String kMove = "move";
    public static final String kHide = "hide";
    public static final String kShow = "show";

//	public HashMap<Integer, Integer> firingPlayers; // ID --> Direction.ordinal() 
//	public HashMap<Integer, Integer> movingPlayers; // ID --> Direction.ordinal()
//	public ArrayList<Integer> miningPlayers;
    public HashMap<Integer, String> commands;

    public ClientToServerPack() {
        super(kClientToServerCommands);
//		firingPlayers = new HashMap<Integer, Integer>();
//		movingPlayers = new HashMap<Integer, Integer>();
//		miningPlayers = new ArrayList<Integer>();
        commands = new HashMap<Integer, String>();
    }

    public void rotate(Player p, Direction d) {
        rotate(p.getId(), d);
    }

    public void rotate(int pid, Direction d) {
        if (d != Direction.NONE) {
            rotate(pid, d.ordinal());
        }
    }

    public void rotate(int pid, int direction) {
        //firingPlayers.put(pid, direction);
        commands.put(pid, String.valueOf(direction));
    }
    /*
     public void move(Player p, Direction d) {
     move(p.getId(), d);
     }
	
     public void move(int pid, Direction d) {
     move(pid, d.ordinal());
     }
     */

    public void move(Player p) {
        move(p.getId());
    }

    public void move(int pid/*, int direction*/) {
//		movingPlayers.put(pid, direction);
        commands.put(pid, kMove);
    }

    public void setVisible(Player p, boolean state) {
        setVisible(p.getId(), state);
    }

    public void setVisible(int pid, boolean state) {
        commands.put(pid, state ? kShow : kHide);
    }

    public void fire(Player p) {
        fire(p.getId());
    }

    public void fire(int pid) {
        commands.put(pid, kFire);
    }

    /*
     public void mine(Player p) {
     mine(p.getId());
     }
	
     public void mine(int pid) {
     if (!miningPlayers.contains(pid)) {
     miningPlayers.add(pid);
     }
     }
	
     */
    /*
     public HashMap<Integer, Integer> getFiringPlayers() {
     return firingPlayers;
     }

     public HashMap<Integer, Integer> getMovingPlayers() {
     return movingPlayers;
     }
	
     public ArrayList<Integer> getMiningPlayers() {
     return miningPlayers;
     }
     */
    public HashMap<Integer, String> getCommands() {
        return commands;
    }
}
