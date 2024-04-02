package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class UpdatePlayerDataS2CPacket extends S2CPacket<UpdatePlayerDataS2CPacket> {
    @Override
    public void serialize(DataWriter writer) {}

    @SuppressWarnings("unused")
    public static UpdatePlayerDataS2CPacket deserialize(DataReader reader) {
        return new UpdatePlayerDataS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.lobbyPane.requestUpdate();
        display.playerInfoPane.update();
        display.buttonsPane.update();
    }
}
