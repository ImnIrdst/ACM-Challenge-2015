package common.net.commands;

import java.util.ArrayList;

import common.board.Cell;
import common.board.Gold;
import common.net.Command;
import common.player.Bullet;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Spy;

public class ServerToClientPack extends Command {

    public int cycle;
    public int yourScore;
    public int opponentScore;
    public ArrayList<Cell> updateCells;
    public ArrayList<GoldMiner> goldMiner;
    public ArrayList<Hunter> hunter;
    public ArrayList<Gold> golds;
    public ArrayList<Spy> spies;
    public ArrayList<Spy> opponentSpies;
    public ArrayList<GoldMiner> opponentGoldMiner;
    public ArrayList<Hunter> opponentHunter;
    public ArrayList<Bullet> bullets;

    public ServerToClientPack() {
        super(kServerToClientCommands);
        updateCells = new ArrayList<Cell>();
        golds = new ArrayList<Gold>();
        goldMiner = new ArrayList<GoldMiner>();
        hunter = new ArrayList<Hunter>();
        opponentGoldMiner = new ArrayList<GoldMiner>();
        opponentHunter = new ArrayList<Hunter>();
        spies = new ArrayList<Spy>();
        opponentSpies = new ArrayList<Spy>();
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getYourScore() {
        return yourScore;
    }

    public void setYourScore(int yourScore) {
        this.yourScore = yourScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }

    public ArrayList<Cell> getUpdateCells() {
        return updateCells;
    }

    public void setUpdateCells(ArrayList<Cell> updateCells) {
        this.updateCells = updateCells;
    }

    public ArrayList<Gold> getGolds() {
        return golds;
    }

    public void setGolds(ArrayList<Gold> golds) {
        this.golds = golds;
    }

    public ArrayList<GoldMiner> getGoldMiner() {
        return goldMiner;
    }

    public void setGoldMiner(ArrayList<GoldMiner> goldMiner) {
        this.goldMiner = goldMiner;
    }

    public ArrayList<Hunter> getHunter() {
        return hunter;
    }

    public void setHunter(ArrayList<Hunter> hunter) {
        this.hunter = hunter;
    }

    public ArrayList<GoldMiner> getOpponentGoldMiner() {
        return opponentGoldMiner;
    }

    public void setOpponentGoldMiner(ArrayList<GoldMiner> opponentGoldMiner) {
        this.opponentGoldMiner = opponentGoldMiner;
    }

    public ArrayList<Hunter> getOpponentHunter() {
        return opponentHunter;
    }

    public void setOpponentHunter(ArrayList<Hunter> opponentHunter) {
        this.opponentHunter = opponentHunter;
    }
}
