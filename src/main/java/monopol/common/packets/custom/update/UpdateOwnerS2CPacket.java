package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.IPurchasable;
import monopol.common.packets.S2CPacket;
import monopol.common.data.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateOwnerS2CPacket extends S2CPacket<UpdateOwnerS2CPacket> {
    private final Map<String, String> owner;

    public UpdateOwnerS2CPacket() {
        owner = new HashMap<>();
        for(IPurchasable purchasable : Field.purchasables()) {
            owner.put(purchasable.getName(), purchasable.getOwner());
        }
    }

    private UpdateOwnerS2CPacket(Map<String, String> owner) {
        this.owner = owner;
    }

    @Override
    public Object[] serialize() {
        return new Object[]{owner.keySet().toArray(), owner.values().toArray()};
    }

    @SuppressWarnings("unused")
    public static UpdateOwnerS2CPacket deserialize(Object[] objects) {
        Map<String, String> owner = new HashMap<>();
        List<String> keys = new ArrayList<>((List<String>) objects[0]);
        List<String> values = new ArrayList<>((List<String>) objects[1]);
        for(int i = 0; i < keys.size(); i++) {
            owner.put(keys.get(i), values.get(i));
        }
        return new UpdateOwnerS2CPacket(owner);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        for(IPurchasable purchasable : Field.purchasables()) {
            if(!owner.containsKey(purchasable.getName())) throw new IllegalStateException();
            purchasable.setOwner(owner.get(purchasable.getName()));
        }
        display.playerInfoPane.update();
    }
}
