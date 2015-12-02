package client.java.communication;

import java.util.ArrayList;

import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.constant.Config;
import common.player.Bullet;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;

/**
 * This class contains game instance for each client. It's an abstract class and
 * each team has to implement the step() method.
 */
public abstract class ClientGame {

    private Client mNetworkController;
    
    private Board board;
    private ArrayList<Bullet> bulletsOnMap;
    private int cycleNumber;

    private int myScore;
    private int opponentScore;
    
    private ArrayList<GoldMiner> goldMiners = new ArrayList<GoldMiner>();
    private ArrayList<Hunter> hunters = new ArrayList<Hunter>();
    private ArrayList<Spy> spies = new ArrayList<Spy>();
    private ArrayList<GoldMiner> opponentGoldMiners = new ArrayList<GoldMiner>();
    private ArrayList<Hunter> opponentHunters = new ArrayList<Hunter>();
    private ArrayList<Spy> opponentSpies = new ArrayList<Spy>();

    public ClientGame() {
        board = new Board();
    }

    
    public Board getBoard() {
        return board;
    }

    protected final ArrayList<Hunter> getMyHunters() {
        return hunters;
    }

    protected final ArrayList<Spy> getMySpies() {
        return spies;
    }

    protected final ArrayList<GoldMiner> getMyGoldMiners() {
        return goldMiners;
    }

    protected final ArrayList<Player> getMyPlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        players.addAll(getMyGoldMiners());
        players.addAll(getMyHunters());
        players.addAll(getMySpies());
        
        return players;
    }    

    protected final ArrayList<Hunter> getOpponentHunters() {
        return opponentHunters;
    }    

    protected final ArrayList<GoldMiner> getOpponentGoldMiners() {
        return opponentGoldMiners;
    }
    
    protected final ArrayList<Spy> getOpponentSpies() {
        return opponentSpies;
    }

    protected final ArrayList<Player> getOpponentPlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        players.addAll(getOpponentGoldMiners());
        players.addAll(getOpponentHunters());
        players.addAll(getOpponentSpies());
        
        return players;
    }
    
    protected final int getCycleNumber() {
        return cycleNumber;
    }

    protected final int getMyScore() {
        return myScore;
    }

    protected final int getOpponentScore() {
        return opponentScore;
    }


    protected final void rotate(Player p, Direction d) {
        mNetworkController.rotate(p, d);
    }

    protected final void fire(Hunter p) {
        mNetworkController.fire(p);
    }

    protected final void move(Player p) {
        mNetworkController.move(p);
    }
    
    protected final void show(Spy spy) {
        mNetworkController.setVisibility(spy, false);
    }
    
    protected final void hide(Spy spy) {
        mNetworkController.setVisibility(spy, true);        
    }

    protected final ArrayList<Gold> getGolds() {
        return board.getGolds();
    }

    public final ArrayList<Bullet> getBullets() {
        return bulletsOnMap;
    }
    
    public abstract String getTeamName();    

    
    
    /**
     * @definition this method add new cells visited to client board!
     * @param cells
     */
    protected final void addCellsVisitedToBoard(ArrayList<Cell> cells) {
        for (Cell cell : cells) {
            board.addCellToBoard(cell);
        }
    }
    
    
    /**
     * This method is called at each cycle.
     */
    public abstract void step();

    /**
     * This method is called once in the beginning of the game. Should return a
     * Universally unique name that represents the team.
     */

    protected final void addNewGolds(ArrayList<Gold> golds) {
        board.addNewGolds(golds);
    }

    public final void setBulletsOnMap(ArrayList<Bullet> bulletsOnMap) {
        this.bulletsOnMap = bulletsOnMap;
    }

    public void setGoldMiners(ArrayList<GoldMiner> goldMiners) {
        this.goldMiners = goldMiners;
    }

    public void setHunters(ArrayList<Hunter> hunters) {
        this.hunters = hunters;
    }

    public void setOpponentGoldMiners(ArrayList<GoldMiner> opponentGoldMiners) {
        this.opponentGoldMiners = opponentGoldMiners;
    }

    public void setOpponentHunters(ArrayList<Hunter> opponentHunters) {
        this.opponentHunters = opponentHunters;
    }


    public void setSpies(ArrayList<Spy> spies) {
        this.spies = spies;
    }

    public void setOpponentSpies(ArrayList<Spy> opponentSpies) {
        this.opponentSpies = opponentSpies;
    }    

    public void setBoard(Board board) {
        this.board = board;
        Board.setInstance(board);
    }

    public void setCycleNumber(int cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public void setScore(int myScore) {
        this.myScore = myScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }


    protected final void setClientGame(Client networkController) {
        this.mNetworkController = networkController;
    }
    
    
    public void debug(int fromRow, int fromCol, int toRow, int toCol) {
        Board board = getBoard();

        for (int i = fromRow; i < toRow; i++) {
            for (int j = fromCol; j < toCol; j++) {
                Cell c = board.getCellAt(i, j);
                String view = "   ";
                if (c != null) {
                    view = "000";
                    if (c.getType().isBlock()) {
                        view = "###";
                    }
                    if (c.getPlayerInside() != null) {
                        Player player = c.getPlayerInside();
                        view = "" + player.getTeam().getId();
                        if (player instanceof Spy) {
                            view += "S";
                        } else if (player instanceof Hunter) {
                            view += "H";
                        } else {
                            view += "G";
                        }
                        
                        String direction = player.getMovementDirection() + "";
                        view = view + direction.substring(0, 1);
                    }
                }
                System.out.print(view + " ");
            }
            System.out.println("");
        }
    }
}
