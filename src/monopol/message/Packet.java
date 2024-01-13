package monopol.message;

import monopol.client.Client;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A packet is a container of a message. All attributes need to be
 * (de)serialized to be transported over a network. To do so, you
 * need to create the following deserialization method: <b>
 * {@code public static T deserialize(Object[]args)}</b>. Packets
 * are sent via {@link PacketManager#sendS2C(Packet, Function, Consumer)}
 * and {@link PacketManager#sendC2S(Packet, Client, Consumer)}. Once
 * received, the {@link Packet#handle()} method is executed.
 *
 * @param <T> the packets class
 */
public abstract class Packet<T extends Packet<T>> {
    public abstract Object[] serialize();
    public abstract void handle();
}
