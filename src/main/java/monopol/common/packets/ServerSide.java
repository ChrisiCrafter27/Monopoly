package monopol.common.packets;

import monopol.server.Server;

public class ServerSide implements Side {
    public final Server server;

    public ServerSide(Server server) {
        this.server = server;
    }
}
