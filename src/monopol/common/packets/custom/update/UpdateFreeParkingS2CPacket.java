package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;

public class UpdateFreeParkingS2CPacket extends S2CPacket<UpdateFreeParkingS2CPacket> {
    private final int money;

    public UpdateFreeParkingS2CPacket(int money) {
        this.money = money;
    }

    @Override
    public Object[] serialize() {
        return new Object[]{money};
    }

    @SuppressWarnings("unused")
    public static UpdateFreeParkingS2CPacket deserialize(Object[] objects) {
        return new UpdateFreeParkingS2CPacket((int) objects[0]);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.freeParkingPane.update(money);
    }
}
