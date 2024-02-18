package monopol.common.packets.custom;

import monopol.common.packets.C2SPacket;
import monopol.server.Server;

public class RequestRejoinC2SPacket extends C2SPacket<RequestRejoinC2SPacket> {
    private final String name;

    public RequestRejoinC2SPacket(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public static RequestRejoinC2SPacket deserialize(Object[] objects) {
        return new RequestRejoinC2SPacket((String) objects[0]);
    }

    @Override
    public Object[] serialize() {
        return new Object[]{name};
    }

    @Override
    public void handleOnServer(Server server) {}

    public String name() {
        return name;
    }
}
