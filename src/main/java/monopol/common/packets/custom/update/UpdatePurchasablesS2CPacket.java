package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.IPurchasable;
import monopol.common.packets.S2CPacket;
import monopol.common.data.Field;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePurchasablesS2CPacket extends S2CPacket<UpdatePurchasablesS2CPacket> {
    public UpdatePurchasablesS2CPacket() {}

    @Override
    public Object[] serialize() {
        return new Object[]{};
    }

    @SuppressWarnings("unused")
    public static UpdatePurchasablesS2CPacket deserialize(Object[] objects) {
        return new UpdatePurchasablesS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        try {
            for(IPurchasable purchasable : client.serverMethod().getPurchasables()) {
                String name = purchasable.getName();
                Field.purchasables().stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow().copyOf(purchasable);
            }
            display.selectedCardPane.tryUpdate();
        } catch (RemoteException e) {
            client.close();
        }
    }
}
