package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;

public class RollDiceS2CPacket extends S2CPacket<RollDiceS2CPacket> {
    private final int dice1, dice2, tempoDice;

    public RollDiceS2CPacket(int dice1, int dice2, int tempoDice) {
        this.dice1 = dice1;
        this.dice2 = dice2;
        this.tempoDice = tempoDice;
    }

    @SuppressWarnings("unused")
    public static RollDiceS2CPacket deserialize(Object[] objects) {
        return new RollDiceS2CPacket((int) objects[0], (int) objects[1], (int) objects[2]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{dice1, dice2, tempoDice};
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.dicePane.show(dice1,dice2,tempoDice);
    }
}
