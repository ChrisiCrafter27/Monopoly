package monopol.common.packets.custom;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

import java.net.Socket;

public class ButtonC2SPacket extends C2SPacket<ButtonC2SPacket> {
    private final String name;
    private final String selectedCard;
    private final Button button;
    private final int target;

    public ButtonC2SPacket(String name, String selectedCard, Button button) {
        this(name, selectedCard,  button, 0);
    }

    public ButtonC2SPacket(String name, String selectedCard, Button button, int target) {
        this.name = name;
        this.selectedCard = selectedCard;
        this.button = button;
        this.target = target;
    }

    public static ButtonC2SPacket deserialize(DataReader reader) {
        return new ButtonC2SPacket(reader.readString(), reader.readString(), reader.readEnum(Button.class), reader.readInt());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(name);
        writer.writeString(selectedCard);
        writer.writeEnum(button);
        writer.writeInt(target);
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        IPurchasable selected = Field.purchasables().stream().filter(p -> p.getName().equals(selectedCard)).findFirst().orElse(null);
        switch (button) {
            case ACTION_1 -> {
                if(server.events().diceRolled()) server.events().onTryNextRound(name);
                else server.events().onDiceRoll(name);
            }
            case ACTION_2 -> {
                if(server.getPlayerServerSide(name).inPrison()) server.events().onPaySurety(name, false);
                else if(server.events().diceRolled()) server.events().onPayRent(name);
                else server.events().onBusDrive(name, target);
            }
            case PURCHASE -> server.events().onPurchaseCard(name, selected);
            case UPGRADE -> server.events().onUpgrade(name, selected);
            case DOWNGRADE -> server.events().onDowngrade(name, selected);
            case MORTGAGE -> server.events().onMortgage(name, selected);
            case TELEPORT -> server.events().onTeleport(name, target);
        }
    }

    public enum Button {
        ACTION_1,
        ACTION_2,
        PURCHASE,
        UPGRADE,
        DOWNGRADE,
        MORTGAGE,
        TELEPORT
    }
}
