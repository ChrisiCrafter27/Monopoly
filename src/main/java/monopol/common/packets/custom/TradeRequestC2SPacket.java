package monopol.common.packets.custom;

import monopol.common.core.Monopoly;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.packets.C2SPacket;
import monopol.common.packets.PacketManager;
import monopol.server.Server;
import monopol.server.events.Events;

import java.net.Socket;

public class TradeRequestC2SPacket extends C2SPacket<TradeRequestC2SPacket> {
    private final IPurchasable purchasable;
    private final int money;

    public TradeRequestC2SPacket(IPurchasable purchasable, int money) {
        this.purchasable = purchasable;
        this.money = money;
    }

    public static TradeRequestC2SPacket deserialize(DataReader reader) {
        return new TradeRequestC2SPacket(Field.purchasables().get(reader.readInt()), reader.readInt());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeInt(Field.purchasables().indexOf(purchasable));
        writer.writeInt(money);
    }

    @Override
    public void handleOnServer(Server server, Socket source) {
        PacketManager.sendS2C(new TradeRequestS2CPacket(purchasable, money, server.getPlayer(source).getName()), PacketManager.named(purchasable.getOwnerNotNull()), Throwable::printStackTrace);
    }
}
