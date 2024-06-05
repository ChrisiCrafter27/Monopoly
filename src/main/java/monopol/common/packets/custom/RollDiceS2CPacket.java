package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.S2CPacket;

public class RollDiceS2CPacket extends S2CPacket<RollDiceS2CPacket> {
    private final int dice1, dice2, tempoDice;

    public RollDiceS2CPacket(int dice1, int dice2, int tempoDice) {
        this.dice1 = dice1;
        this.dice2 = dice2;
        this.tempoDice = tempoDice;
    }

    public static RollDiceS2CPacket deserialize(DataReader reader) {
        return new RollDiceS2CPacket(reader.readInt(), reader.readInt(), reader.readInt());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeInt(dice1);
        writer.writeInt(dice2);
        writer.writeInt(tempoDice);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.dicePane.show(dice1,dice2,tempoDice);
    }
}
