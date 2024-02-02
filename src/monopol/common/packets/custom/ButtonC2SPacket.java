package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.server.Server;

public class ButtonC2SPacket extends C2SPacket<ButtonC2SPacket> {
    private static String target = null;

    public static void request(String target) {
        ButtonC2SPacket.target = target;
    }

    private final String name;

    public ButtonC2SPacket(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public static ButtonC2SPacket deserialize(Object[] objects) {
        return new ButtonC2SPacket((String) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{name};
    }

    @Override
    public void handleOnServer(Server server) {
        if(name.equals(target)) {
            server.events().onDiceRoll();
        }
    }
}
