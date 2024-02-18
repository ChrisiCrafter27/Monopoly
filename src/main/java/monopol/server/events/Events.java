package monopol.server.events;

import monopol.common.core.Monopoly;
import monopol.common.data.IPurchasable;
import monopol.common.data.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Events {
    public final boolean limitBusTickets;
    public final int maxBusTickets;
    public final boolean limitBuildings;
    public final boolean tempoDice;
    public final boolean megaBuildings;
    public final boolean tripleTeleport;
    public final int startMoney;
    public final int losMoney;
    public final boolean doubleLosMoney;
    public final boolean freeParking;
    public final boolean gainRentInPrison;
    public final boolean buildEquable;
    public final boolean reRollEventCardsAfterUse;
    public final BuildRule buildRule;
    public final OwnedCardsOfColorGroup cardsRequiredForOneHouse;
    public final OwnedCardsOfColorGroup cardsRequiredForTwoHouses;
    public final OwnedCardsOfColorGroup cardsRequiredForThreeHouses;
    public final OwnedCardsOfColorGroup cardsRequiredForFourHouses;
    public final OwnedCardsOfColorGroup cardsRequiredForHotel;
    public final OwnedCardsOfColorGroup cardsRequiredForSkyscraper;

    protected final List<String> players = new ArrayList<>();
    protected int currentPlayer;
    protected boolean diceRolled;
    protected int diceResult;
    protected int dice1;
    protected int dice2;
    protected int dice3;
    protected boolean hasToPayRent;

    protected Events(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) {
        this.limitBusTickets = limitBusTickets;
        this.maxBusTickets = maxBusTickets;
        this.limitBuildings = limitBuildings;
        this.tempoDice = tempoDice;
        this.megaBuildings = megaBuildings;
        this.tripleTeleport = tripleTeleport;
        this.startMoney = startMoney;
        this.losMoney = losMoney;
        this.doubleLosMoney = doubleLosMoney;
        this.freeParking = freeParking;
        this.gainRentInPrison = gainRentInPrison;
        this.buildEquable = buildEquable;
        this.reRollEventCardsAfterUse = reRollEventCardsAfterUse;
        this.buildRule = buildRule;
        this.cardsRequiredForOneHouse = cardsRequiredForOneHouse;
        this.cardsRequiredForTwoHouses = cardsRequiredForTwoHouses;
        this.cardsRequiredForThreeHouses = cardsRequiredForThreeHouses;
        this.cardsRequiredForFourHouses = cardsRequiredForFourHouses;
        this.cardsRequiredForHotel = cardsRequiredForHotel;
        this.cardsRequiredForSkyscraper = cardsRequiredForSkyscraper;
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

    public abstract void onGameStop();
    public abstract void onGameStart(List<String> playerNames);
    public abstract void onRejoin();
    public abstract void onTryNextRound(String name);
    public abstract void onNextRound();
    public abstract void onPrisonerRound();
    public abstract void onDiceRoll(String name);
    public abstract void onGetBusCard();
    public abstract void onArrivedAtField();
    public abstract void onPayRent(String name);
    public abstract void onCommunityCardAction(String action);
    public abstract void onPaySurety(String name);
    public abstract void onTryMortgage();
    public abstract void onUpgrade();
    public abstract void onDowngrade();
    public abstract void onPurchaseCard();
    public abstract void onArrivedAtLos();
    public abstract void onArrivedAtAuction();
    public abstract void onArrivedAtBusPass();
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
}