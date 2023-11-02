package monopol.data;

import java.io.Serializable;

public interface IPurchasable extends IField, Serializable {
    void setOwner(String owner);
    String getOwner();

    // TODO Hi

    /*
    int getX();
    int getY();
    Direction getDirection();
     */
}
