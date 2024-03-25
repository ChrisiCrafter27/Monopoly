package monopol.common.packets.custom;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.message.DisconnectReason;
import monopol.common.packets.C2SPacket;
import monopol.server.Server;

import java.net.Socket;

public class DisconnectC2SPacket extends C2SPacket<DisconnectC2SPacket> {
    private final DisconnectReason reason;

    public DisconnectC2SPacket(DisconnectReason reason) {
        this.reason = reason;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeEnum(reason);
    }

    @SuppressWarnings("unused")
    public static DisconnectC2SPacket deserialize(DataReader reader) {
        return new DisconnectC2SPacket(reader.readEnum(DisconnectReason.class));
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        server.kick(source, reason);
    }
}
