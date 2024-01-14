package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;

public class InfoS2CPacket extends S2CPacket<InfoS2CPacket> {
    private final String text;

    public InfoS2CPacket(String text) {
        this.text = text;
    }

    @SuppressWarnings("unused")
    public static InfoS2CPacket deserialize(Object[] objects) {
        return new InfoS2CPacket((String) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{text};
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.infoPane.show(client, text);
    }
}
