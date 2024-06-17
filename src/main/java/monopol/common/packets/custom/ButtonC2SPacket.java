package monopol.common.packets.custom;

import monopol.common.core.Monopoly;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;
import monopol.server.events.Events;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

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
        Events events = server.events();
        synchronized (events) {
            switch (button) {
                case ACTION_1 -> {
                    if(events.diceRolled()) server.events().onTryNextRound(name);
                    else events.onDiceRoll(name);
                }
                case ACTION_2 -> {
                    if(server.getPlayerServerSide(name).inPrison()) events.onPaySurety(name, false);
                    else if(events.diceRolled()) events.onPayRent(name);
                    else events.onBusDrive(name, target);
                }
                case PURCHASE -> events.onPurchaseCard(name, selected);
                case UPGRADE -> events.onUpgrade(name, selected);
                case DOWNGRADE -> events.onDowngrade(name, selected);
                case MORTGAGE -> events.onMortgage(name, selected);
                case TELEPORT -> events.onTeleport(name, target);
            }
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
