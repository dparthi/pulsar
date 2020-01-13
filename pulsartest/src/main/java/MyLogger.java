import java.util.Date;

public class MyLogger {
    public static void log(String message) {
        System.out.println("[" + Thread.currentThread().getId() + "] [" + (new Date()) + "] " + message);
    }

    public static void logError(String message) {
        System.err.println("[" + Thread.currentThread().getId() + "] [" + (new Date()) + "] " + message);
    }
}
