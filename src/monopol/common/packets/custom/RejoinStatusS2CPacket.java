package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;

import java.util.List;

public class RejoinStatusS2CPacket extends S2CPacket<RejoinStatusS2CPacket> {
    private final List<String> names;

    public RejoinStatusS2CPacket(List<String> names) {
        this.names = names;
    }

    @SuppressWarnings("unused")
    public static RejoinStatusS2CPacket deserialize(Object[] objects) {
        return new RejoinStatusS2CPacket((List<String>) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{names};
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if(names.isEmpty()) display.rejoinPane.reset();
        else display.rejoinPane.setList(names);
    }
}
