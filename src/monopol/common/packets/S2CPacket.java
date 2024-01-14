package monopol.common.packets;

import monopol.client.Client;
import monopol.client.screen.RootPane;

public abstract class S2CPacket<T extends S2CPacket<T>> implements Packet<T> {
    @Override
    public void handle(Side side) {
        if (side instanceof ClientSide clientSide) {
            handleOnClient(clientSide.client, clientSide.display);
        } else throw new IllegalStateException();
    }

    public abstract void handleOnClient(Client client, RootPane display);
}
