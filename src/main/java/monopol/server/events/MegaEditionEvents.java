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

public class MegaEditionEvents extends Events {
    protected boolean running = false;
    protected boolean requestTeleport = false;

    public MegaEditionEvents(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean mrMonopoly, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) {
        super(limitBusTickets, maxBusTickets, true, tempoDice, mrMonopoly, false, tripleTeleport, startMoney, losMoney, doubleLosMoney, freeParking, gainRentInPrison, buildEquable, reRollEventCardsAfterUse, buildRule, cardsRequiredForOneHouse, cardsRequiredForTwoHouses, cardsRequiredForThreeHouses, cardsRequiredForFourHouses, cardsRequiredForHotel, cardsRequiredForSkyscraper);
        if(!limitBuildings) System.err.println("Setting limitBuildings cannot be false!");
        if(megaBuildings) System.err.println("Setting megaBuildings cannot be true!");
    }

    @Override
    public int minPlayers() {
        return 3;
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
        Monopoly.INSTANCE.server().getSocketsServerSide().forEach(socket -> PacketManager.sendS2C(new GameEndS2CPacket(Monopoly.INSTANCE.server().gameData().getResult(players.stream().map(Monopoly.INSTANCE.server()::getPlayerServerSide).toList())), socket, Throwable::printStackTrace));
        players.clear();
        currentPlayer = -1;
        running = false;
    }

    @Override
    public void onGameStart(List<String> playerNames) {
        players.clear();
        players.addAll(playerNames);
        players.stream().map(Monopoly.INSTANCE.server()::getPlayerServerSide).forEach(player -> player.addMoney(startMoney));
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
        if(requestTeleport) PacketManager.sendS2C(new InfoS2CPacket("Klicke auf das Feld, auf das du möchtest."), PacketManager.named(player().getName()), Throwable::printStackTrace);
        PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
    }

    private boolean mayDoNextRound() {
        return CommunityCard.getCurrent() == null && BusCard.getCurrent() == null && EventCard.getCurrent() == null && (!buildEquable || BuildRule.buildingEquable(player())) && !requestTeleport && !hasToPayRent && diceRolled;
    }

    @Override
    public void onTryNextRound(String name) {
        if(Monopoly.INSTANCE.server().getPlayersServerSide().stream().map(Player::getName).noneMatch(name::equals)) {
            onNextRound();
        }
        if(name.equals(player().getName()) && mayDoNextRound()) {
            if(player().enoughMoney()) {
                if(player().getDoubles() > 0) currentPlayer--;
                onNextRound();
            } else onGoBankrupt();
        }
    }

    @Override
    public void onNextRound() {
        currentPlayer++;
        diceRolled = false;
        requestTeleport = false;
        hasToPayRent = false;
        Arrays.stream(TrainStation.values()).forEach(trainStation -> trainStation.setSpecialRent(false));
        Arrays.stream(Plant.values()).forEach(plant -> plant.setSpecialRent(false));
        if(player().inPrison()) onPrisonerRound();
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist am Zug."), PacketManager.all(), Throwable::printStackTrace);
        PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
    }

    @Override
    public void onPrisonerRound() {
        player().prisonRound();
    }

    @Override
    public void onDiceRoll(String name) {
        if(!name.equals(player().getName()) || diceRolled) return;
        diceRolled = true;
        PacketManager.sendS2C(new UpdateButtonsS2CPacket("", true, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);

        Random random = new Random();

        int dice1 = random.nextInt(6) + 1;
        int dice2 = random.nextInt(6) + 1;
        int dice3 = tempoDice ? random.nextInt(6) + 1 : -1;
        diceResult = new Triplet<>(dice1, dice2, dice3);
        int intResult = dice1 + dice2;
        switch (dice3) {
            case 1 -> intResult += 1;
            case 2 -> intResult += 2;
            case 3 -> intResult += 3;
            case 4,5 -> {
                if(mrMonopoly) {
                    int distToMaxRent = 0;
                    int maxRent = 0;
                    int dist = 0;
                    while ((!(Field.get(player().getPosition() + intResult + dist) instanceof IPurchasable purchasable) || purchasable.getOwner() != null) && dist < 52) {
                        dist++;
                        if(Field.get(player().getPosition() + intResult + dist) instanceof IPurchasable purchasable && purchasable.getRent(diceResult, false) > maxRent) {
                            distToMaxRent = dist;
                            maxRent = purchasable.getRent(diceResult, false);
                        }
                    }
                    if(dist < 52) intResult += dist;
                    else intResult += distToMaxRent;
                }
            }
            default -> {}
        }
        final int finalResult = intResult;
        new Thread(() -> {
            PacketManager.sendS2C(new RollDiceS2CPacket(dice1, dice2, dice3), PacketManager.all(), Throwable::printStackTrace);

            try {
                Thread.sleep(6000);
            } catch (InterruptedException ignored) {}
            if(!running) return;

            int oldPos = player().getPosition();
            if(dice1 == dice2 && !player().inPrison()) player().addDouble();
            else player().setDoubles(0);
            if(player().inPrison()) {
                if(dice1 == dice2) {
                    player().setInPrison(false);
                    player().move(finalResult);
                    PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist ausgebrochen."), PacketManager.all(), Throwable::printStackTrace);
                } else if (player().prisonRounds() >= 3) {
                    onPaySurety(player().getName(), true);
                }
            } else if(tripleTeleport && dice1 == dice2 && dice1 == dice3 && dice1 < 4) {
                player().setDoubles(0);
                requestTeleport = true;
                PacketManager.sendS2C(new InfoS2CPacket("Klicke auf das Feld, auf das du möchtest."), PacketManager.named(player().getName()), Throwable::printStackTrace);
                PacketManager.sendS2C(new UpdateButtonsS2CPacket("", true, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
                return;
            } else if(player().getDoubles() >= 3) {
                player().setDoubles(0);
                player().setInPrison(true);
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " muss ins Gefängnis."), PacketManager.all(), Throwable::printStackTrace);
            } else {
                player().move(finalResult);
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich."), PacketManager.all(), Throwable::printStackTrace);
            }

            try {
                Thread.sleep((player().inPrison()) ? 0 : (finalResult + 1) * 250L);
            } catch (InterruptedException ignored) {}
            if(!running) return;

            if(oldPos > player().getPosition() && !player().inPrison()) onPassedLos();
            if(dice3 == 5) onGetBusCard();
            onArrivedAtField();
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
        PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
    }

    @Override
    public void onPayRent(String name) {
        if(name.equals(player().getName()) && hasToPayRent && !requestTeleport && diceRolled) {
            IPurchasable purchasable = (IPurchasable) Field.fields().get(player().getPosition());
            Monopoly.INSTANCE.server().getPlayerServerSide(purchasable.getOwner()).addMoney(purchasable.getRent(diceResult, true));
            player().contractMoney(purchasable.getRent(diceResult, true));
            purchasable.setSpecialRent(false);
            hasToPayRent = false;
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onEventCardAction(String player, String action) {
        EventCard card = EventCard.getCurrent();
        if(card != null && card.actions().containsKey(action) && player.equals(player().getName())) {
            card.actions().get(action).act(Monopoly.INSTANCE.server(), player());
            EventCard.setCurrent(null);
            if(reRollEventCardsAfterUse) EventCard.resetUnused();
            PacketManager.sendS2C(new EventCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), EventCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onCommunityCardAction(String player, String action) {
        CommunityCard card = CommunityCard.getCurrent();
        if(card != null && card.actions().containsKey(action) && player.equals(player().getName())) {
            card.actions().get(action).act(Monopoly.INSTANCE.server(), player());
            CommunityCard.setCurrent(null);
            if(reRollEventCardsAfterUse) CommunityCard.resetUnused();
            PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onPaySurety(String name, boolean hasTo) {
        if(name.equals(player().getName()) && player().inPrison()) {
            if(player().getPrisonCards() > 0) player().removePrisonCard();
            else if(player().getMoney() > 50 || hasTo) {
                player().contractMoney(50);
                Monopoly.INSTANCE.server().gameData().addFreeParking(50);
            } else return;
            player().setInPrison(false);
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist wieder frei."), PacketManager.all(), Throwable::printStackTrace);
            if(!hasTo) PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onMortgage(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && !requestTeleport && diceRolled) {
            if(purchasable.isMortgaged()) {
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " kauft " + purchasable.getName() + " zurück."), PacketManager.all(), Throwable::printStackTrace);
                player().contractMoney(purchasable.getMortgage());
                purchasable.unmortgage();
            } else {
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " verkauft " + purchasable.getName() + " an die Bank."), PacketManager.all(), Throwable::printStackTrace);
                player().addMoney(purchasable.getMortgage());
                purchasable.mortgage();
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onGetBusCard() {
        if(BusCard.unusedSize() == 0 && !limitBusTickets) BusCard.resetUnused();
        if(BusCard.getCurrent() != null) {
            BusCard.enqueue();
        } else {
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " zieht eine Busfahrkarte."), PacketManager.all(), Throwable::printStackTrace);
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
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " nimmt eine Busfahrkarte."), PacketManager.all(), Throwable::printStackTrace);
            if(BusCard.getCurrent().expiration) {
                PacketManager.sendS2C(new InfoS2CPacket("Alle anderen Busfahrkarten verfallen."), PacketManager.all(), Throwable::printStackTrace);
                Monopoly.INSTANCE.server().getPlayersServerSide().forEach(Player::removeBusCards);
            }
            BusCard.setCurrent(null);
            Monopoly.INSTANCE.server().getPlayerServerSide(name).addBusCard();
            if(BusCard.deQueue()) onGetBusCard();
            else PacketManager.sendS2C(new BusCardS2CPacket(null, false, BusCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onBusDrive(String name, int target) {
        if(!requestTeleport && !diceRolled && name.equals(player().getName()) && player().getBusCards() > 0 && BusCard.onSide(player().getPosition(), target)) {
            diceRolled = true;
            PacketManager.sendS2C(new UpdateButtonsS2CPacket("", true, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
            int oldPos = player().getPosition();
            player().useBusCard();
            player().move(target == 0 ? target - oldPos + 52 : target - oldPos);
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich."), PacketManager.all(), Throwable::printStackTrace);
            if(oldPos > player().getPosition() && !player().inPrison()) onPassedLos();
            new Thread(()  ->  {
                try {
                    Thread.sleep((player().inPrison()) ? 0 : (target == 0 ? target - oldPos + 52 : target - oldPos + 1) * 250L);
                } catch (InterruptedException ignored) {}
                if(!running) return;
                onArrivedAtField();
            }).start();
        }
    }

    @Override
    public void onTeleport(String name, int target) {
        if(diceRolled && name.equals(player().getName()) && requestTeleport) {
            requestTeleport = false;
            int oldPos = player().getPosition();
            player().setPosition(target);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich."), PacketManager.all(), Throwable::printStackTrace);
            if(oldPos > player().getPosition() && !player().inPrison()) onPassedLos();
            onArrivedAtField();
        }
    }

    @Override
    public void onUpgrade(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && buildRule.valid(player(), purchasable) && !requestTeleport && diceRolled) {
            if(purchasable instanceof Street street && (!requiredCards.valid(player(), street) || (buildEquable && BuildRule.buildingEquable(street) == BuildRule.EqualBuildingResult.OKAY) || (street.getLevel() == street.getMaxLevel() - 1 && !megaBuildings))) return;
            if(purchasable instanceof TrainStation && !megaBuildings) return;
            if(purchasable.upgrade()) {
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " wertet " + purchasable.getName() + " auf."), PacketManager.all(), Throwable::printStackTrace);
                player().contractMoney(purchasable.getUpgradeCost());
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onDowngrade(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && buildRule.valid(player(), purchasable) && !requestTeleport && diceRolled) {
            if(purchasable instanceof Street street && buildEquable && BuildRule.buildingEquable(street) == BuildRule.EqualBuildingResult.OKAY) return;
            if(purchasable.downgrade()) {
                PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " wertet " + purchasable.getName() + " ab."), PacketManager.all(), Throwable::printStackTrace);
                player().addMoney(purchasable.getUpgradeCost() / 2);
            }
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onPurchaseCard(String name, IPurchasable purchasable) {
        if(name.equals(player().getName()) && purchasable.getOwner() == null && !requestTeleport && diceRolled) {
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " kauft " + purchasable.getName() + "."), PacketManager.all(), Throwable::printStackTrace);
            purchasable.setOwner(name);
            player().contractMoney(purchasable.getPrice());
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateButtonsS2CPacket(player().getName(), diceRolled, hasToPayRent, player().inPrison(), mayDoNextRound(), requestTeleport, buildRule, requiredCards, player()), PacketManager.all(), Throwable::printStackTrace);
        }
    }

    @Override
    public void onArrivedAtLos() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " kommt auf Los."), PacketManager.all(), Throwable::printStackTrace);
        player().addMoney(doubleLosMoney ? losMoney * 2 : losMoney);
    }

    @Override
    public void onArrivedAtAuction() {
        PacketManager.sendS2C(new InfoS2CPacket("Das Auktionshaus ist geschlossen."), PacketManager.all(), Throwable::printStackTrace);
    }

    @Override
    public void onArrivedAtBusCard() {
        onGetBusCard();
    }

    @Override
    public void onArrivedAtBirthday() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " hat Geburtstag und erhält 100€."), PacketManager.all(), Throwable::printStackTrace);
        player().addMoney(100);
    }

    @Override
    public void onArrivedAtPurchasable(IPurchasable purchasable) {
        if(purchasable.getOwner() != null && !purchasable.getOwnerNotNull().equals(player().getName()) && (rentInPrison || !Monopoly.INSTANCE.server().getPlayerServerSide(purchasable.getOwner()).inPrison())) hasToPayRent = true;
    }

    @Override
    public void onArrivedAtEventField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " zieht eine Ereigniskarte."), PacketManager.all(), Throwable::printStackTrace);
        EventCard card = EventCard.getUnused();
        EventCard.setCurrent(card);
        card.activate(player());
    }

    @Override
    public void onArrivedAtCommunityField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() + " zieht eine Gemeinschaftskarte."), PacketManager.all(), Throwable::printStackTrace);
        CommunityCard card = CommunityCard.getUnused();
        CommunityCard.setCurrent(card);
        card.activate(player());
    }

    @Override
    public void onArrivedAtFreeParking() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " parkt frei."), PacketManager.all(), Throwable::printStackTrace);
        player().addMoney(Monopoly.INSTANCE.server().gameData().parkForFree());
    }

    @Override
    public void onArrivedAtGoToPrisonField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " muss ins Gefängnis."), PacketManager.all(), Throwable::printStackTrace);
        player().setInPrison(true);
    }

    @Override
    public void onArrivedAtTaxField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " zahlt Steuern."), PacketManager.all(), Throwable::printStackTrace);
        int money = (int) (Math.min(player().getMoney() / 10d, 200d) + 0.5d);
        player().contractMoney(money);
        Monopoly.INSTANCE.server().gameData().addFreeParking(money);
    }

    @Override
    public void onArrivedAtAdditionalTaxField() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " zahlt Steuern."), PacketManager.all(), Throwable::printStackTrace);
        player().contractMoney(75);
        Monopoly.INSTANCE.server().gameData().addFreeParking(75);
    }

    @Override
    public void onArrivedAtPrisonField() {
        if(!player().inPrison()) PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist nur zu Besuch."), PacketManager.all(), Throwable::printStackTrace);
    }

    @Override
    public void onPassedLos() {
        if(player().getPosition() == Field.fields().indexOf(Corner.LOS)) return;
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " kommt über Los."), PacketManager.all(), Throwable::printStackTrace);
        player().addMoney(losMoney);
    }

    @Override
    public void onGoBankrupt() {
        PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " ist bankrott."), PacketManager.all(), Throwable::printStackTrace);
        new Thread(() -> {
            Monopoly.INSTANCE.server().gameData().saveBankruptPlayer(player());
            Field.purchasables().stream().filter(p -> player().getName().equals(p.getOwner())).forEach(p -> {
                p.setOwner(null);
                while(p.downgrade()) p.downgrade();
            });
            int result = JOptionPane.showOptionDialog(Monopoly.INSTANCE.parentComponent,  player().getName() + " ist bankrott!", "Bankrott", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, players.size() <= minPlayers() ? new Object[]{"Spiel beenden"} : new Object[]{"Spiel beenden", "Weiterspielen"}, null);
            if(result == 0) {
                onGameStop();
            } else {
                players.remove(player().getName());
                currentPlayer--;
                onNextRound();
            }
        }).start();
    }
}
