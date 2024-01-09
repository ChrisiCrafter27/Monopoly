package monopol.data;

import monopol.rules.ColorGroup;

import java.io.Serializable;

public interface IPurchasable extends IField, Serializable {
    void setOwner(String owner);
    String getOwner();
    String name();
    int price();
    int getMortgage();
    boolean mortgaged();
    int getLevel();
    boolean upgrade();
    boolean downgrade();
    boolean isMortgaged();
    boolean mortgage();
    boolean unmortgage();
    int getRent(int diceResult);
}
