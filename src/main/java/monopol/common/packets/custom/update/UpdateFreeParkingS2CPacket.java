package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class UpdateFreeParkingS2CPacket extends S2CPacket<UpdateFreeParkingS2CPacket> {
    private final int money;

    public UpdateFreeParkingS2CPacket(int money) {
        this.money = money;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeInt(money);
    }

        public static UpdateFreeParkingS2CPacket deserialize(DataReader reader) {
        return new UpdateFreeParkingS2CPacket(reader.readInt());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.freeParkingPane.update(money);
    }
}
