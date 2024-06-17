package monopol.common.data;

import monopol.common.utils.Triplet;

import java.io.Serializable;

public interface IPurchasable extends IField {
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
    default void write(DataWriter writer) {
        writer.writeString(getOwner());
        writer.writeBool(getSpecialRent());
        writer.writeBool(isMortgaged());
        writer.writeInt(getLevel());
    }
    default void read(DataReader reader) {
        setOwner(reader.readString());
        setSpecialRent(reader.readBool());
        if(reader.readBool()) mortgage();
        else unmortgage();
        int level = reader.readInt();
        while (getLevel() < level) upgrade();
        while (getLevel() > level) downgrade();
    }
}
