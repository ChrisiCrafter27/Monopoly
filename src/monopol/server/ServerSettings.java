package monopol.server;

import java.io.Serializable;

public class ServerSettings implements Serializable {
    public boolean allPlayersCanKick;
    public boolean allPlayersCanAccessSettings;

    public ServerSettings(boolean allPlayersCanKick, boolean allPlayersCanAccessSettings) {
        this.allPlayersCanKick = allPlayersCanKick;
        this.allPlayersCanAccessSettings = allPlayersCanAccessSettings;
    }
}