package monopol.message;

import monopol.client.Client;
import monopol.core.Monopoly;
import monopol.server.Server;
import monopol.server.ServerPlayer;

import java.util.function.Consumer;
import java.util.function.Function;

public class PacketManager {
    public static void sendS2C(Packet<?> packet, Function<ServerPlayer, Boolean> targets, Consumer<Exception> catcher) {
        try {
            Server server = Monopoly.INSTANCE.server();
            for (ServerPlayer player : server.getServerPlayers()) {
                if (targets.apply(player)) Message.send(packet.toMessage(), server.serverPlayers.get(player));
            }
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void sendC2S(Packet<?> packet, Client client, Consumer<Exception> catcher) {
        try {
            Message.send(packet.toMessage(), client.socket());
        } catch (Exception e) {
            catcher.accept(e);
        }
    }

    public static void handle(Message message) {
        if(message.getMessage()[0] instanceof Packet<?> packet) packet.handle();
    }
}
