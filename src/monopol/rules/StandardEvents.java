package monopol.rules;

import java.rmi.RemoteException;

public class StandardEvents extends Events{
    public StandardEvents(boolean limitBusTickets, int maxBusTickets, boolean limitBuildings, boolean tempoDice, boolean megaBuildings, boolean tripleTeleport, int startMoney, int losMoney, boolean doubleLosMoney, boolean freeParking, boolean gainRentInPrison, boolean buildEquable, boolean reRollEventCardsAfterUse, BuildRule buildRule, OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper) throws RemoteException {
        super(limitBusTickets, maxBusTickets, limitBuildings, tempoDice, megaBuildings, tripleTeleport, startMoney, losMoney, doubleLosMoney, freeParking, gainRentInPrison, buildEquable, reRollEventCardsAfterUse, buildRule, cardsRequiredForOneHouse, cardsRequiredForTwoHouses, cardsRequiredForThreeHouses, cardsRequiredForFourHouses, cardsRequiredForHotel, cardsRequiredForSkyscraper);
    }

    @Override
    public void onGameStart() {

    }

    @Override
    public void onNextRound() {

    }

    @Override
    public void onPrisonerRound() {

    }

    @Override
    public void onDiceRoll() {

    }

    @Override
    public void onTryMortgage() {

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

    }

    @Override
    public void onArrivedAtBirthday() {

    }

    @Override
    public void onArrivedAtStreetOrFacility() {

    }

    @Override
    public void onArrivedAtEventField() {

    }

    @Override
    public void onArrivedAtCommunityField() {

    }

    @Override
    public void onArrivedAtFreeParking() {

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

    @Override
    public void printSth(String value) {

    }
}
