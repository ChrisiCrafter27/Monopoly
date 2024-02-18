package monopol.common.packets.custom;

import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;
import monopol.client.Client;

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
    public Object[] serialize() {
        return new Object[]{player, descriptions, buttons, size};
    }

    @SuppressWarnings({"unused", "unchecked"})
    public static CommunityCardS2CPacket deserialize(Object[] objects) {
        return new CommunityCardS2CPacket((String) objects[0], (List<String>) objects[1], (List<String>) objects[2], (int) objects[3]);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        //TODO update not existing pane
    }
}
