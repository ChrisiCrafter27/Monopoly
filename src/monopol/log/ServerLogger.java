package monopol.log;

import java.util.logging.Level;

public class ServerLogger extends ICustomLogger {
    public static final ServerLogger INSTANCE = new ServerLogger();

    private ServerLogger() {
        super("logs/server_log.txt");
        getLogger().setLevel(Level.CONFIG);
    }

    @Override
    public ICustomLogger getInstance() {
        return ServerLogger.INSTANCE;
    }
}
