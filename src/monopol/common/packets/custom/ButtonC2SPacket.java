package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.server.Server;

public class ButtonC2SPacket extends C2SPacket<ButtonC2SPacket> {
    private final String name;
    private final Button button;

    public ButtonC2SPacket(String name, Button button) {
        this.name = name;
        this.button = button;
    }

    @SuppressWarnings("unused")
    public static ButtonC2SPacket deserialize(Object[] objects) {
        return new ButtonC2SPacket((String) objects[0], Button.valueOf((String) objects[1]));
    }

    @Override
    public Object[] serialize() {
        return new Object[]{name, button.name()};
    }

    @Override
    public void handleOnServer(Server server) {
        switch (button) {
            case ACTION_1 -> {
                if(server.events().diceRolled()) server.events().onTryNextRound(name);
                else server.events().onDiceRoll(name);
            }
            case ACTION_2 -> {
                if(server.events().diceRolled()) server.events().onPayRent(name);
                //else server.events().onBusDrive();
            }
        }
    }

    public enum Button {
        ACTION_1,
        ACTION_2
    }
}
