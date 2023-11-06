package monopol.log;

import java.util.logging.Level;

public class DebugLogger extends CustomLogger {
    public static final DebugLogger INSTANCE = new DebugLogger();

    private DebugLogger() {
        super("logs/debug_log.txt");
        log().setLevel(Level.INFO);
    }

    @Override
    public CustomLogger getInstance() {
        return DebugLogger.INSTANCE;
    }
}
