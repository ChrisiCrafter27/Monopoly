package monopol.common.data;

import monopol.common.core.Monopoly;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {
    private String name;
    private int money = 0;
    private int prisonCards = 0;
    private int busCards = 1;
    private int position = 0;
    private Color color = Color.WHITE;
    private boolean inPrison = false;
    private int prisonRounds = 0;
    private int doubles = 0;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public int getMoney() {
        return money;
    }
    public int getPrisonCards() {
        return prisonCards;
    }
    public int getBusCards() {
        return busCards;
    }
    public int getPosition() {
        return position;
    }
    public Color getColor() {
        return color;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void addMoney(int money) {
        this.money += money;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public void contractMoney(int money) {
        this.money -= money;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public boolean enoughMoney() {
        return money > 0;
    }
    public void addPrisonCard() {
        prisonCards++;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public void removePrisonCard() {
        prisonCards--;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public void addBusCard() {
        busCards++;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public void useBusCard() {
        busCards--;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public void removeBusCards() {
        busCards = 0;
        Monopoly.INSTANCE.server().updatePlayerData();
    }
    public void setPosition(int position) {
        this.position = position;
        Monopoly.INSTANCE.server().updatePosition(false);
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public void move(int range) {
        position += range;
        while (position >= 52) position -= 52;
        Monopoly.INSTANCE.server().updatePosition(true);
    }
    public boolean inPrison() {
        return inPrison;
    }
    public void setInPrison(boolean inPrison) {
        this.inPrison = inPrison;
        if(inPrison) setPosition(Field.fields().indexOf(Corner.GEFAENGNIS));
        else {
            prisonRounds = 0;
            Monopoly.INSTANCE.server().updatePosition(false);
        }
    }
    public void prisonRound() {
        prisonRounds++;
    }
    public int prisonRounds() {
        return prisonRounds;
    }
    public void setDoubles(int doubles) {
        this.doubles = doubles;
    }
    public void addDouble() {
        doubles++;
    }
    public int getDoubles() {
        return doubles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return money == player.money && prisonCards == player.prisonCards && busCards == player.busCards && position == player.position && inPrison == player.inPrison && prisonRounds == player.prisonRounds && doubles == player.doubles && Objects.equals(name, player.name) && Objects.equals(color, player.color);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", money=" + money +
                ", prisonCards=" + prisonCards +
                ", busCards=" + busCards +
                ", position=" + position +
                ", color=" + color +
                ", inPrison=" + inPrison +
                ", prisonRounds=" + prisonRounds +
                ", doubles=" + doubles +
                '}';
    }
}
