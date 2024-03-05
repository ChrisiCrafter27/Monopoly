package monopol.common.packets.custom;

import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

public class ButtonC2SPacket extends C2SPacket<ButtonC2SPacket> {
    private final String name;
    private final String selectedCard;
    private final Button button;

    public ButtonC2SPacket(String name, String selectedCard, Button button) {
        this.name = name;
        this.selectedCard = selectedCard;
        this.button = button;
    }

    @SuppressWarnings("unused")
    public static ButtonC2SPacket deserialize(Object[] objects) {
        return new ButtonC2SPacket((String) objects[0], (String) objects[1], Button.valueOf((String) objects[2]));
    }

    @Override
    public Object[] serialize() {
        return new Object[]{name, selectedCard, button.name()};
    }

    @Override
    public void handleOnServer(Server server) {
        IPurchasable selected = Field.purchasables().stream().filter(p -> p.getName().equals(selectedCard)).findFirst().orElse(null);
        switch (button) {
            case ACTION_1 -> {
                if(server.events().diceRolled()) server.events().onTryNextRound(name);
                else server.events().onDiceRoll(name);
            }
            case ACTION_2 -> {
                if(server.getPlayerServerSide(name).inPrison()) server.events().onPaySurety(name);
                else if(server.events().diceRolled()) server.events().onPayRent(name);
                //else server.events().onBusDrive();
            }
            case PURCHASE -> server.events().onPurchaseCard(name, selected);
            case UPGRADE -> server.events().onUpgrade(name, selected);
            case DOWNGRADE -> server.events().onDowngrade(name, selected);
            case MORTGAGE -> server.events().onMortgage(name, selected);
        }
    }

    public enum Button {
        ACTION_1,
        ACTION_2,
        PURCHASE,
        UPGRADE,
        DOWNGRADE,
        MORTGAGE
    }
}
