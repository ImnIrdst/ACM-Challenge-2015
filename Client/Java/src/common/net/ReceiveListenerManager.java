package common.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.ThreadManager;

public final class ReceiveListenerManager {

    private static Logger l = Logger.getLogger("common.net.ReceiveListenerManager");

    static void e(Exception e) {
        l.log(Level.SEVERE, null, e);
    }

    private HashMap<Integer, ReceiveListeners> mListenersMap;
    private NetworkManager mNetworkManager;

    public ReceiveListenerManager() {
        mListenersMap = new HashMap<Integer, ReceiveListeners>();
    }

    public ReceiveListenerManager(NetworkManager networkmanager) {
        this();
        setNetworkManager(networkmanager);
    }

    public void setNetworkManager(NetworkManager networkmanager) {
        mNetworkManager = networkmanager;
        mNetworkManager.setListenerManager(this);
    }

    public void registerListener(int type) {
        if (!mListenersMap.containsKey(type)) {
            mListenersMap.put(type, new ReceiveListeners());
        }
    }

    public void unRegisterAllListeners(int type) {
        mListenersMap.remove(type);
    }

    public void unRegisterListener(int type, ReceiveListener l) {
        mListenersMap.get(type).remove(l);
    }

    public void unRegisterListener(ReceiveListener l) {
        for (ReceiveListeners ls : mListenersMap.values()) {
            ls.remove(l);
        }
    }

    public void registerListener(int type, ReceiveListener listener) {
        registerListener(type);
        mListenersMap.get(type).add(listener);
    }

//    public void startListening() {
//        ThreadManager.run(new Runnable() {
//            @Override
//            public void run() {
//                while (mNetworkManager.isNetworkingEnabled()) {
//                    Command cmd = mNetworkManager.getRecievedCommandBlocking();
//
//                    if (mListenersMap.containsKey(cmd.getCommandID())) {
//                        mListenersMap.get(cmd.getCommandID()).notifyListeners(cmd);
//                    } else {
//                        l.log(Level.WARNING, "No listener was registered for command. Ignoring it. (CMD_ID = " + cmd.getCommandID() + ")\n");
//                    }
//                }
//            }
//        });
//    }
    public void startListening() {
        ThreadManager.run(new Runnable() {
            @Override
            public void run() {
                while (mNetworkManager.isNetworkingEnabled()) {
                    Command cmd = mNetworkManager.getRecievedCommandBlocking();
                    commandReceived(cmd);
                }
            }
        });
    }

    protected void commandReceived(Command cmd) {
//                    if (cmd == null)
//                        continue;
        if (mListenersMap.containsKey(cmd.getCommandID())) {
            mListenersMap.get(cmd.getCommandID()).notifyListeners(cmd);
        } else {
            l.log(Level.WARNING, "No listener was registered for command. Ignoring it. (CMD_ID = " + cmd.getCommandID() + ")\n");
        }
    }

    final class ReceiveListeners {

        private Logger l = Logger.getLogger("common.net.ReceiveListeners");

        void e(Exception e) {
            l.log(Level.SEVERE, null, e);
        }

        private ArrayList<ReceiveListener> mListeners;

        public ReceiveListeners() {
            mListeners = new ArrayList<ReceiveListener>();
        }

        public void notifyListeners(Command cmd) {
            if (mListeners.size() == 0) {
                l.log(Level.WARNING, "No listener was registered for command. Ignoring it. (CMD_ID = " + cmd.getCommandID() + ")\n");
            }
            for (ReceiveListener l : mListeners) {
                try {
                    l.notify(cmd, mNetworkManager);
                } catch (Exception e) {
                    e(e);
                }
            }
        }

        public void add(ReceiveListener l) {
            mListeners.add(l);
        }

        public void remove(ReceiveListener l) {
            mListeners.remove(l);
        }
    }

    public void stopListening() {
        mListenersMap.clear();
    }
}
