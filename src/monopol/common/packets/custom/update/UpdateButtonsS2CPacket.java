package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.packets.S2CPacket;

public class UpdateButtonsS2CPacket extends S2CPacket<UpdateButtonsS2CPacket> {
    private final String currentPlayer;
    private final boolean diceRolled, hasToPayRent, inPrison, ready;

    public UpdateButtonsS2CPacket(String currentPlayer, boolean diceRolled, boolean hasToPayRent, boolean inPrison, boolean ready) {
        this.currentPlayer = currentPlayer;
        this.diceRolled = diceRolled;
        this.hasToPayRent = hasToPayRent;
        this.inPrison = inPrison;
        this.ready = ready;
    }

    @SuppressWarnings("unused")
    public static UpdateButtonsS2CPacket deserialize(Object[] objects) {
        return new UpdateButtonsS2CPacket((String) objects[0], (boolean) objects[1], (boolean) objects[2], (boolean) objects[3], (boolean) objects[4]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{currentPlayer, diceRolled, hasToPayRent, inPrison, ready};
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.playerInfoPane.updateButtons(currentPlayer, diceRolled, hasToPayRent, inPrison, ready);
    }
}
