package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.core.GameState;
import monopol.common.core.Monopoly;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.PacketManager;
import monopol.common.packets.S2CPacket;

public class StartS2CPacket extends S2CPacket<StartS2CPacket> {
    public StartS2CPacket() {}

    @Override
    public void serialize(DataWriter writer) {}

    @SuppressWarnings("unused")
    public static StartS2CPacket deserialize(DataReader reader) {
        return new StartS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        Monopoly.INSTANCE.setState(GameState.RUNNING);
    }
}
