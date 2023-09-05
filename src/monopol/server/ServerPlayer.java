package monopol.server;

import java.io.Serializable;

public class ServerPlayer implements Serializable {
    private String name;
    private int money;

    public ServerPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public int getMoney() {
        return money;
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
}
