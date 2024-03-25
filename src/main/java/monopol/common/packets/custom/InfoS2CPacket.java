package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class InfoS2CPacket extends S2CPacket<InfoS2CPacket> {
    private final String text;

    public InfoS2CPacket(String text) {
        this.text = text;
    }

    @SuppressWarnings("unused")
    public static InfoS2CPacket deserialize(DataReader reader) {
        return new InfoS2CPacket(reader.readString());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(text);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.infoPane.show(client, text);
    }
}
