package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.common.packets.PacketManager;
import monopol.server.Server;

import java.rmi.RemoteException;

public class TestC2SPacket extends C2SPacket<TestC2SPacket> {
    private final String name;
    private final int i;

    public TestC2SPacket(int i, String name) {
        this.i = i;
        this.name = name;
    }

    @SuppressWarnings("unused")
    public static TestC2SPacket deserialize(Object[] objects) {
        return new TestC2SPacket((int) objects[0], (String) objects[1]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{i, name};
    }

    @Override
    public void handleOnServer(Server server) {
        try {
            server.getPlayer(name).move(i);
            PacketManager.sendS2C(new InfoS2CPacket(name +  " bewegt sich " + i + " Felder"), PacketManager.Restriction.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new InfoS2CPacket("hi"), PacketManager.Restriction.named("F", "C"), Throwable::printStackTrace);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
