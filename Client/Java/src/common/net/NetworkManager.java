package common.net;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import common.ThreadManager;
import common.net.commands.ClientToServerPack;
import common.net.commands.ServerToClientPack;

public class NetworkManager {

    private static Logger l = Logger.getLogger("common.net.NetworkManager");

    private static void e(Exception e) {
        l.log(Level.SEVERE, null, e);
    }

    private OutputStreamWriter mWriter;
    private InputStreamReader mReader;

    private boolean mContinueNetworking;
    private LinkedBlockingDeque<Command> commandsToSend = new LinkedBlockingDeque<Command>();
    private LinkedBlockingDeque<Command> commandsReceived = new LinkedBlockingDeque<Command>();

    private Gson mGson;
    private OutputStreamWriter mLogOutput;
    private OutputStreamWriter mLogInput;
    private boolean mLogToFile = false;

    private ReceiveListenerManager mListener;

    public NetworkManager(Socket socket) throws IOException {
        mWriter = new OutputStreamWriter(socket.getOutputStream());
        mReader = new InputStreamReader(socket.getInputStream());

        mGson = new Gson();
    }

    /**
     * Checks if there is a recieved command that is not read yet.
     *
     * @return
     */
    public boolean hasNewRecievedCommands() {
        return commandsReceived.size() > 0;
    }

    /**
     * Returns first unread recieved command. If there is not any, returns null.
     *
     * @return
     */
    public Command getRecievedCommand() throws InterruptedException {
        if (hasNewRecievedCommands()) {
            return getRecievedCommandBlocking();
        } else {
            return null;
        }
    }

    public void setListenerManager(ReceiveListenerManager listenerManager) {
        mListener = listenerManager;
    }

    /**
     * Returns first unread recieved command. Blocks current thread till one
     * recieved.
     *
     * Caution: it will block until a new command read, even if network
     * communication ends.
     *
     * @return
     */
    public Command getRecievedCommandBlocking() {
        try {
            return commandsReceived.take();
        } catch (InterruptedException ex) {
            e(ex);
        }
        return null;
    }

    /**
     * Starts network threads that send and receive data
     */
    public void startNetworking() {
        if (mContinueNetworking) {
            throw new IllegalStateException(
                    "You should start networking only once.");
        }

        mContinueNetworking = true;

        ThreadManager.run(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mContinueNetworking) {
                        doSend();
                    }
                } finally {
                    try {
                        mWriter.close();
                    } catch (IOException ex) {
                    }
                }
            }
        });

        ThreadManager.run(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mContinueNetworking) {
                        doReceive();
                    }
                } finally {
                    try {
                        mReader.close();
                    } catch (IOException ex) {
                    }
                }
            }
        });
    }

    /**
     * Stops network threads.
     */
    public void stopNetworking() {
        mContinueNetworking = false;
        if (mLogToFile) {
            if (mLogInput != null) {
                try {
                    mLogInput.close();
                } catch (IOException e) {
                    e(e);
                }
            }
            if (mLogOutput != null) {
                try {
                    mLogOutput.close();
                } catch (IOException e) {
                    e(e);
                }
            }
        }
        System.gc();
        ThreadManager.kill();
    }

    /**
     * Do actual send commands stuff. used inside a loop. If IOException occurs
     * during sending a command it will retry.
     *
     */
    private void doSend() {
        Command toSend;
        try {
            toSend = commandsToSend.take();
            toSend.addSendAttempts();
            String serialized;
            serialized = null;
            try {
                serialized = mGson.toJson(toSend);
            } catch (Exception e) {
                e(e);
            }

            try {
                mWriter.write(serialized);
                mWriter.write('\n');
                mWriter.flush();
                // System.out.println(serialized);
                if (mLogToFile) {
                    mLogOutput.write(serialized);
                    mLogOutput.write('\n');
                    mLogOutput.flush();
                }
            } catch (SocketException e) {
                e(e);
                stopNetworking();
            } catch (IOException e) {
                e(e);
                commandsToSend.put(toSend);
            } finally {
                if (mLogToFile) {
                    try {
                        mLogOutput.flush();
                    } catch (Exception e) {
                        e(e);
                    }
                }
            }
            // }
        } catch (InterruptedException ex) {
            e(ex);
        }
    }

    /**
     * Reads commands form socket. You can access received commands throw
     *
     * @link{getRecievedCommand and @link{getRecievedCommandBlocking}.
     *
     *                          You should do callback handlings yourself
     *                          through those methods.
     */
    private void doReceive() {
        Scanner s = new Scanner(mReader);
        while (s.hasNext()) {
            String newLine = s.nextLine();
            // System.out.println("-->" + newLine);
            Command readCommand = mGson.fromJson(newLine, Command.class);
            if (readCommand.getCommandID() == Command.kClientToServerCommands) {
                readCommand = mGson.fromJson(newLine, ClientToServerPack.class);
            } else if (readCommand.getCommandID() == Command.kServerToClientCommands) {
                readCommand = mGson.fromJson(newLine, ServerToClientPack.class);
            }

            if (mLogToFile) {
                try {
                    mLogInput.write(newLine);
                    mLogInput.write('\n');
                } catch (IOException e) {
                    e(e);
                } finally {
                    if (mLogToFile) {
                        try {
                            mLogInput.flush();
                        } catch (Exception e) {
                            e(e);
                        }
                    }
                }
            }

            // try {
            // commandsReceived.put(readCommand);
            mListener.commandReceived(readCommand);
            // } catch (InterruptedException ex) {
            // e(ex);
            // }
        }
        s.close();
    }

    /**
     * Adds a command to send queue to be sent ASAP.
     *
     *
     * @param c : Command to be sent.
     */
    public void send(Command c) {
        if (c != null) {
            try {
                commandsToSend.put(c);
            } catch (InterruptedException ex) {
                e(ex);
            }
        }
    }

    /**
     * Returns true if startNetworking() have been called but stopNetworking()
     * is not called yet.
     *
     * @return
     */
    public boolean isNetworkingEnabled() {
        return mContinueNetworking;
    }

    public void setLogger(OutputStreamWriter logOutput,
            OutputStreamWriter logInput) {
        mLogOutput = logOutput;
        mLogInput = logInput;
        mLogToFile = true;
    }
}
