package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.Player;
import monopol.common.core.Monopoly;
import monopol.common.packets.S2CPacket;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePlayerDataS2CPacket extends S2CPacket<UpdatePlayerDataS2CPacket> {
    @Override
    public Object[] serialize() {
        return new Object[]{};
    }

    @SuppressWarnings("unused")
    public static UpdatePlayerDataS2CPacket deserialize(Object[] objects) {
        return new UpdatePlayerDataS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.lobbyPane.requestUpdate();
        //TODO: update not existing pane
    }
}
