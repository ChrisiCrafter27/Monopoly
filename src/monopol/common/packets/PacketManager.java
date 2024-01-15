package monopol.common.packets;

import monopol.client.Client;
import monopol.common.message.MessageType;
import monopol.common.core.Monopoly;
import monopol.common.message.Message;
import monopol.server.Server;
import monopol.common.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketManager {
    public static void sendS2C(S2CPacket<?> packet, Function<Player, Boolean> targets, Consumer<Exception> catcher) {
        try {
            Server server = Monopoly.INSTANCE.server();
            for (Player player : server.getPlayers()) {
                if (targets.apply(player)) Message.send(new Message(new Object[]{packet.getClass().getName(), packet.serialize()}, MessageType.PACKET), server.players.get(player));
            }
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void sendC2S(C2SPacket<?> packet, Client client, Consumer<Exception> catcher) {
        try {
            Message.send(new Message(new Object[]{packet.getClass().getName(), packet.serialize()}, MessageType.PACKET), client.socket());
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void handle(Message message, Side side) {
        if(message.getMessage().length > 1) {
            try {
                Object[] objects = message.getMessage();
                Class<?> clazz = Class.forName((String) objects[0]);
                List<Method> methods = Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getName().equals("deserialize"))
                        .filter(method ->
                                Modifier.isPublic(method.getModifiers())
                                && Modifier.isStatic(method.getModifiers())
                                && method.getParameterCount() == 1
                                && method.getParameterTypes()[0] == Object[].class
                                && method.getReturnType() == clazz
                        )
                        .toList();
                if (methods.size() == 1) {
                    Method method = methods.get(0);
                    ((Packet<?>) method.invoke(null, new Object[]{((List<?>) objects[1]).toArray()})).handle(side);
                } else throw new IllegalStateException("missing deserialization method in " + clazz);
            } catch (InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Restriction {
        public static Function<Player, Boolean> all() {
            return serverPlayer -> true;
        }

        public static Function<Player, Boolean> named(String... names) {
            return serverPlayer -> Arrays.stream(names).anyMatch(name -> name.equals(serverPlayer.getName()));
        }
    }
}
