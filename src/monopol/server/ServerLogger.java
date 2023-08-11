package monopol.server;

import monopol.utils.CustomLogger;

public class ServerLogger extends CustomLogger {
    public static final ServerLogger INSTANCE = new ServerLogger();

    protected ServerLogger() {
        super("logs/server_log.txt");
    }

    @Override
    public CustomLogger getInstance() {
        return ServerLogger.INSTANCE;
    }

    @Override
    protected String getType() {
        return "Server";
    }
}
