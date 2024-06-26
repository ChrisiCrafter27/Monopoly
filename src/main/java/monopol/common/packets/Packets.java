package monopol.common.packets;

import monopol.common.data.DataReader;
import monopol.common.packets.custom.*;
import monopol.common.packets.custom.update.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Packets {
    private static final Map<Class<? extends Packet<?>>, Function<DataReader, ? extends Packet<?>>> PACKETS = new HashMap<>();

    private static <T extends Packet<?>> void register(Class<T> clazz, Function<DataReader, T> function) {
        PACKETS.put(clazz, function);
    }
    
    public static void register() {
        register(AskRejoinS2CPacket.class, AskRejoinS2CPacket::deserialize);
        register(ButtonC2SPacket.class, ButtonC2SPacket::deserialize);
        register(CommunityCardC2SPacket.class, CommunityCardC2SPacket::deserialize);
        register(CommunityCardS2CPacket.class, CommunityCardS2CPacket::deserialize);
        register(DisconnectC2SPacket.class, DisconnectC2SPacket::deserialize);
        register(DisconnectS2CPacket.class, DisconnectS2CPacket::deserialize);
        register(InfoS2CPacket.class, InfoS2CPacket::deserialize);
        register(NameS2CPacket.class, NameS2CPacket::deserialize);
        register(PingC2SPacket.class, PingC2SPacket::deserialize);
        register(PingS2CPacket.class, PingS2CPacket::deserialize);
        register(RejoinStatusS2CPacket.class, RejoinStatusS2CPacket::deserialize);
        register(RequestRejoinC2SPacket.class, RequestRejoinC2SPacket::deserialize);
        register(RollDiceS2CPacket.class, RollDiceS2CPacket::deserialize);
        register(StartS2CPacket.class, StartS2CPacket::deserialize);
        register(BusCardS2CPacket.class, BusCardS2CPacket::deserialize);
        register(BusCardC2SPacket.class, BusCardC2SPacket::deserialize);
        register(EventCardS2CPacket.class, EventCardS2CPacket::deserialize);
        register(EventCardC2SPacket.class, EventCardC2SPacket::deserialize);
        register(GameEndS2CPacket.class, GameEndS2CPacket::deserialize);
        register(TradeRequestC2SPacket.class, TradeRequestC2SPacket::deserialize);
        register(TradeRequestS2CPacket.class, TradeRequestS2CPacket::deserialize);
        register(TradeAcceptC2SPacket.class, TradeAcceptC2SPacket::deserialize);

        register(UpdateButtonsS2CPacket.class, UpdateButtonsS2CPacket::deserialize);
        register(UpdateFreeParkingS2CPacket.class, UpdateFreeParkingS2CPacket::deserialize);
        register(UpdatePlayerDataS2CPacket.class, UpdatePlayerDataS2CPacket::deserialize);
        register(UpdatePositionS2CPacket.class, UpdatePositionS2CPacket::deserialize);
        register(UpdatePurchasablesS2CPacket.class, UpdatePurchasablesS2CPacket::deserialize);
    }

    public static Packet<?> deserialize(Class<?> clazz, DataReader reader) {
        Function<DataReader, ? extends Packet<?>> packet = PACKETS.get(clazz);
        if(packet == null) return null;
        else return packet.apply(reader);
    }
}
