package monopol.server.events;

import monopol.common.core.Monopoly;
import monopol.common.data.IPurchasable;
import monopol.common.data.Player;
import monopol.common.utils.Triplet;

import java.util.ArrayList;
import java.util.List;

public abstract class Events {
    public final boolean limitBusTickets;
    public final int maxBusTickets;
    public final boolean limitBuildings; //noch nicht implementiert
    public final boolean tempoDice;
    public final boolean megaBuildings;
    public final boolean tripleTeleport; //noch nicht implementiert
    public final int startMoney;
    public final int losMoney;
    public final boolean doubleLosMoney;
    public final boolean freeParking;
    public final boolean rentInPrison;
    public final boolean buildEquable;
    public final boolean reRollEventCardsAfterUse;
    public final BuildRule buildRule;
    public final RequiredCardsOfColorGroup requiredCards;

    protected final List<String> players = new ArrayList<>();
    protected int currentPlayer;
    protected boolean diceRolled;
    protected Triplet<Integer, Integer, Integer> diceResult;
    protected int dice1;
    protected int dice2;
    protected int dice3;
    protected boolean hasToPayRent;

    protected Events(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean rentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) {
        this.limitBusTickets = limitBusTickets;
        this.maxBusTickets = maxBusTickets;
        this.limitBuildings = true;
        this.tempoDice = tempoDice;
        this.megaBuildings = megaBuildings;
        this.tripleTeleport = tripleTeleport;
        this.startMoney = startMoney;
        this.losMoney = losMoney;
        this.doubleLosMoney = doubleLosMoney;
        this.freeParking = freeParking;
        this.rentInPrison = rentInPrison;
        this.buildEquable = buildEquable;
        this.reRollEventCardsAfterUse = reRollEventCardsAfterUse;
        this.buildRule = buildRule;
        this.requiredCards = new RequiredCardsOfColorGroup(cardsRequiredForOneHouse, cardsRequiredForTwoHouses, cardsRequiredForThreeHouses, cardsRequiredForFourHouses, cardsRequiredForHotel, cardsRequiredForSkyscraper, buildEquable, megaBuildings);
    }

    protected Player player() {
        Player player = null;
        while (!players.isEmpty()) {
            if(currentPlayer + 1 > players.size()) currentPlayer = 0;
            String name = players.get(currentPlayer);
            player = Monopoly.INSTANCE.server().getPlayerServerSide(name);
            if(player == null) players.remove(name);
            else break;
        }
        return player;
    }

    public boolean diceRolled() {
        return diceRolled;
    }

    public abstract int minPlayers();
    public abstract void prepareGame();

    public abstract void onGameStop();
    public abstract void onGameStart(List<String> playerNames);
    public abstract void onRejoin();
    public abstract void onTryNextRound(String name);
    public abstract void onNextRound();
    public abstract void onPrisonerRound();
    public abstract void onDiceRoll(String name);
    public abstract void onGetBusCard();
    public abstract void onTakeBusCard(String name);
    public abstract void onBusDrive(String name, int target);
    public abstract void onArrivedAtField();
    public abstract void onPayRent(String name);
    public abstract void onEventCardAction(String player, String action);
    public abstract void onCommunityCardAction(String player, String action);
    public abstract void onPaySurety(String name, boolean hasTo);
    public abstract void onMortgage(String name, IPurchasable purchasable);
    public abstract void onUpgrade(String name, IPurchasable purchasable);
    public abstract void onDowngrade(String name, IPurchasable purchasable);
    public abstract void onPurchaseCard(String name, IPurchasable purchasable);
    public abstract void onArrivedAtLos();
    public abstract void onArrivedAtAuction();
    public abstract void onArrivedAtBusCard();
    public abstract void onArrivedAtBirthday();
    public abstract void onArrivedAtPurchasable(IPurchasable purchasable);
    public abstract void onArrivedAtEventField();
    public abstract void onArrivedAtCommunityField();
    public abstract void onArrivedAtFreeParking();
    public abstract void onArrivedAtGoToPrisonField();
    public abstract void onArrivedAtTaxField();
    public abstract void onArrivedAtAdditionalTaxField();
    public abstract void onArrivedAtPrisonField();
    public abstract void onPassedLos();
    public abstract void onOfferTrade();
    public abstract void onAcceptTrade();
    public abstract void onGoBankrupt();

    @FunctionalInterface
    public interface Factory<T extends Events> {
        T create(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper);
        default T copyOf(Events events) {
            return create(events.limitBusTickets, events.maxBusTickets, events.limitBuildings, events.tempoDice, events.megaBuildings, events.tripleTeleport, events.startMoney, events.losMoney, events.doubleLosMoney, events.freeParking, events.rentInPrison, events.buildEquable, events.reRollEventCardsAfterUse, events.buildRule, events.requiredCards.cardsRequiredForOneHouse(), events.requiredCards.cardsRequiredForTwoHouses(), events.requiredCards.cardsRequiredForThreeHouses(), events.requiredCards.cardsRequiredForFourHouses(), events.requiredCards.cardsRequiredForHotel(), events.requiredCards.cardsRequiredForSkyscraper());
        }
    }
}