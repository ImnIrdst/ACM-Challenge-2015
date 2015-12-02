package common.constant;

public class Config {

    //TODO Change this constants.
    public static int NUM_OF_HUNTERS;
    public static int NUM_OF_GOLD_MINERS;
    public static int NUM_OF_SPIES;
    public static int BULLET_CAPICITY;
    public static int SPY_HIDENESS_CAPACITY;
    public static final int SPY_SPEED = 1;
    public static final int NUM_OF_MINING_CYCLES = 3;
    public static final int NUM_OF_TEAMS = 2;
    public static final int SERVER_PORT = 8569;
    public static final int BULLET_SPEED = 2;

    private int numOfGolds;

    public int getNumOfGolds() {
        return numOfGolds;
    }

    public void setNumOfGolds(int numOfGolds) {
        this.numOfGolds = numOfGolds;
    }

}
