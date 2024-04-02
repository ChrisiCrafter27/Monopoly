package monopol.common.packets.custom;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

import java.net.Socket;

public class BusCardC2SPacket extends C2SPacket<BusCardC2SPacket> {
    private final String player;

    public BusCardC2SPacket(String player) {
        this.player = player;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(player);
    }

    @SuppressWarnings("unused")
    public static BusCardC2SPacket deserialize(DataReader reader) {
        return new BusCardC2SPacket(reader.readString());
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        server.events().onTakeBusCard(player);
    }
}
