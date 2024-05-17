package monopol.common.packets;

import monopol.common.data.DataWriter;

public interface Packet<T extends Packet<T>> {
    void serialize(DataWriter writer);
    void handle(Side side);
}
