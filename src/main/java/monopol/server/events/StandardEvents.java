package monopol.server.events;

import monopol.common.core.Monopoly;
import monopol.common.data.*;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.*;
import monopol.common.packets.custom.update.UpdateButtonsS2CPacket;
import monopol.common.packets.custom.update.UpdatePurchasablesS2CPacket;
import monopol.common.utils.Triplet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StandardEvents extends Events {
    private boolean running = false;

    public StandardEvents(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) {
        super(limitBusTickets, maxBusTickets, limitBuildings, tempoDice, megaBuildings, tripleTeleport, startMoney, losMoney, doubleLosMoney, freeParking, gainRentInPrison, buildEquable, reRollEventCardsAfterUse, buildRule, cardsRequiredForOneHouse, cardsRequiredForTwoHouses, cardsRequiredForThreeHouses, cardsRequiredForFourHouses, cardsRequiredForHotel, cardsRequiredForSkyscraper);
    }

    @Override
    public int minPlayers() {
        return 1;
    }

    @Override
    public void prepareGame() {
        EventCard.setCurrent(null);
        EventCard.resetUnused();
        CommunityCard.setCurrent(null);
        CommunityCard.resetUnused();
        BusCard.setCurrent(null);
        BusCard.setSize(maxBusTickets);
        BusCard.resetUnused();
        Field.purchasables().forEach(purchasable -> purchasable.setOwner(null));
        Arrays.stream(TrainStation.values()).forEach(trainStation -> trainStation.setSpecialRent(false));
        Arrays.stream(Plant.values()).forEach(plant -> plant.setSpecialRent(false));
    }

    @Override
    public void onGameStop() {
        players.clear();
        currentPlayer = -1;
        running = false;
    }

    @Override
    public void onGameStart(List<String> playerNames) {
        players.clear();
        players.addAll(playerNames);
        currentPlayer = new Random().nextInt(players.size()) - 1;
        running = true;
        onNextRound();
    }

    @Override
    public void onRejoin() {
        if(CommunityCard.getCurrent() != null) CommunityCard.getCurrent().activate(player());
        else PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
        if(EventCard.getCurrent() != null) EventCard.getCurrent().activate(player());
        else PacketManager.sendS2C(new EventCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), EventCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
        if(BusCard.getCurrent() != null) BusCard.getCurrent().activate(player());
        else PacketManager.sendS2C(new BusCardS2CPacket(null, false, BusCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
    }

    private boolean mayDoNextRound() {
        return CommunityCard.getCurrent() == null && BusCard.getCurrent() == null && EventCard.getCurrent() == null && !hasToPayRent && diceRolled;
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
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist am Zug"), PacketManager.all(), Throwable::printStackTrace);
        PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
    }

    @Override
    public void onPrisonerRound() {
        player().prisonRound();
    }

    @Override
    public void onDiceRoll(String name) {
        if(!name.equals(player().getName()) || diceRolled) return;
        diceRolled = true;
        PacketManager.sendS2C(new UpdateButtonsS2CPacket("", true, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);

        Random random = new Random();

        dice1 = random.nextInt(6) + 1;
        dice2 = random.nextInt(6) + 1;
        dice3 = tempoDice ? random.nextInt(6) + 1 : -1;
        diceResult = new Triplet<>(dice1, dice2, dice3);
        int intResult = dice1 + dice2;
        switch (dice3) {
            case 1 -> intResult += 1;
            case 2 -> intResult += 2;
            case 3 -> intResult += 3;
            default -> {}
        }
        final int finalResult = intResult;
        new Thread(() -> {
            PacketManager.sendS2C(new RollDiceS2CPacket(dice1, dice2, dice3), PacketManager.all(), Throwable::printStackTrace);
            try {
                Thread.sleep(6000);
            } catch (InterruptedException ignored) {}
            if(!running) return;

            if(dice1 == dice2 && !player().inPrison()) player().addDouble();
            else player().setDoubles(0);
            if(player().inPrison()) {
                if(dice1 == dice2) {
                    player().move(finalResult);
                    PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist ausgebrochen"), PacketManager.all(), Throwable::printStackTrace);
                } else if (player().prisonRounds() >= 3) {
                    onPaySurety(player().getName());
                }
            } else if(player().getDoubles() >= 3) {
                player().setDoubles(0);
                player().setInPrison(true);
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " muss ins Gefängnis"), PacketManager.all(), Throwable::printStackTrace);
            } else {
                player().move(finalResult);
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich"), PacketManager.all(), Throwable::printStackTrace);
            }

            try {
                Thread.sleep((player().inPrison()) ? 0 : (finalResult + 1) * 250L);
            } catch (InterruptedException ignored) {}
            if(!running) return;
            if(dice3 == 5) onGetBusCard();
            onArrivedAtField();
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }).start();
    }

    @Override
    public void onArrivedAtField() {
        IField field = Field.get(player().getPosition());
        if(field instanceof IPurchasable purchasable) onArrivedAtPurchasable(purchasable);
        else if(field == Field.GEMEINSCHAFTSFELD) onArrivedAtCommunityField();
        else if(field == Field.EREIGNISFELD) onArrivedAtEventField();
        else if(field == Field.AUKTION) onArrivedAtAuction();
        else if(field == Field.BUSFAHRKARTE) onArrivedAtBusCard();
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
            Monopoly.INSTANCE.server().getPlayerServerSide(purchasable.getOwner()).addMoney(purchasable.getRent(diceResult, true));
            player().contractMoney(purchasable.getRent(diceResult, true));
            purchasable.setSpecialRent(false);
            hasToPayRent = false;
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onEventCardAction(String player, String action) {
        EventCard card = EventCard.getCurrent();
        if(card != null && card.actions().containsKey(action) && player.equals(player().getName())) {
            card.actions().get(action).act(Monopoly.INSTANCE.server(), player());
            EventCard.setCurrent(null);
            PacketManager.sendS2C(new EventCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), EventCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onCommunityCardAction(String player, String action) {
        CommunityCard card = CommunityCard.getCurrent();
        if(card != null && card.actions().containsKey(action) && player.equals(player().getName())) {
            card.actions().get(action).act(Monopoly.INSTANCE.server(), player());
            CommunityCard.setCurrent(null);
            PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onPaySurety(String name) {
        if(name.equals(player().getName()) && player().inPrison()) {
            player().contractMoney(50);
            player().setInPrison(false);
            Monopoly.INSTANCE.server().gameData().addFreeParking(50);
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist wieder frei"), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
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
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onGetBusCard() {
        if(BusCard.unusedSize() == 0 && !limitBusTickets) BusCard.resetUnused();
        if(BusCard.getCurrent() != null) {
            BusCard.enqueue();
        } else {
            BusCard card = BusCard.getUnused();
            if(card != null) {
                if(BusCard.unusedSize() == 0 && !limitBusTickets) BusCard.resetUnused();
                card.activate(player());
                BusCard.setCurrent(card);
            }
        }
    }

    @Override
    public void onTakeBusCard(String name) {
        if(name.equals(player().getName()) && BusCard.getCurrent() != null) {
            if(BusCard.getCurrent().expiration) Monopoly.INSTANCE.server().getPlayersServerSide().forEach(Player::removeBusCards);
            BusCard.setCurrent(null);
            Monopoly.INSTANCE.server().getPlayerServerSide(name).addBusCard();
            if(BusCard.deQueue()) onGetBusCard();
            else PacketManager.sendS2C(new BusCardS2CPacket(null, false, BusCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onUpgrade(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && buildRule.valid(player(), purchasable)) {
            if(purchasable.upgrade()) {
                player().contractMoney(purchasable.getUpgradeCost());
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onDowngrade(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && buildRule.valid(player(), purchasable)) {
            if(purchasable.downgrade()) {
                player().addMoney(purchasable.getUpgradeCost() / 2);
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onPurchaseCard(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && purchasable.getOwner().isEmpty()) {
            purchasable.setOwner(name);
            player().contractMoney(purchasable.getPrice());
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), buildRule, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onArrivedAtLos() {

    }

    @Override
    public void onArrivedAtAuction() {

    }

    @Override
    public void onArrivedAtBusCard() {
        onGetBusCard();
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
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " zog eine Ereigniskarte"), PacketManager.all(), Throwable::printStackTrace);
        EventCard card = EventCard.getUnused();
        EventCard.setCurrent(card);
        card.activate(player());
    }

    @Override
    public void onArrivedAtCommunityField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " zog eine Gemeinschaftskarte"), PacketManager.all(), Throwable::printStackTrace);
        CommunityCard card = CommunityCard.getUnused();
        CommunityCard.setCurrent(card);
        card.activate(player());
    }

    @Override
    public void onArrivedAtFreeParking() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " parkt frei"), PacketManager.all(), Throwable::printStackTrace);
        player().addMoney(Monopoly.INSTANCE.server().gameData().parkForFree());
    }

    @Override
    public void onArrivedAtGoToPrisonField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " muss ins Gefängnis"), PacketManager.all(), Throwable::printStackTrace);
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
