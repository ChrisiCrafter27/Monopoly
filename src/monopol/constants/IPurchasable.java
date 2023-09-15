package monopol.constants;

import java.io.Serializable;

public interface IPurchasable extends IField, Serializable {
    void setOwner(String owner);
    String getOwner();
}
