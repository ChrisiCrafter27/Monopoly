package monopol.server.events;

import monopol.common.data.Player;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.BusCardS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BusCard {
    private static final List<BusCard> BUS_CARDS = new ArrayList<>();
    public static void setSize(int size) {
        int expiration = size / 5;
        int normal = size - expiration;
        BUS_CARDS.clear();
        for(int i = 0; i < expiration; i++) BUS_CARDS.add(new BusCard(true));
        for(int i = 0; i < normal; i++) BUS_CARDS.add(new BusCard(false));
    }

    private static List<BusCard> unusedCards = new ArrayList<>();
    private static BusCard currentCard = null;
    private static boolean inQueue;

    public static List<BusCard> getAll() {
        return new ArrayList<>(BUS_CARDS);
    }
    public static BusCard getUnused() {
        if(unusedCards.isEmpty()) return null;
        BusCard toReturn = unusedCards.get(new Random().nextInt(unusedCards.size()));
        unusedCards.remove(toReturn);
        return toReturn;
    }

    public static int unusedSize() {
        return unusedCards.size();
    }
    public static void resetUnused() {
        unusedCards = getAll();
    }
    public static void setCurrent(BusCard currentCard) {
        BusCard.currentCard = currentCard;
    }
    public static BusCard getCurrent() {
        return currentCard;
    }

    public static void enqueue() {
        inQueue = true;
    }
    public static boolean deQueue() {
        if(inQueue) {
            inQueue = false;
            return true;
        } else return false;
    }
    public static boolean onSide(int position, int target) {
        if(position >= 39) {
            return target <= 52 && target > position || target == 0;
        } else if(position >= 26) {
            return target <= 39 && target > position;
        } else if(position >= 13) {
            return target <= 26 && target > position;
        } else if(position >= 0) {
            return target <= 13 && target > position;
        }
        return false;
    }

    public final boolean expiration;

    private BusCard(boolean expiration) {
        this.expiration = expiration;
    }

    public void activate(Player player) {
        PacketManager.sendS2C(new BusCardS2CPacket(player.getName(), expiration, unusedSize()), PacketManager.all(), Throwable::printStackTrace);
    }
}
