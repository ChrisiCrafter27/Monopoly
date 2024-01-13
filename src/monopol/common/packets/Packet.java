package monopol.common.packets;

import monopol.client.Client;
import monopol.common.message.Message;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A packet is a container of a message. All attributes need to be
 * (de)serialized to be transported over a network. To do so, you
 * need to create the following deserialization method: <b>
 * {@code public static T deserialize(Object[]args)}</b>. This method is
 * executed via java reflect (see {@link PacketManager#handle(Message, Side)}). Packets
 * are sent via {@link PacketManager#sendS2C(S2CPacket, Function, Consumer)}
 * and {@link PacketManager#sendC2S(C2SPacket, Client, Consumer)}. Once
 * received, the {@link Packet#handle(Side)} method is executed.
 *
 * @param <T> the packets class
 */
public interface Packet<T extends Packet<T>> {
    Object[] serialize();
    void handle(Side side);
}
