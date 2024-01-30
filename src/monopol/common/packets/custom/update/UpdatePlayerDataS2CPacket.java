package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;

public class UpdatePlayerDataS2CPacket extends S2CPacket<UpdatePlayerDataS2CPacket> {
    @Override
    public Object[] serialize() {
        return new Object[]{};
    }

    @SuppressWarnings("unused")
    public static UpdatePlayerDataS2CPacket deserialize(Object[] objects) {
        return new UpdatePlayerDataS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.lobbyPane.requestUpdate();
        display.playerInfoPane.update();
    }
}
