package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.*;
import monopol.common.packets.S2CPacket;

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
        System.out.println(Street.BADSTRASSE.getOwner());
        System.out.println(Field.purchasables().get(0).getOwner());
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
        System.out.println(Street.BADSTRASSE.getOwner());
        System.out.println(Field.purchasables().get(0).getOwner());
    }
}
