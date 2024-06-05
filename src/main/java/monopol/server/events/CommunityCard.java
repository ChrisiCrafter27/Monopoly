package monopol.server.events;

import monopol.common.data.*;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.CommunityCardS2CPacket;
import monopol.common.packets.custom.InfoS2CPacket;
import monopol.server.Server;

import java.util.*;

public class CommunityCard {
    private static final List<CommunityCard> COMMUNITY_CARDS = List.of(
            new CommunityCard(List.of("", "Gehe zurück zur", "Badstraße."), Map.of("Bewegen", (server, player) -> {
                player.setPosition(Field.fields().indexOf(Street.BADSTRASSE));
                server.events().onArrivedAtField();
            })),
            new CommunityCard(List.of("", "Zahle eine Strafe von 10€", "oder nimm eine Ereigniskarte."), Map.of("Zahlen", (server, player) -> {
                player.contractMoney(10);
                server.gameData().addFreeParking(10);
            }, "Ziehen", (server, player) -> {
                PacketManager.sendS2C(new InfoS2CPacket(player.getName() + " zieht eine Ereigniskarte."), PacketManager.all(), Throwable::printStackTrace);
                EventCard card = EventCard.getUnused();
                EventCard.setCurrent(card);
                card.activate(player);
            })),
            new CommunityCard(List.of("", "Bank-Irrtum zu deinen Gunsten.", "Ziehe 200€ ein."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(200);
            })),
            new CommunityCard(List.of("Es ist dein Geburtstag.", "Ziehe von jedem", "Spieler 10€ ein."), Map.of("Geld einziehen", (server, player) -> {
                int money = 0;
                for(Player player1 : server.getPlayersServerSide()) {
                    if(!player1.equals(player)) {
                        player1.contractMoney(10);
                        money += 10;
                    }
                }
                player.addMoney(money);
            })),
            new CommunityCard(List.of("Gehe in das Gefängnis!", "Begib dich direkt dorthin.", "Gehe nicht über Los.", "Ziehe nicht 200€ ein."), Map.of("Ins Gefängnis gehen", (server, player) -> {
                player.setInPrison(true);
            })),
            new CommunityCard(List.of("", "Arzt-Kosten.", "Zahle 50€."), Map.of("Geld zahlen", (server, player) -> {
                player.contractMoney(50);
                server.gameData().addFreeParking(50);
            })),
            new CommunityCard(List.of("Du hast den 2. Preis in einer", "Schönheitskonkurrenz", "gewonnen. Ziehe 10€ ein."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(10);
            })),
            new CommunityCard(List.of("Du kommst aus dem", "Gefängnis frei.", "Diese Karte musst du behalten,", "bis du sie benötigst."), Map.of("Karte einziehen", (server, player) -> {
                player.addPrisonCard();
            })),
            new CommunityCard(List.of("", "Aus Lagerverkäufen", "erhältst du 50€."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(50);
            })),
            new CommunityCard(List.of("", "", "Rücke vor bis auf Los."), Map.of("Bewegen", (server, player) -> {
                player.setPosition(Field.fields().indexOf(Corner.LOS));
                server.events().onArrivedAtField();
            })),
            new CommunityCard(List.of("", "Die Jahresrente wird fällig.", "Ziehe 100€ ein."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(100);
            })),
            new CommunityCard(List.of("", "Dur erbst", "100€."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(100);
            })),
            new CommunityCard(List.of("Rücke vor bis zum nächsten", "Bahnhof. Ist dieser verkauft,", "erhält der Eigentümer die", "doppelte Miete."), Map.of("Bewegen", (server, player) -> {
                int pos = player.getPosition();
                do {
                    pos++;
                    if(pos >= 52) pos -= 52;
                } while (!(Field.get(pos) instanceof TrainStation trainStation));
                int oldPos = player.getPosition();
                trainStation.setSpecialRent(true);
                player.setPosition(pos);
                if(player.getPosition() < oldPos) server.events().onPassedLos();
                server.events().onArrivedAtField();
            })),
            new CommunityCard(List.of("Du erhältst auf Vorzugs-", "Aktien 7% Dividende:", "25€"), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(25);
            })),
            new CommunityCard(List.of("Einkommenssteuer-", "Rückzahlung.", "Ziehe 20€ ein."), Map.of("Geld einziehen", (server, player) -> {
                player.addMoney(200);
            })),
            new CommunityCard(List.of("", "Zahle an das", "Krankenhaus 100€"), Map.of("Geld zahlen", (server, player) -> {
                player.contractMoney(100);
                server.gameData().addFreeParking(100);
            }))
    );
    private static List<CommunityCard> unusedCards = new ArrayList<>();
    private static CommunityCard currentCard = null;

    public static List<CommunityCard> getAll() {
        return new ArrayList<>(COMMUNITY_CARDS);
    }
    public static CommunityCard getUnused() {
        if(unusedCards.isEmpty()) resetUnused();
        CommunityCard toReturn = unusedCards.get(new Random().nextInt(unusedCards.size()));
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
    public static void setCurrent(CommunityCard currentCard) {
        CommunityCard.currentCard = currentCard;
    }
    public static CommunityCard getCurrent() {
        return currentCard;
    }

    public final List<String> description;
    public final Map<String, CommunityCardAction> actions;

    private CommunityCard(List<String> description, Map<String, CommunityCardAction> actions) {
        this.description = description;
        this.actions = actions;
    }

    public void activate(Player player) {
        PacketManager.sendS2C(new CommunityCardS2CPacket(player.getName(), description, new ArrayList<>(actions.keySet()), unusedSize()), PacketManager.all(), Throwable::printStackTrace);
    }

    public Map<String, CommunityCardAction> actions() {
        return Map.copyOf(actions);
    }

    @FunctionalInterface
    public interface CommunityCardAction {
        void act(Server server, Player player);
    }
}
