package monopol.message.packets;

import monopol.message.Packet;

public class ToastPacket extends Packet<ToastPacket> {
    private final String toast;

    public ToastPacket(String toast) {
        this.toast = toast;
    }

    public static ToastPacket deserialize(Object[] objects) {
        return new ToastPacket((String) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{toast};
    }

    @Override
    public void handle() {
        System.out.println("handel");
        System.out.println(toast);
    }
}
