package monopol.server.events;

import monopol.common.data.*;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.CommunityCardS2CPacket;
import monopol.common.packets.custom.EventCardS2CPacket;
import monopol.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventCard {
    private static final List<EventCard> COMMUNITY_CARDS = List.of(
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {})),
            new EventCard(List.of(), Map.of("button", (server, player) -> {}))
    );
    private static List<EventCard> unusedCards = new ArrayList<>();
    private static EventCard currentCard = null;

    public static List<EventCard> getAll() {
        return new ArrayList<>(COMMUNITY_CARDS);
    }
    public static EventCard getUnused() {
        if(unusedCards.isEmpty()) resetUnused();
        EventCard toReturn = unusedCards.get(new Random().nextInt(unusedCards.size()));
        unusedCards.remove(toReturn);
        if(unusedCards.isEmpty()) resetUnused();
        return toReturn;
    }

    public static int unusedSize() {
        return unusedCards.size();
    }
    public static void resetUnused() {
        unusedCards = getAll();
    }
    public static void setCurrent(EventCard currentCard) {
        EventCard.currentCard = currentCard;
    }
    public static EventCard getCurrent() {
        return currentCard;
    }

    public final List<String> description;
    public final Map<String, EventCardAction> actions;

    private EventCard(List<String> description, Map<String, EventCardAction> actions) {
        this.description = description;
        this.actions = actions;
    }

    public void activate(Player player) {
        PacketManager.sendS2C(new EventCardS2CPacket(player.getName(), description, new ArrayList<>(actions.keySet()), unusedSize()), PacketManager.all(), Throwable::printStackTrace);
    }

    public Map<String, EventCardAction> actions() {
        return Map.copyOf(actions);
    }

    @FunctionalInterface
    public interface EventCardAction {
        void act(Server server, Player player);
    }
}
