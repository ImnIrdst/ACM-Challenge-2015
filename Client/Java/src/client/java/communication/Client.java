package client.java.communication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.java.teamclient.TeamClientAi;
import common.board.Board;
import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.constant.Config;
import common.net.Command;
import common.net.NetworkManager;
import common.net.ReceiveListener;
import common.net.ReceiveListenerManager;
import common.net.commands.ClientToServerPack;
import common.net.commands.ServerToClientPack;
import common.player.Bullet;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;
import common.team.Team;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * This class contains communication with server and sending client AI.
 */
public class Client {

    private static Logger logger = Logger.getLogger("client.java.communication.Client");
    private LinkedBlockingDeque<Object> callStepController = new LinkedBlockingDeque<Object>();

    static void e(Exception e) {
        logger.log(Level.SEVERE, null, e);
    }

    static void l(String log) {
        logger.log(Level.INFO, log);
    }
    private NetworkManager mNetworkManager;
    private ReceiveListenerManager mListeners;
    private ClientGame mClient;
    protected Team mTeam;
    protected Team mEnemyTeam;
    private OutputStreamWriter mLogOutput;
    private OutputStreamWriter mLogInput;
    private ClientToServerPack mMessagePack;

    public Client(Socket socket) throws IOException {
        // mNetworkManager = NetworkManager.init(socket);
        mNetworkManager = new NetworkManager(socket);
        mListeners = new ReceiveListenerManager(mNetworkManager);
        mClient = new TeamClientAi();
        mTeam = new Team(mClient.getTeamName());
        mClient.setClientGame(this);

        String timeStamp = new SimpleDateFormat("MMddyy_hh:mm").format(new Date());
//        mLogOutput = new OutputStreamWriter(new FileOutputStream(new File("acmlog-" + mClient.getTeamName() + "out-" + timeStamp + ".log")));
//        mLogInput = new OutputStreamWriter(new FileOutputStream(new File("acmlog-" + mClient.getTeamName() + "in-" + timeStamp + ".log")));
        //mNetworkManager.setLogger(mLogOutput, mLogInput);

        initListeners();
        callingStep();
        getUniqueID(mTeam);
        getMapInfo(mTeam);
        mEnemyTeam = new Team("Enemy");
        mNetworkManager.startNetworking();
        //mListeners.startListening();
    }

    public final void callingStep() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        callStepController.take();
                        callStepController.clear();
                        nextCycle();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();
    }

    public void nextCycle() {
        mMessagePack = new ClientToServerPack();
        mClient.step();
        if (mMessagePack != null) {
            synchronized (mMessagePack) {
                mMessagePack.setTimeSent(mClient.getCycleNumber());
                mNetworkManager.send(mMessagePack);
            }
        }
    }

    public void fire(Player p) {
        mMessagePack.fire(p);
    }

    public void fire(int pid) {
        mMessagePack.fire(pid);
    }

    public void move(Player p) {
        mMessagePack.move(p);
    }

    public void move(int pid) {
        mMessagePack.move(pid);
    }

    public void setVisibility(Player p, boolean state) {
        mMessagePack.setVisible(p, state);
    }

    public void setVisibility(int pid, boolean state) {
        mMessagePack.setVisible(pid, state);
    }

    public void rotate(Player p, Direction d) {
        mMessagePack.rotate(p, d);
    }

    public void rotate(int pid, Direction d) {
        mMessagePack.rotate(pid, d);
    }

    public Team getmTeam() {
        return mTeam;
    }

    /**
     * Update game information of client with data received from server. Game
     * calls this before every step.
     *
     * @param cmd
     */
    public void updateClient(ServerToClientPack cmd) {

        Board board = mClient.getBoard();
        mClient.setBoard(new Board(board.getNumberOfRows(), board.getNumberOfColumns()));
        int cycle = cmd.cycle;
        int yourScore = cmd.yourScore;
        int opponentScore = cmd.opponentScore;

        ArrayList<Cell> updateCells = cmd.updateCells;
        ArrayList<GoldMiner> goldMiners = cmd.goldMiner;
        ArrayList<Hunter> hunters = cmd.hunter;
        ArrayList<GoldMiner> opponentGoldMiners = cmd.opponentGoldMiner;
        ArrayList<Hunter> opponentHunters = cmd.opponentHunter;
        ArrayList<Gold> newGolds = cmd.golds;
        ArrayList<Bullet> bullets = cmd.bullets;// TODO must implement it in network!
        ArrayList<Spy> spies = cmd.spies;
        ArrayList<Spy> opponentSpies = cmd.opponentSpies;

        for (Cell cell : updateCells) {
            mClient.getBoard().addCellToBoard(cell);
        }
        updateMyTeamGoldMiners(goldMiners);
        updateMyTeamHunters(hunters);
        updateMyTeamSpies(spies);

        updateEnemyTeamGoldMiners(opponentGoldMiners);
        updateEnemyTeamHunters(opponentHunters);
        updateEnemyTeamSpies(opponentSpies);

        updateBulletPosition(bullets);

        mClient.addNewGolds(newGolds);
        mClient.setHunters(hunters);
        mClient.setGoldMiners(goldMiners);
        mClient.setSpies(spies);
        mClient.setOpponentGoldMiners(opponentGoldMiners);
        mClient.setOpponentHunters(opponentHunters);
        mClient.setOpponentSpies(opponentSpies);
        mClient.setBulletsOnMap(bullets);
        mClient.setCycleNumber(cycle);
        mClient.setScore(yourScore);
        mClient.setOpponentScore(opponentScore);
    }

    private void updateEnemyTeamSpies(ArrayList<Spy> opponentSpies) {
        for (Player player : opponentSpies) {
            player.setTeam(mEnemyTeam);
        }
        updateSpyPosition(opponentSpies);
    }

    private void updateMyTeamSpies(ArrayList<Spy> spies) {
        for (Player player : spies) {
            player.setTeam(mTeam);
        }
        updateSpyPosition(spies);
    }

    private void updateMyTeamHunters(ArrayList<Hunter> players) {
        for (Player player : players) {
            player.setTeam(mTeam);
        }
        updateHunterPosition(players);
    }

    private void updateMyTeamGoldMiners(ArrayList<GoldMiner> players) {
        for (Player player : players) {
            player.setTeam(mTeam);
        }
        updateGoldMinerPosition(players);
    }

    private void updateEnemyTeamHunters(ArrayList<Hunter> players) {
        for (Player player : players) {
            player.setTeam(mEnemyTeam);
        }
        updateHunterPosition(players);
    }

    private void updateEnemyTeamGoldMiners(ArrayList<GoldMiner> players) {
        for (Player player : players) {
            player.setTeam(mEnemyTeam);
        }
        updateGoldMinerPosition(players);
    }

    private void updateHunterPosition(ArrayList<Hunter> players) {
        for (Player player : players) {
            updatePlayerPosition(player);
        }
    }

    private void updateSpyPosition(ArrayList<Spy> opponentSpies) {
        for (Player player : opponentSpies) {
            updatePlayerPosition(player);
        }
    }

    private void updateGoldMinerPosition(ArrayList<GoldMiner> players) {
        for (Player player : players) {
            updatePlayerPosition(player);
        }
    }

    private void updatePlayerPosition(Player player) {
        Cell cell = player.getCell();
        Cell boardCell = Board.getInstance().getCellAt(cell.getRowNumber(), cell.getColumnNumber());
        player.setCell(boardCell);
        boardCell.setPlayerInside(player);
    }

    /**
     * Asks server for a unique id.
     *
     * @param team
     */
    private synchronized void getUniqueID(Team team) {
        Command c = Command.create(Command.kRequestTeamID, new String[]{team.getName()});
        mNetworkManager.send(c);
    }

    private synchronized void getMapInfo(Team taem) {
        Command c = Command.create(Command.kMapInfo);
        c.setTimeSent(mClient.getCycleNumber());
        mNetworkManager.send(c);

    }

    /**
     * Initializes network listeners.
     *
     */
    private synchronized void initListeners() {
        mListeners.registerListener(Command.kRequestTeamID, new ReceiveListener() {
            @Override
            public void notify(final Command cmd,
                    final NetworkManager netManager) {
                int teamID = Integer.parseInt(cmd.getArguments()[0]);
                mTeam.setTeamID(teamID);
                mEnemyTeam.setTeamID(1 - teamID);
                l("got team id = " + teamID);
            }
        });

        mListeners.registerListener(Command.kMapInfo, new ReceiveListener() {
            @Override
            public void notify(final Command cmd,
                    final NetworkManager netManager) {
                int rowNumber = Integer.parseInt(cmd.getArguments()[0]);
                int colNumber = Integer.parseInt(cmd.getArguments()[1]);
                Board board = new Board(rowNumber, colNumber);
                mClient.setBoard(board);
            }
        });

        mListeners.registerListener(Command.kServerToClientCommands, new ReceiveListener() {
            @Override
            public void notify(Command cmd, NetworkManager netManager) {
                if (!(cmd instanceof ServerToClientPack)) {
                    e(new Exception("Packet type mismatch."));
                    return;
                }
//				l("Got new game info");
                updateClient((ServerToClientPack) cmd);
//                nextCycle();
                try {
                    callStepController.put(new Object());
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        mListeners.registerListener(Command.kFinished, new ReceiveListener() {
            @Override
            public void notify(final Command cmd,
                    final NetworkManager netManager) {
                if (mNetworkManager != null) {
                    mNetworkManager.stopNetworking();
                }
                System.exit(0);
            }
        });

    }

    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("127.0.0.1", Config.SERVER_PORT);
            Client client = new Client(socket);
        } catch (UnknownHostException ex) {
            e(ex);
        } catch (IOException ex) {
            e(ex);
        }
    }

    private void updateBulletPosition(ArrayList<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            Cell cell = bullet.getCell();
            Cell boardCell = Board.getInstance().getCellAt(cell.getRowNumber(), cell.getColumnNumber());
            boardCell.setBulletIsIn(bullet);
        }
    }
}
