package common.team;

import java.util.ArrayList;

import common.board.Cell;
import common.board.Direction;
import common.board.Gold;
import common.constant.Config;
import common.player.GoldMiner;
import common.player.Hunter;
import common.player.Player;
import common.player.Spy;

/**
 * Represents team.
 */
public class Team {

    private String name;
    private int id;
    private int money;
    private Hunter[] hunters;
    private GoldMiner[] goldMiners;
    private Spy[] spies;

    public Team(String name, int id) {
        this.name = name;
        this.money = 0;
        setTeamID(id);
    }

    public Team(String name) {
        this(name, -1);
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public int getId() {
        return id;
    }

    public int getScore() {
        return money;
    }

    public Hunter[] getHunters() {
        return hunters;
    }

    public void setHunters(Hunter[] hunters) {
        this.hunters = hunters;
    }

    public GoldMiner[] getGoldMiners() {
        return goldMiners;
    }

    public void setGoldMiners(GoldMiner[] goldMiners) {
        this.goldMiners = goldMiners;
    }

    @Override
    public boolean equals(Object obj) {
        return id == ((Team) obj).getId();
    }

    public void setTeamID(int id) {
        this.id = id;
    }

    public int getTeamID() {
        return this.id;
    }

    public Spy[] getSpies() {
        return spies;
    }
}
