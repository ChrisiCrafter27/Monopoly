package monopol.common.packets;

import monopol.server.Server;

import java.net.Socket;

public abstract class C2SPacket<T extends C2SPacket<T>> implements Packet<T> {
    @Override
    public void handle(Side side) {
        if (side instanceof ServerSide serverSide) {
            handleOnServer(serverSide.server(), serverSide.source());
        } else throw new IllegalStateException();
    }

    public abstract void handleOnServer(Server server, Socket source);
}
