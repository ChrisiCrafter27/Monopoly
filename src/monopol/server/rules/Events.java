package monopol.server.rules;

public abstract class Events {
    public final boolean LIMIT_BUS_TICKETS;
    public final int MAX_BUS_TICKETS;
    public final boolean LIMIT_BUILDINGS;
    public final boolean TEMPO_DICE;
    public final boolean MEGA_BUILDINGS;
    public final boolean TRIPLE_TELEPORT;
    public final int START_MONEY;
    public final int LOS_MONEY;
    public final boolean DOUBLE_LOS_MONEY;
    public final boolean FREE_PARKING;
    public final boolean GAIN_RENT_IN_PRISON;
    public final boolean BUILD_EQUABLE;
    public final boolean RE_ROLL_EVENT_CARDS_AFTER_USE;
    public final BuildRule BUILD_RULE;
    public final OwnedCardsOfColorGroup CARDS_REQUIRED_FOR_ONE_HOUSE;
    public final OwnedCardsOfColorGroup CARDS_REQUIRED_FOR_TWO_HOUSES;
    public final OwnedCardsOfColorGroup CARDS_REQUIRED_FOR_THREE_HOUSES;
    public final OwnedCardsOfColorGroup CARDS_REQUIRED_FOR_FOUR_HOUSES;
    public final OwnedCardsOfColorGroup CARDS_REQUIRED_FOR_HOTEL;
    public final OwnedCardsOfColorGroup CARDS_REQUIRED_FOR_SKYSCRAPER;

    protected Events(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) {
        LIMIT_BUS_TICKETS = limitBusTickets;
        MAX_BUS_TICKETS = maxBusTickets;
        LIMIT_BUILDINGS = limitBuildings;
        TEMPO_DICE = tempoDice;
        MEGA_BUILDINGS = megaBuildings;
        TRIPLE_TELEPORT = tripleTeleport;
        START_MONEY = startMoney;
        LOS_MONEY = losMoney;
        DOUBLE_LOS_MONEY = doubleLosMoney;
        FREE_PARKING = freeParking;
        GAIN_RENT_IN_PRISON = gainRentInPrison;
        BUILD_EQUABLE = buildEquable;
        RE_ROLL_EVENT_CARDS_AFTER_USE = reRollEventCardsAfterUse;
        BUILD_RULE = buildRule;
        CARDS_REQUIRED_FOR_ONE_HOUSE = cardsRequiredForOneHouse;
        CARDS_REQUIRED_FOR_TWO_HOUSES = cardsRequiredForTwoHouses;
        CARDS_REQUIRED_FOR_THREE_HOUSES = cardsRequiredForThreeHouses;
        CARDS_REQUIRED_FOR_FOUR_HOUSES = cardsRequiredForFourHouses;
        CARDS_REQUIRED_FOR_HOTEL = cardsRequiredForHotel;
        CARDS_REQUIRED_FOR_SKYSCRAPER = cardsRequiredForSkyscraper;
    }

    public abstract void onGameStart();
    public abstract void onNextRound();
    public abstract void onPrisonerRound();
    public abstract void onDiceRoll();
    public abstract void onTryMortgage();
    public abstract void onPurchaseBuilding();
    public abstract void onSellBuilding();
    public abstract void onPurchaseCard();
    public abstract void onArrivedAtLos();
    public abstract void onArrivedAtAuction();
    public abstract void onArrivedAtBusPass();
    public abstract void onArrivedAtBirthday();
    public abstract void onArrivedAtStreetOrFacility();
    public abstract void onArrivedAtEventField();
    public abstract void onArrivedAtCommunityField();
    public abstract void onArrivedAtFreeParking();
    public abstract void onArrivedAtGoToPrisonField();
    public abstract void onArrivedAtTaxField();
    public abstract void onArrivedAtAdditionalTaxField();
    public abstract void onPassedLos();
    public abstract void onOfferTrade();
    public abstract void onAcceptTrade();
    public abstract void onGoBankrupt();
}