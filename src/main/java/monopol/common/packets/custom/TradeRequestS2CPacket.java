package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.*;
import monopol.common.packets.PacketManager;
import monopol.common.packets.S2CPacket;

import javax.swing.*;

public class TradeRequestS2CPacket extends S2CPacket<TradeRequestS2CPacket> {
    private final IPurchasable purchasable;
    private final int money;
    private final String source;

    public TradeRequestS2CPacket(IPurchasable purchasable, int money, String source) {
        this.purchasable = purchasable;
        this.money = money;
        this.source = source;
    }

    public static TradeRequestS2CPacket deserialize(DataReader reader) {
        return new TradeRequestS2CPacket(Field.purchasables().get(reader.readInt()), reader.readInt(), reader.readString());
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeInt(Field.purchasables().indexOf(purchasable));
        writer.writeInt(money);
        writer.writeString(source);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        if(purchasable.getOwnerNotNull().equals(client.player().getName())) {
            if(JOptionPane.showConfirmDialog(display, "Möchtest du " + purchasable.getName() + " für " + money + "€ an" + source + " verkaufen?", source + " handelt mit " + client.player().getName(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                PacketManager.sendC2S(new TradeAcceptC2SPacket(purchasable, money, source, client.player().getName()), client, Throwable::printStackTrace);
            }
        }
    }
}
