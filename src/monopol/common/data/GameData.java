package monopol.common.data;

import monopol.common.core.Monopoly;

import java.util.ArrayList;

public class GameData {
    private int freeParkingAmount = 1000+500+100+50+20+10+5+1;

    public void setup() {
        freeParkingAmount = 0;
    }

    public void addFreeParking(int amount) {
        freeParkingAmount += amount;
    }

    public int getFreeParkingAmount() {
        return freeParkingAmount;
    }

    public int freeParking() {
        int value = freeParkingAmount;
        freeParkingAmount = 0;
        return value;
    }
}
