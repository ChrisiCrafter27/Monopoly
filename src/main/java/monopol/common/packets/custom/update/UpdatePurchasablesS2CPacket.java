package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.data.IPurchasable;
import monopol.common.packets.S2CPacket;
import monopol.common.data.Field;

import java.rmi.RemoteException;

public class UpdatePurchasablesS2CPacket extends S2CPacket<UpdatePurchasablesS2CPacket> {
    public UpdatePurchasablesS2CPacket() {}

    @Override
    public void serialize(DataWriter writer) {}

    public static UpdatePurchasablesS2CPacket deserialize(DataReader reader) {
        return new UpdatePurchasablesS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        try {
            for(IPurchasable purchasable : client.serverMethod().getPurchasables()) {
                String name = purchasable.getName();
                Field.purchasables().stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow().copyOf(purchasable);
            }
            display.selectedCardPane.update();
            display.housePane.update();
        } catch (RemoteException e) {
            client.close();
        }
    }
}
