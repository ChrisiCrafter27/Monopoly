package monopol.server.events;

import monopol.common.core.Monopoly;
import monopol.common.data.*;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.CommunityCardS2CPacket;
import monopol.common.packets.custom.InfoS2CPacket;
import monopol.common.packets.custom.RollDiceS2CPacket;
import monopol.common.packets.custom.update.UpdateButtonsS2CPacket;
import monopol.common.packets.custom.update.UpdatePurchasablesS2CPacket;

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
    }

    @Override
    public void onGameStart(List<String> playerNames) {
        CommunityCard.setCurrent(null);
        CommunityCard.resetUnused();
        Field.purchasables().forEach(purchasable -> purchasable.setOwner(null));
        players.clear();
        players.addAll(playerNames);
        currentPlayer = new Random().nextInt(players.size()) - 1;
        running = true;
        onNextRound();
    }

    @Override
    public void onRejoin() {
        if(CommunityCard.getCurrent() != null) CommunityCard.getCurrent().activate(player());
        else PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.Restriction.all(), Throwable::printStackTrace);
    }

    private boolean mayDoNextRound() {
        return CommunityCard.getCurrent() == null && !hasToPayRent && diceRolled;
    }

    @Override
    public void onTryNextRound(String name) {
        if(Monopoly.INSTANCE.server().getPlayersServerSide().stream().map(Player::getName).noneMatch(name::equals) || (name.equals(player().getName()) && mayDoNextRound())) {
            if(player().getDoubles() > 0) currentPlayer--;
            onNextRound();
        }
    }

    @Override
    public void onNextRound() {
        currentPlayer++;
        diceRolled = false;
        hasToPayRent = false;
        if(player().inPrison()) onPrisonerRound();
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist am Zug"), PacketManager.Restriction.all(), Throwable::printStackTrace);
        PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
    }

    @Override
    public void onPrisonerRound() {
        player().prisonRound();
    }

    @Override
    public void onDiceRoll(String name) {
        if(!name.equals(player().getName()) || diceRolled()) return;
        diceRolled = true;
        PacketManager.sendS2C(new UpdateButtonsS2CPacket("", true, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);

        Random random = new Random();

        dice1 = random.nextInt(6) + 1;
        dice2 = random.nextInt(6) + 1;
        dice3 = tempoDice ? random.nextInt(6) + 1 : -1;
        diceResult = dice1 + dice2;
        switch (dice3) {
            case 1 -> diceResult += 1;
            case 2 -> diceResult += 2;
            case 3 -> diceResult += 3;
            default -> {}
        }
        final int finalResult = diceResult;
        new Thread(() -> {
            PacketManager.sendS2C(new RollDiceS2CPacket(dice1, dice2, dice3), PacketManager.Restriction.all(), Throwable::printStackTrace);
            try {
                Thread.sleep(6000);
            } catch (InterruptedException ignored) {}
            if(!running) return;

            if(dice1 == dice2 && !player().inPrison()) player().addDouble();
            else player().setDoubles(0);
            if(player().inPrison()) {
                if(dice1 == dice2) {
                    player().move(finalResult);
                    PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist ausgebrochen"), PacketManager.Restriction.all(), Throwable::printStackTrace);
                } else if (player().prisonRounds() >= 3) {
                    onPaySurety(player().getName());
                    player().move(finalResult);
                }
            } else if(player().getDoubles() >= 3) {
                player().setDoubles(0);
                player().setInPrison(true);
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " muss ins Gefängnis"), PacketManager.Restriction.all(), Throwable::printStackTrace);
            } else {
                player().move(finalResult);
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich"), PacketManager.Restriction.all(), Throwable::printStackTrace);
            }

            if(dice3 == 5) onGetBusCard(player().getName());
            Monopoly.INSTANCE.server().updatePlayerData();
            try {
                Thread.sleep((player().inPrison()) ? 0 : (finalResult + 1) * 250L);
            } catch (InterruptedException ignored) {}
            if(!running) return;
            onArrivedAtField();
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
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
        else if(field == Corner.LOS) onArrivedAtLos();
        else if(field == Corner.GEFAENGNIS) onArrivedAtPrisonField();
        else if(field == Corner.FREIPARKEN) onArrivedAtFreeParking();
        else if(field == Corner.INSGEFAENGNIS) onArrivedAtGoToPrisonField();
        else if(field == Field.EINKOMMENSSTEUER) onArrivedAtTaxField();
        else if(field == Field.ZUSATZSTEUER) onArrivedAtAdditionalTaxField();
    }

    @Override
    public void onPayRent(String name) {
        if(name.equals(player().getName()) && hasToPayRent) {
            IPurchasable purchasable = (IPurchasable) Field.fields().get(player().getPosition());
            Monopoly.INSTANCE.server().getPlayerServerSide(purchasable.getOwner()).addMoney(purchasable.getRent(diceResult));
            player().contractMoney(purchasable.getRent(diceResult));
            hasToPayRent = false;
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
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
    public void onPaySurety(String name) {
        if(name.equals(player().getName()) && player().inPrison()) {
            player().contractMoney(50);
            player().setInPrison(false);
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist wieder frei"), PacketManager.Restriction.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onMortgage(String name, IPurchasable purchasable) {
        if(name.equals(player().getName())) {
            if(purchasable.isMortgaged()) {
                player().contractMoney(purchasable.getMortgage());
                purchasable.unmortgage();
            } else {
                player().addMoney(purchasable.getMortgage());
                purchasable.mortgage();
            }
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onGetBusCard(String name) {
        if(new Random().nextInt(10) == 0) {
            Monopoly.INSTANCE.server().getPlayersServerSide().forEach(Player::removeBusCards);
        }
        Monopoly.INSTANCE.server().getPlayerServerSide(name).addBusCard();
    }

    @Override
    public void onUpgrade(String name, IPurchasable purchasable) {
        if(name.equals(player().getName())) {
            if(purchasable.upgrade()) {
                player().contractMoney(purchasable.getUpgradeCost());
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.Restriction.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onDowngrade(String name, IPurchasable purchasable) {
        if(name.equals(player().getName())) {
            if(purchasable.downgrade()) {
                player().addMoney(purchasable.getUpgradeCost() / 2);
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.Restriction.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onPurchaseCard(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && purchasable.getOwner() == null) {
            if(purchasable.downgrade()) {
                player().addMoney(purchasable.getUpgradeCost() / 2);
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.Restriction.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound()), PacketManager.Restriction.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onArrivedAtLos() {

    }

    @Override
    public void onArrivedAtAuction() {

    }

    @Override
    public void onArrivedAtBusPass() {
        onGetBusCard(player().getName());
    }

    @Override
    public void onArrivedAtBirthday() {

    }

    @Override
    public void onArrivedAtPurchasable(IPurchasable purchasable) {
        if(!purchasable.getOwner().isEmpty() && !purchasable.getOwner().equals(player().getName())) hasToPayRent = true;
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
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " parkt frei"), PacketManager.Restriction.all(), Throwable::printStackTrace);
        player().addMoney(Monopoly.INSTANCE.server().gameData().parkForFree());
    }

    @Override
    public void onArrivedAtGoToPrisonField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " muss ins Gefängnis"), PacketManager.Restriction.all(), Throwable::printStackTrace);
        player().setInPrison(true);
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
    public void onGoBankrupt(String name) {

    }
}
