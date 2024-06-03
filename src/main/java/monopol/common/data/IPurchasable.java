package monopol.common.data;

import monopol.common.utils.Triplet;

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
    void setSpecialRent(boolean specialRent);
    boolean getSpecialRent();
    int getRent(Triplet<Integer, Integer, Integer> diceResult, boolean considerSpecialRent);
    String keyText(int line);
    String valueText(int line);

    default String getOwnerNotNull() {
        return getOwner() == null ? "" : getOwner();
    }
    default void copyOf(IPurchasable other) {
        setOwner(other.getOwner());
        setSpecialRent(other.getSpecialRent());
        if(other.isMortgaged()) mortgage();
        else unmortgage();
        while (getLevel() < other.getLevel()) upgrade();
        while (getLevel() > other.getLevel()) downgrade();
    }
}
