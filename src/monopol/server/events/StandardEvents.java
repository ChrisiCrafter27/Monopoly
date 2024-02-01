package monopol.server.events;

import monopol.common.data.Player;
import monopol.common.core.Monopoly;
import monopol.common.data.Field;
import monopol.common.data.IField;
import monopol.common.data.IPurchasable;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.CommunityCardS2CPacket;
import monopol.common.packets.custom.InfoS2CPacket;
import monopol.common.packets.custom.RollDiceC2SPacket;
import monopol.common.packets.custom.RollDiceS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StandardEvents extends Events {
    private boolean running = false;

    public StandardEvents(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) {
        super(limitBusTickets, maxBusTickets, limitBuildings, tempoDice, megaBuildings, tripleTeleport, startMoney, losMoney, doubleLosMoney, freeParking, gainRentInPrison, buildEquable, reRollEventCardsAfterUse, buildRule, cardsRequiredForOneHouse, cardsRequiredForTwoHouses, cardsRequiredForThreeHouses, cardsRequiredForFourHouses, cardsRequiredForHotel, cardsRequiredForSkyscraper);
    }

    @Override
    public void onGameStop() {
        players.clear();
        currentPlayer = -1;
        running = false;
        RollDiceC2SPacket.request(null);
    }

    @Override
    public void onGameStart(List<String> playerNames) {
        CommunityCard.setCurrent(null);
        CommunityCard.resetUnused();
        players.clear();
        players.addAll(playerNames);
        currentPlayer = new Random().nextInt(players.size()) - 1;
        running = true;
        onNextRound();
    }

    @Override
    public void onRejoin() {
        if(CommunityCard.getCurrent() != null)
            CommunityCard.getCurrent().activate(player());
        else PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.Restriction.all(), Throwable::printStackTrace);
    }

    @Override
    public void onTryNextRound() {
        if(CommunityCard.getCurrent() == null) onNextRound();
    }

    @Override
    public void onNextRound() {
        currentPlayer++;
        Player player = player();
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist am Zug"), PacketManager.Restriction.all(), Throwable::printStackTrace);
        RollDiceC2SPacket.request(player.getName());
    }

    @Override
    public void onPrisonerRound() {

    }

    @Override
    public void onDiceRoll() {
        RollDiceC2SPacket.request(null);
        Random random = new Random();

        int result1 = random.nextInt(6) + 1; //dice 1
        int result2 = random.nextInt(6) + 1; //dice 2
        int result3 = tempoDice ? random.nextInt(6) + 1 : -1; //dice 3
        int result = result1 + result2;
        switch (result3) {
            case 1 -> result += 1;
            case 2 -> result += 2;
            case 3 -> result += 3;
            case 4 -> onGetBusCard();
            default -> {}
        }
        final int finalResult = result;
        new Thread(() -> {
            PacketManager.sendS2C(new RollDiceS2CPacket(result1, result2, result3), PacketManager.Restriction.all(), Throwable::printStackTrace);
            try {
                Thread.sleep(6000);
            } catch (InterruptedException ignored) {}
            if(!running) return;
            player().move(finalResult);
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich " + finalResult + " Felder"), PacketManager.Restriction.all(), Throwable::printStackTrace);
            Monopoly.INSTANCE.server().updatePlayerData();
            try {
                Thread.sleep(finalResult * 250);
            } catch (InterruptedException ignored) {}
            if(!running) return;
            onArrivedAtField();
            onNextRound();
        }).start();
    }

    @Override
    public void onArrivedAtField() {
        IField field = Field.get(player().getPosition());
        if(field instanceof IPurchasable purchasable) onArrivedAtPurchasable(purchasable);
        else if(field == Field.GEMEINSCHAFTSFELD) onArrivedAtCommunityField();
        else if(field == Field.EREIGNISFELD) onArrivedAtEventField();
        else if(field == Field.AUKTION) onArrivedAtAuction();
        else if(field == Field.BUSFAHRKARTE) onArrivedAtBusPass();
        else if(field == Field.GESCHENK) onArrivedAtBirthday();
        else if(field == Field.LOS) onArrivedAtLos();
        else if(field == Field.GEFAENGNIS) onArrivedAtPrisonField();
        else if(field == Field.FREIPARKEN) onArrivedAtFreeParking();
        else if(field == Field.INSGEFAENGNIS) onArrivedAtGoToPrisonField();
        else if(field == Field.EINKOMMENSSTEUER) onArrivedAtTaxField();
        else if(field == Field.ZUSATZSTEUER) onArrivedAtAdditionalTaxField();
        else throw new IllegalStateException("Player arrived at unregistered field");
    }

    @Override
    public void onCommunityCardAction(String action) {
        CommunityCard card = CommunityCard.getCurrent();
        if(card != null && card.actions().containsKey(action)) {
            card.actions().get(action).act(Monopoly.INSTANCE.server(), player());
            CommunityCard.setCurrent(null);
            PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onTryMortgage() {

    }

    @Override
    public void onGetBusCard() {
        if(new Random().nextInt(10) == 0) {
            Monopoly.INSTANCE.server().getPlayersServerSide().forEach(Player::removeBusCards);
        }
        player().addBusCard();
    }

    @Override
    public void onPurchaseBuilding() {

    }

    @Override
    public void onSellBuilding() {

    }

    @Override
    public void onPurchaseCard() {

    }

    @Override
    public void onArrivedAtLos() {

    }

    @Override
    public void onArrivedAtAuction() {

    }

    @Override
    public void onArrivedAtBusPass() {
        onGetBusCard();
    }

    @Override
    public void onArrivedAtBirthday() {

    }

    @Override
    public void onArrivedAtPurchasable(IPurchasable purchasable) {

    }

    @Override
    public void onArrivedAtEventField() {

    }

    @Override
    public void onArrivedAtCommunityField() {
        CommunityCard card = CommunityCard.getUnused();
        CommunityCard.setCurrent(card);
        card.activate(player());
    }

    @Override
    public void onArrivedAtFreeParking() {
        player().addMoney(Monopoly.INSTANCE.server().gameData().parkForFree());
    }

    @Override
    public void onArrivedAtGoToPrisonField() {

    }

    @Override
    public void onArrivedAtTaxField() {

    }

    @Override
    public void onArrivedAtAdditionalTaxField() {

    }

    @Override
    public void onArrivedAtPrisonField() {

    }

    @Override
    public void onPassedLos() {

    }

    @Override
    public void onOfferTrade() {

    }

    @Override
    public void onAcceptTrade() {

    }

    @Override
    public void onGoBankrupt() {

    }
}
