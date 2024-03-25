package monopol.common.packets;

import monopol.server.Server;

import java.net.Socket;

public record ServerSide(Server server, Socket source) implements Side {}