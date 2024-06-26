package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.client.screen.SettingsScreen;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class StartS2CPacket extends S2CPacket<StartS2CPacket> {
    private final boolean tempoDice;

    public StartS2CPacket(boolean tempoDice) {
        this.tempoDice = tempoDice;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeBool(tempoDice);
    }

    public static StartS2CPacket deserialize(DataReader reader) {
        return new StartS2CPacket(reader.readBool());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.start();
        display.dicePane.setSWVisibility(tempoDice);
    }
}
