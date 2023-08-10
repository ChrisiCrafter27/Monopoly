package monopol.test;

import monopol.server.ServerLogger;

public class TestF {
    public static void main(String[] args) {
        ServerLogger.INSTANCE.getLogger().finer("Everything fine");
        ServerLogger.INSTANCE.getLogger().config("Here is some config");
        ServerLogger.INSTANCE.getLogger().info("This is an information");
        ServerLogger.INSTANCE.getLogger().warning("Oh no, there is a problem");
        ServerLogger.INSTANCE.getLogger().severe("This probably causes a crash");
    }
}
