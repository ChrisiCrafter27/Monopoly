package monopol.common.data;

import monopol.common.core.Monopoly;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GameData {
    private int freeParkingAmount = 0;
    private final HashMap<String, Integer> deadPlayers = new HashMap<>();

    public void addFreeParking(int amount) {
        freeParkingAmount += amount;
        Monopoly.INSTANCE.server().updateFreeParking();
    }

    public int getFreeParkingAmount() {
        return freeParkingAmount;
    }

    public int parkForFree() {
        int value = freeParkingAmount;
        freeParkingAmount = 0;
        Monopoly.INSTANCE.server().updateFreeParking();
        return value;
    }

    public void saveBankruptPlayer(Player player) {
        deadPlayers.put(player.getName(), calculateWealth(player));
    }

    private int calculateWealth(Player player) {
        AtomicInteger money = new AtomicInteger(player.getMoney());
        Field.purchasables().forEach(p -> {
            if(player.getName().equals(p.getOwner()) && !p.isMortgaged()) {
                while (p.downgrade()) {
                    money.addAndGet(p.getUpgradeCost() / 2);
                }
                money.addAndGet(p.getMortgage() / 2);
            }
        });
        return money.get();
    }

    public HashMap<String, Integer> getResult(List<Player> activePlayers) {
        activePlayers.forEach(this::saveBankruptPlayer);
        return new HashMap<>(deadPlayers);
    }
}
