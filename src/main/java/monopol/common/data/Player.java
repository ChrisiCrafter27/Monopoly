package monopol.common.data;

import monopol.common.core.Monopoly;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {
    private String name;
    private int money = 1000;
    private int prisonCards = 0;
    private int busCards = 0;
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
        return money >= 0;
    }
    public void addPrisonCard() {
        prisonCards++;
    }
    public boolean usePrisonCards() {
        if(prisonCards > 0) {
            prisonCards--;
            return true;
        }
        return false;
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
            Monopoly.INSTANCE.server().updatePosition(true);
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Player player = (Player) object;
        return money == player.money && prisonCards == player.prisonCards && busCards == player.busCards && position == player.position && inPrison == player.inPrison && prisonRounds == player.prisonRounds && Objects.equals(name, player.name) && Objects.equals(color, player.color);
    }
}
