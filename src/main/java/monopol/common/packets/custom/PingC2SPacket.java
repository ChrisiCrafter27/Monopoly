package monopol.common.packets.custom;

import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.C2SPacket;
import monopol.common.packets.PacketManager;
import monopol.server.Server;

import java.net.Socket;

public class PingC2SPacket extends C2SPacket<PingC2SPacket> {
    private final long time;
    private final boolean back;

    public PingC2SPacket() {
        time = System.currentTimeMillis();
        back = false;
    }

    public PingC2SPacket(long time, boolean back) {
        this.time = time;
        this.back = back;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeLong(time);
        writer.writeBool(back);
    }

    public static PingC2SPacket deserialize(DataReader reader) {
        return new PingC2SPacket(reader.readLong(), reader.readBool());
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        if(back) {
            server.ping(source, System.currentTimeMillis() - time);
        } else {
            PacketManager.sendS2C(new PingS2CPacket(time, true), source, Throwable::printStackTrace);
        }
    }
}
