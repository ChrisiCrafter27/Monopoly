package monopol.data;

import monopol.core.Monopoly;

import java.util.ArrayList;

public class GameData {
    private ArrayList<String> playerList = new ArrayList<>();
    private String currentPlayer = "";
    private int freeParkingAmount = 1000+500+100+50+20+10+5+1;

    public void setup() {
        playerList = Monopoly.INSTANCE.getAllPlayerNamesOfOwnServer();
        if(playerList.isEmpty()) return;
        currentPlayer = playerList.get(0);
        freeParkingAmount = 0;
    }

    public boolean checkForErrors() {
        if(!playerList.equals(Monopoly.INSTANCE.getAllPlayerNamesOfOwnServer())) return true;
        //TODO: other conditions
        return false;
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

    public void nextPlayer() {
        if(playerList.indexOf(currentPlayer) == playerList.size() - 1) currentPlayer = playerList.get(0); else currentPlayer = playerList.get(playerList.indexOf(currentPlayer) + 1);
    }
}
