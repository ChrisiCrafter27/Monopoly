package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.common.packets.PacketManager;
import monopol.server.Server;

import java.rmi.RemoteException;
import java.util.Random;

public class RollDiceC2SPacket extends C2SPacket<RollDiceC2SPacket> {
    private static String target = null;

    public static void request(String target) {
        RollDiceC2SPacket.target = target;
    }

    private final String name;

    public RollDiceC2SPacket(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public static RollDiceC2SPacket deserialize(Object[] objects) {
        return new RollDiceC2SPacket((String) objects[0]);
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
