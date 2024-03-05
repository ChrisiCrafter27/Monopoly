package monopol.common.data;

import java.io.Serializable;

public interface IPurchasable extends IField, Serializable {
    void setOwner(String owner);
    String getOwner();
    String getName();
    int getPrice();
    int getMortgage();
    boolean mortgaged();
    int getLevel();
    int getMaxLevel();
    int getUpgradeCost();
    boolean upgrade();
    boolean downgrade();
    boolean isMortgaged();
    void mortgage();
    void unmortgage();
    int getRent(int diceResult);
    String keyText(int line);
    String valueText(int line);

    default void copyOf(IPurchasable other) {
        setOwner(other.getOwner());
        if(other.isMortgaged()) mortgage();
        else unmortgage();
        while (getLevel() < other.getLevel()) upgrade();
        while (getLevel() > other.getLevel()) downgrade();
    }
}
