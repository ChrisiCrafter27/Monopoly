package monopol.common.packets.custom;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

import java.net.Socket;

public class CommunityCardC2SPacket extends C2SPacket<CommunityCardC2SPacket> {
    private final String button;

    public CommunityCardC2SPacket(String button) {
        this.button = button;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(button);
    }

    @SuppressWarnings("unused")
    public static CommunityCardC2SPacket deserialize(DataReader reader) {
        return new CommunityCardC2SPacket(reader.readString());
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        server.events().onCommunityCardAction(button);
    }
}
