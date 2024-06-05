package monopol.common.packets.customs;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

import java.net.Socket;

public class RequestRejoinC2SPacket extends C2SPacket<RequestRejoinC2SPacket> {
    private final String name;

    public RequestRejoinC2SPacket(String name) {
        this.name = name;
    }

    public static RequestRejoinC2SPacket deserialize(DataReader reader) {
        return new RequestRejoinC2SPacket(reader.readString());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeString(name);
    }

    @Override
    public void handleOnServer(Server server, Socket source) {}

    public String name() {
        return name;
    }
}
