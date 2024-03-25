package monopol.common.packets.custom;

import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;
import monopol.client.Client;

import java.util.ArrayList;
import java.util.List;

public class CommunityCardS2CPacket extends S2CPacket<CommunityCardS2CPacket> {
    private final String player;
    private final List<String> descriptions;
    private final List<String> buttons;
    private final int size;

    public CommunityCardS2CPacket(String player, List<String> descriptions, List<String> buttons, int size) {
        this.player = player;
        this.descriptions = descriptions;
        this.buttons = buttons;
        this.size = size;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(player);
        writer.writeList(descriptions, DataWriter::writeString);
        writer.writeList(buttons, DataWriter::writeString);
        writer.writeInt(size);
    }

    public static CommunityCardS2CPacket deserialize(DataReader reader) {
        return new CommunityCardS2CPacket(reader.readString(), reader.readList(ArrayList::new, DataReader::readString), reader.readList(ArrayList::new, DataReader::readString), reader.readInt());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        //TODO update not existing pane
    }
}
