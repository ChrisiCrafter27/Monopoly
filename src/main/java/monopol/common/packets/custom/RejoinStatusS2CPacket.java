package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

import java.util.ArrayList;
import java.util.List;

public class RejoinStatusS2CPacket extends S2CPacket<RejoinStatusS2CPacket> {
    private final List<String> names;

    public RejoinStatusS2CPacket(List<String> names) {
        this.names = names;
    }

    @SuppressWarnings("unused")
    public static RejoinStatusS2CPacket deserialize(DataReader reader) {
        return new RejoinStatusS2CPacket(reader.readList(ArrayList::new, DataReader::readString));
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeList(names, DataWriter::writeString);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if(names.isEmpty()) display.rejoinPane.reset();
        else display.rejoinPane.setList(names, display);
    }
}
