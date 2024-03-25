package monopol.common.packets;

import monopol.client.Client;
import monopol.common.core.Monopoly;
import monopol.common.data.DataReader;
import monopol.common.message.Message;
import monopol.server.Server;
import monopol.common.data.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketManager {
    public static void sendS2C(S2CPacket<?> packet, Function<Player, Boolean> targets, Consumer<Exception> catcher) {
        try {
            Server server = Monopoly.INSTANCE.server();
            for (Player player : server.getPlayersServerSide()) {
                if(targets.apply(player)) new Message(packet).send(server.socket(player));
            }
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void sendS2C(S2CPacket<?> packet, Socket target, Consumer<Exception> catcher) {
        try {
            new Message(packet).send(target);
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void sendC2S(C2SPacket<?> packet, Client client, Consumer<Exception> catcher) {
        try {
            new Message(packet).send(client.socket());
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void handle(Message message, Side side) {
        try {
            packet(message).handle(side);
        } catch (InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    public static Packet<?> packet(Message message) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        return (Packet<?>) Packets.deserialize(message.getClazz(), new DataReader(message.getContent()));
    }

    public static Function<Player, Boolean> all() {
        return serverPlayer -> true;
    }

    public static Function<Player, Boolean> named(String... names) {
        return serverPlayer -> Arrays.stream(names).anyMatch(name -> name.equals(serverPlayer.getName()));
    }
}
