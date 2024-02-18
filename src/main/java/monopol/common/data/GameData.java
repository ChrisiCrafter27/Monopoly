package monopol.common.data;

import monopol.common.core.Monopoly;

public class GameData {
    private int freeParkingAmount = 0;

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
}
