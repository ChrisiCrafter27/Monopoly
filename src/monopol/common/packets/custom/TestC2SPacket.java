package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.common.packets.PacketManager;
import monopol.server.Server;

public class TestC2SPacket extends C2SPacket<TestC2SPacket> {
    private final String text;

    public TestC2SPacket(String text) {
        this.text = text;
    }

    @SuppressWarnings("unused")
    public static TestC2SPacket deserialize(Object[] objects) {
        return new TestC2SPacket((String) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{text};
    }

    @Override
    public void handleOnServer(Server server) {
        PacketManager.sendS2C(new InfoS2CPacket(text), PacketManager.Restriction.all(), Throwable::printStackTrace);
        PacketManager.sendS2C(new InfoS2CPacket("hi"), PacketManager.Restriction.named("F", "C"), Throwable::printStackTrace);
    }
}