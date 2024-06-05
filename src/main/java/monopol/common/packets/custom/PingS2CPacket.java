package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.packets.PacketManager;
import monopol.common.packets.S2CPacket;

public class PingS2CPacket extends S2CPacket<PingS2CPacket> {
    private final long time;
    private final boolean back;

    public PingS2CPacket() {
        time = System.currentTimeMillis();
        back = false;
    }

    public PingS2CPacket(long time, boolean back) {
        this.time = time;
        this.back = back;
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeLong(time);
        writer.writeBool(back);
    }

    public static PingS2CPacket deserialize(DataReader reader) {
        return new PingS2CPacket(reader.readLong(), reader.readBool());
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if(back) {
            client.ping(System.currentTimeMillis() - time);
        } else {
            PacketManager.sendC2S(new PingC2SPacket(time, true), client, Throwable::printStackTrace);
        }
    }
}
