package monopol.log;

import java.util.logging.Level;

public class ServerLogger extends CustomLogger {
    public static final ServerLogger INSTANCE = new ServerLogger();

    private ServerLogger() {
        super("logs/server_log.txt");
        getLogger().setLevel(Level.INFO);
    }

    @Override
    public CustomLogger getInstance() {
        return ServerLogger.INSTANCE;
    }
}
