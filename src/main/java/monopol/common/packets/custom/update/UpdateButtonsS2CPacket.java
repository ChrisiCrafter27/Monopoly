package monopol.common.packets.custom.update;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.*;
import monopol.common.packets.S2CPacket;
import monopol.server.events.BuildRule;
import monopol.server.events.RequiredCardsOfColorGroup;

import java.util.ArrayList;
import java.util.List;

public class UpdateButtonsS2CPacket extends S2CPacket<UpdateButtonsS2CPacket> {
    private final String currentPlayer;
    private final boolean diceRolled, hasToPayRent, inPrison, ready;
    private final List<IPurchasable> purchasables;

    public UpdateButtonsS2CPacket(String currentPlayer, boolean diceRolled, boolean hasToPayRent, boolean inPrison, boolean ready, BuildRule buildRule, RequiredCardsOfColorGroup requiredCards, Player player) {
        this(currentPlayer, diceRolled, hasToPayRent, inPrison, ready, buildRule.apply(player).stream().filter(p -> !(p instanceof TrainStation) || requiredCards.megaBuildings()).filter(p -> !(p instanceof Street street) || p.getOwner() == null || (requiredCards.valid(player, street) && (!requiredCards.buildEquable() || BuildRule.buildingEquable(street) != BuildRule.EqualBuildingResult.OKAY))).toList());
    }

    public UpdateButtonsS2CPacket(String currentPlayer, boolean diceRolled, boolean hasToPayRent, boolean inPrison, boolean ready, List<IPurchasable> purchasables) {
        this.currentPlayer = currentPlayer;
        this.diceRolled = diceRolled;
        this.hasToPayRent = hasToPayRent;
        this.inPrison = inPrison;
        this.ready = ready;
        this.purchasables = purchasables;
    }

    public static UpdateButtonsS2CPacket deserialize(DataReader reader) {
        List<IPurchasable> list = new ArrayList<>();
        list.addAll(reader.readList(ArrayList::new, r -> r.readEnum(Street.class)));
        list.addAll(reader.readList(ArrayList::new, r -> r.readEnum(TrainStation.class)));
        list.addAll(reader.readList(ArrayList::new, r -> r.readEnum(Plant.class)));
        return new UpdateButtonsS2CPacket(reader.readString(), reader.readBool(), reader.readBool(), reader.readBool(), reader.readBool(), list);
    }

    @Override
    public void serialize(DataWriter writer) {
        writer.writeList(purchasables.stream().filter(purchasable -> purchasable instanceof Street).map(purchasable -> (Street) purchasable).toList(), DataWriter::writeEnum);
        writer.writeList(purchasables.stream().filter(purchasable -> purchasable instanceof TrainStation).map(purchasable -> (TrainStation) purchasable).toList(), DataWriter::writeEnum);
        writer.writeList(purchasables.stream().filter(purchasable -> purchasable instanceof Plant).map(purchasable -> (Plant) purchasable).toList(), DataWriter::writeEnum);
        writer.writeString(currentPlayer);
        writer.writeBool(diceRolled);
        writer.writeBool(hasToPayRent);
        writer.writeBool(inPrison);
        writer.writeBool(ready);
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        display.buttonsPane.update(currentPlayer, diceRolled, hasToPayRent, inPrison, ready, purchasables);
    }
}
