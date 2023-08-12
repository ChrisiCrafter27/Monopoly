package monopol.server;

public class ServerSettings {
    public boolean allPlayersCanKick;
    public boolean allPlayersCanAccessSettings;

    public ServerSettings(boolean allPlayersCanKick, boolean allPlayersCanAccessSettings) {
        this.allPlayersCanKick = allPlayersCanKick;
        this.allPlayersCanAccessSettings = allPlayersCanAccessSettings;
    }
}