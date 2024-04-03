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
            new EventCard(List.of("Miete und Anleihezinsen", "werden fällig. Die Bank", "zahlt dir 150€."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(150);
            })),
            new EventCard(List.of("Rücke vor bis zum nächsten", "Bahnhof. Ist dieser verkauft,", "erhält der Eigentümer die", "doppelte Miete."), Map.of("Bewegen", (server, player) -> {
                int pos = player.getPosition();
                do {
                    pos++;
                } while (!(Field.get(pos) instanceof TrainStation trainStation));
                trainStation.setSpecialRent(true);
                player.setPosition(pos);
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("", "", "Gehe 3 Felder zurück."), Map.of("Bewegen", (server, player) -> {
                player.setPosition(player.getPosition() - 3);
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("Gehe in das Gefängnis!", "Begib dich direkt dorthin.", "Gehe nicht über Los.", "Ziehe nicht 200€ ein."), Map.of("Ins Gefängnis gehen", (server, player) -> {
                player.setInPrison(true);
            })),
            new EventCard(List.of("Rücke vor bis zum", "Opernplatz. Wenn du über", "Los kommst, ziehe 200€ ein."), Map.of("Bewegen", (server, player) -> {
                int oldPos = player.getPosition();
                player.setPosition(Field.fields().indexOf(Street.OPERNPLATZ));
                if(player.getPosition() < oldPos) server.events().onPassedLos();
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("Rücke vor bis zur", "Seestrasse. Wenn du über", "Los kommst, ziehe 200€ ein."), Map.of("Bewegen", (server, player) -> {
                int oldPos = player.getPosition();
                player.setPosition(Field.fields().indexOf(Street.SEESTRASSE));
                if(player.getPosition() < oldPos) server.events().onPassedLos();
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("Mache einen Ausflug zum", "Südbahnhof. Wenn du über", "Los kommst, ziehe 200€ ein."), Map.of("Bewegen", (server, player) -> {
                int oldPos = player.getPosition();
                player.setPosition(Field.fields().indexOf(TrainStation.SUEDBAHNHOF));
                if(player.getPosition() < oldPos) server.events().onPassedLos();
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("", "", "Rücke vor bis auf Los."), Map.of("Bewegen", (server, player) -> {
                player.setPosition(Field.fields().indexOf(Corner.LOS));
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("", "Die Bank zahlt dir eine Dividende:", "50€"), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(50);
            })),
            new EventCard(List.of("Du bist zum Vorstand", "gewählt worden. Zahle", "jedem Spieler 50€."), Map.of("Geld zahlen", (server, player) -> {
                int i = 0;
                for(Player p : server.getPlayersServerSide()) {
                    if(!p.equals(player)) {
                        p.addMoney(50);
                        i++;
                    }
                }
                player.contractMoney(i * 50);
            })),
            new EventCard(List.of("Rücke vor bis zum nächsten", "Bahnhof. Ist dieser verkauft,", "erhält der Eigentümer die", "doppelte Miete."), Map.of("Bewegen", (server, player) -> {
                int pos = player.getPosition();
                do {
                    pos++;
                } while (!(Field.get(pos) instanceof TrainStation trainStation));
                int oldPos = player.getPosition();
                trainStation.setSpecialRent(true);
                player.setPosition(pos);
                if(player.getPosition() < oldPos) server.events().onPassedLos();
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("Rücke vor bis zum nächsten", "Versorgungswerk. Der", "Eigentümer erhält als Miete:", "10€ • Wert des linken Würfels"), Map.of("Bewegen", (server, player) -> {
                int pos = player.getPosition();
                do {
                    pos++;
                } while (!(Field.get(pos) instanceof Plant plant));
                int oldPos = player.getPosition();
                plant.setSpecialRent(true);
                player.setPosition(pos);
                if(player.getPosition() < oldPos) server.events().onPassedLos();
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("", "Rücke vor bis zur", "Schlossallee."), Map.of("Bewegen", (server, player) -> {
                player.setPosition(Field.fields().indexOf(Street.SCHLOSSALLEE));
                server.events().onArrivedAtField();
            })),
            new EventCard(List.of("Du wirst zu Straßenheran-", "besserungsarbeiten herangezogen.", "Zahle: 25€/Zugdepot 40€/Haus", "115€/Hotel 200€/Wolkenkratzer"), Map.of("Geld zahlen", (server, player) -> {
                int money = 0;
                for(IPurchasable purchasable : Field.purchasables()) {
                    if(purchasable instanceof Street street) {
                        if(street.getLevel() == 5) money += 115;
                        if(street.getLevel() == 6) money += 200;
                        else money += 40 * street.getLevel();
                    }
                    else if(purchasable instanceof TrainStation trainStation && trainStation.isUpgraded()) money += 25;
                }
                player.contractMoney(money);
            })),
            new EventCard(List.of("Lasse alle deine Häuser", "renovieren. Zahle:", "100€/Zugdepot 40€/Haus", "100€/Hotel 100€/Wolkenkratzer"), Map.of("button", (server, player) -> {
                int money = 0;
                for(IPurchasable purchasable : Field.purchasables()) {
                    if(purchasable instanceof Street street) {
                        if(street.getLevel() == 5) money += 100;
                        if(street.getLevel() == 6) money += 100;
                        else money += 40 * street.getLevel();
                    }
                    else if(purchasable instanceof TrainStation trainStation && trainStation.isUpgraded()) money += 100;
                }
                player.contractMoney(money);
            })),
            new EventCard(List.of("Du kommst aus dem", "Gefängnis frei.", "Diese Karte musst du behalten,", "bis du sie benötigst."), Map.of("Karte einziehen", (server, player) -> {
                player.addPrisonCard();
            }))
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
