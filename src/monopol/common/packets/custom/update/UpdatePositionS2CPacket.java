package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.Player;
import monopol.common.core.Monopoly;
import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.packets.S2CPacket;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePositionS2CPacket extends S2CPacket<UpdatePositionS2CPacket> {
    private final Map<String, Integer> positions;

    public UpdatePositionS2CPacket() {
        positions = new HashMap<>();
        try {
            for(Player player : Monopoly.INSTANCE.server().getPlayers()) {
                positions.put(player.getName(), player.getPosition());
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private UpdatePositionS2CPacket(Map<String, Integer> positions) {
        this.positions = positions;
    }

    @Override
    public Object[] serialize() {
        return new Object[]{positions.keySet().toArray(), positions.values().toArray()};
    }

    @SuppressWarnings("unused")
    public static UpdatePositionS2CPacket deserialize(Object[] objects) {
        Map<String, Integer> positions = new HashMap<>();
        List<String> keys = new ArrayList<>((List<String>) objects[0]);
        List<Integer> values = new ArrayList<>((List<Integer>) objects[1]);
        for(int i = 0; i < keys.size(); i++) {
            positions.put(keys.get(i), values.get(i));
        }
        return new UpdatePositionS2CPacket(positions);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.playerDisplayPane.check(positions.keySet());
        positions.forEach(display.playerDisplayPane::setPosWithAnim);
    }
}
