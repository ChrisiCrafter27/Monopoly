package monopol.log;

public class ServerLogger extends CustomLogger {
    public static final ServerLogger INSTANCE = new ServerLogger();

    private ServerLogger() {
        super("logs/server_log.txt");
        //getLogger().setLevel(Level.CONFIG);
    }

    @Override
    public CustomLogger getInstance() {
        return ServerLogger.INSTANCE;
    }
}