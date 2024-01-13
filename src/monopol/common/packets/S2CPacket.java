package monopol.common.packets;

public abstract class S2CPacket<T extends S2CPacket<T>> extends Packet<T> {
    @Override
    public void handle(Side side) {
        if (side instanceof ClientSide clientSide) {
            handleOnClient(clientSide);
        } else throw new IllegalStateException();
    }

    public abstract void handleOnClient(ClientSide side);
}
