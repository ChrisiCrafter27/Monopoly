package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.server.Server;

public class CommunityCardC2SPacket extends C2SPacket<CommunityCardC2SPacket> {
    private final String button;

    public CommunityCardC2SPacket(String button) {
        this.button = button;
    }

    @Override
    public Object[] serialize() {
        return new Object[]{button};
    }

    @SuppressWarnings("unused")
    public static CommunityCardC2SPacket deserialize(Object[] objects) {
        return new CommunityCardC2SPacket((String) objects[0]);
    }

    @Override
    public void handleOnServer(Server server) {
        server.events().onCommunityCardAction(button);
    }
}
