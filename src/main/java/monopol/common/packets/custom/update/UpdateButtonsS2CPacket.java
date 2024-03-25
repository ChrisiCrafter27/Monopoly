package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
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
    public static UpdateButtonsS2CPacket deserialize(DataReader reader) {
        return new UpdateButtonsS2CPacket(reader.readString(), reader.readBool(), reader.readBool(), reader.readBool(), reader.readBool());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(currentPlayer);
        writer.writeBool(diceRolled);
        writer.writeBool(hasToPayRent);
        writer.writeBool(inPrison);
        writer.writeBool(ready);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.playerInfoPane.updateButtons(currentPlayer, diceRolled, hasToPayRent, inPrison, ready);
    }
}
