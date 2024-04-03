package monopol.common.packets.custom;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

import java.net.Socket;

public class EventCardC2SPacket extends C2SPacket<EventCardC2SPacket> {
    private final String player, button;

    public EventCardC2SPacket(String player, String button) {
        this.player = player;
        this.button = button;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(player);
        writer.writeString(button);
    }

    @SuppressWarnings("unused")
    public static EventCardC2SPacket deserialize(DataReader reader) {
        return new EventCardC2SPacket(reader.readString(), reader.readString());
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        server.events().onEventCardAction(player, button);
    }
}
