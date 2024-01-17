package monopol.common;

import monopol.common.core.Monopoly;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int money = 1000;
    private int gefängniskarten = 0;
    private int busfahrkarten = 0;
    private int position = 0;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public int getMoney() {
        return money;
    }
    public int getGefaengniskarten(){ return gefängniskarten; }
    public int getBusfahrkarten(){ return busfahrkarten; }
    public int getPosition() {
        return position;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void addMoney(int money) {
        this.money += money;
    }
    public void contractMoney(int money) {
        this.money -= money;
    }
    public boolean enoughMoney() {
        return money >= 0;
    }
    public void addGefängniskarten(int karten){ gefängniskarten = gefängniskarten+karten; }
    public void substractGefängniskarten(int karten){ gefängniskarten = gefängniskarten-karten; }
    public void addBusfahrkarten(int karten){ busfahrkarten = busfahrkarten+karten; }
    public void substractBusfahrkarten(int karten){ busfahrkarten = busfahrkarten-karten; }
    public void setPosition(int position) {
        this.position = position;
        Monopoly.INSTANCE.server().updatePosition();
    }
    public void move(int range) {
        position += range;
        while (position >= 52) position -= 52;
        Monopoly.INSTANCE.server().updatePosition();
    }
}
