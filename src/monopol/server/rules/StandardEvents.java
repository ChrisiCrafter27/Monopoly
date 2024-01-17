package monopol.server.rules;

import monopol.common.Player;
import monopol.common.core.Monopoly;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.InfoS2CPacket;
import monopol.common.packets.custom.RollDiceC2SPacket;
import monopol.common.packets.custom.RollDiceS2CPacket;
import monopol.common.packets.custom.update.UpdatePlayerDataS2CPacket;

import java.rmi.RemoteException;
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
        players.clear();
        players.addAll(playerNames);
        currentPlayer = -1;
        running = true;
        onNextRound();
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
            case 4 -> onBusCard();
            default -> {}
        }
        player().move(result);
        final int finalResult = result;
        new Thread(() -> {
            PacketManager.sendS2C(new RollDiceS2CPacket(result1, result2, result3), PacketManager.Restriction.all(), Throwable::printStackTrace);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            if(!running) return;
            PacketManager.sendS2C(new InfoS2CPacket(player().getName() +  " bewegt sich " + finalResult + " Felder"), PacketManager.Restriction.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.Restriction.all(), Throwable::printStackTrace);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            if(!running) return;
            onNextRound();
        }).start();
    }

    @Override
    public void onTryMortgage() {

    }

    @Override
    public void onBusCard() {
        if(new Random().nextInt(10) == 0){
            try {
                Monopoly.INSTANCE.server().getPlayers().forEach(player -> player.substractBusfahrkarten(player.getBusfahrkarten()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        player().addBusfahrkarten(1);
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
}
