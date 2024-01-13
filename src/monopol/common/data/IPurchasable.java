package monopol.common.data;

import java.io.Serializable;

public interface IPurchasable extends IField, Serializable {
    void setOwner(String owner);
    String getOwner();
    String getName();
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
    String keyText(int line);

    String valueText(int line);
}
