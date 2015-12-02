package common.net;

import java.util.Date;
import java.util.List;

public class Command {

    /**
     *
     */
    private static int id = 0;

    private static int g() {
        return id++;
    }

    public static final int kRequestTeamID = g();
    public static final int kClientToServerCommands = g();
    public static final int kServerToClientCommands = g();
    public static final int kMapInfo = g();
    public static final int kFinished = g();

    private static final Date now = new Date();
    private static final String[] ZERO = new String[0];

    /**
     * Creates a command.
     *
     * @param id
     * @param arguments
     * @return
     */
    public static Command create(int id, List<String> arguments) {
        String[] args = null;
        if (arguments != null) {
            args = new String[arguments.size()];
            arguments.toArray(args);
        }

        return create(id, args);
    }

    /**
     * Creates a command.
     *
     * @param id
     * @param arguments
     * @return
     */
    public static Command create(int id, String[] arguments) {
        Command c = new Command(id);
        c.setArguments(arguments);

        return c;
    }

    /**
     * Creates a command that has no arguments.
     *
     * @param id
     * @return
     */
    public static Command create(int id) {
        return create(id, ZERO);
    }

    /**
     * Command id
     */
    private int mCommandID;
    /**
     * Time created
     */
    private long mTimeSent;
    /**
     * Arguments. if has any.
     */
    private String[] mArguments;
    /**
     * Number of failed attempts before successfully sending this command + 1.
     */
    private transient int sendAttempts = 0;

    protected Command(int id) {
        this.mCommandID = id;
        this.mTimeSent = now.getTime();
    }

    private void setArguments(String[] args) {
        this.mArguments = args;
    }

    public boolean hasArguments() {
        return mArguments != null && mArguments.length > 0;
    }

    public int getCommandID() {
        return mCommandID;
    }

    public void setCommandID(int commandID) {
        this.mCommandID = commandID;
    }

    public long getTimeSent() {
        return mTimeSent;
    }

    public void setTimeSent(long timeSent) {
        this.mTimeSent = timeSent;
    }

    public String[] getArguments() {
        return mArguments;
    }

    public void addSendAttempts() {
        ++sendAttempts;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }
}
