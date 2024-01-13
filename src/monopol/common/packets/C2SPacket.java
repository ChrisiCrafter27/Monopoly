package monopol.common.packets;

public abstract class C2SPacket<T extends C2SPacket<T>> implements Packet<T> {
    @Override
    public void handle(Side side) {
        if (side instanceof ServerSide serverSide) {
            handleOnServer(serverSide);
        } else throw new IllegalStateException();
    }

    public abstract void handleOnServer(ServerSide side);
}
