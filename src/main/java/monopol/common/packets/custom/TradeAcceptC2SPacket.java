package monopol.common.packets.custom;

import monopol.common.core.Monopoly;
import monopol.common.data.*;
import monopol.common.packets.C2SPacket;
import monopol.common.packets.PacketManager;
import monopol.server.Server;

import java.net.Socket;

public class TradeAcceptC2SPacket extends C2SPacket<TradeAcceptC2SPacket> {
    private final IPurchasable purchasable;
    private final int money;
    private final String source;
    private final String target;

    public TradeAcceptC2SPacket(IPurchasable purchasable, int money, String source, String target) {
        this.purchasable = purchasable;
        this.money = money;
        this.source = source;
        this.target = target;
    }

    public static TradeAcceptC2SPacket deserialize(DataReader reader) {
        return new TradeAcceptC2SPacket(Field.purchasables().get(reader.readInt()), reader.readInt(), reader.readString(), reader.readString());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeInt(Field.purchasables().indexOf(purchasable));
        writer.writeInt(money);
        writer.writeString(source);
        writer.writeString(target);
    }

    @Override
    public void handleOnServer(Server server, Socket socket) {
        if(purchasable.getOwnerNotNull().equals(target)) {
            Player targetPlayer = server.getPlayerServerSide(target);
            Player sourcePlayer = server.getPlayerServerSide(source);
            purchasable.setOwner(source);
            sourcePlayer.contractMoney(money);
            targetPlayer.addMoney(money);
            PacketManager.sendS2C(new InfoS2CPacket(target + " hat " + source + " " + purchasable.getName() + " für " + money + "€ gegeben"), PacketManager.all(), Throwable::printStackTrace);
        }
    }
}
