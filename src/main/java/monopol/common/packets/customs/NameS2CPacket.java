package monopol.common.packets.customs;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.core.Monopoly;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class NameS2CPacket extends S2CPacket<NameS2CPacket> {
    private final String name;

    public NameS2CPacket(String name) {
        this.name = name;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(name);
    }

    public static NameS2CPacket deserialize(DataReader reader) {
        return new NameS2CPacket(reader.readString());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if (client.player().getName() == null) {
            client.player().setName(name);
            if(client.player().isHost) Monopoly.INSTANCE.setHost(client.player().getName());
            display.playerSelectPane.requestUpdate();
        }
    }
}
