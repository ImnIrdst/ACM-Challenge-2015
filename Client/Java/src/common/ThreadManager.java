package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

//	public static final ExecutorService t = Executors.newFixedThreadPool(2);
    public static final ExecutorService t = Executors.newCachedThreadPool();

    public static void run(Runnable command) {
        if (command != null) {
            try {
                t.execute(command);
            } catch (Exception e) {
                throw new RuntimeException("Faied to run command.", e);
            }
        }
    }

    public static void kill() {
        t.shutdown();
    }
}
