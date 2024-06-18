package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.core.Monopoly;
import monopol.common.data.*;
import monopol.common.packets.S2CPacket;

import java.rmi.RemoteException;

public class UpdatePurchasablesS2CPacket extends S2CPacket<UpdatePurchasablesS2CPacket> {
    private final DataReader dataReader;

    public UpdatePurchasablesS2CPacket() {
        dataReader = null;
    }

    private UpdatePurchasablesS2CPacket(DataReader dataReader) {
        this.dataReader = dataReader;
    }

    @Override
    public void serialize(DataWriter writer) {
        Field.purchasables().forEach(purchasable -> purchasable.write(writer));
    }

    public static UpdatePurchasablesS2CPacket deserialize(DataReader reader) {
        return new UpdatePurchasablesS2CPacket(reader);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if(dataReader != null) {
            if(!Monopoly.INSTANCE.serverEnabled()) Field.purchasables().forEach(purchasable -> purchasable.read(dataReader));
            display.selectedCardPane.update();
            display.housePane.update();
        }
    }
}
