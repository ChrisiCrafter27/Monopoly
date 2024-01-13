package monopol.common.packets.custom;

import monopol.common.packets.ClientSide;
import monopol.common.packets.S2CPacket;

public class InfoS2CPacket extends S2CPacket<InfoS2CPacket> {
    private final String text;

    public InfoS2CPacket(String text) {
        this.text = text;
    }

    public static InfoS2CPacket deserialize(Object[] objects) {
        return new InfoS2CPacket((String) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{text};
    }

    @Override
    public void handleOnClient(ClientSide side) {
        side.display.infoPane.show(text);
    }
}
