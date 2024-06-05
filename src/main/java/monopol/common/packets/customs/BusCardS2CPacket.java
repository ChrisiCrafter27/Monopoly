package monopol.common.packets.customs;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class BusCardS2CPacket extends S2CPacket<BusCardS2CPacket> {
    private final String player;
    private final boolean expiration;
    private final int size;

    public BusCardS2CPacket(String player, boolean expiration, int size) {
        this.player = player;
        this.expiration = expiration;
        this.size = size;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(player);
        writer.writeBool(expiration);
        writer.writeInt(size);
    }

    public static BusCardS2CPacket deserialize(DataReader reader) {
        return new BusCardS2CPacket(reader.readString(), reader.readBool(), reader.readInt());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.cardDecksPane.updateBusCards(player, expiration, size);
    }
}
