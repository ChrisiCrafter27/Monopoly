package monopol.client.screen;

import monopol.client.Client;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.BusCardC2SPacket;
import monopol.common.packets.custom.CommunityCardC2SPacket;
import monopol.common.packets.custom.EventCardC2SPacket;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class CardDecksPane extends JLayeredPane {
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};

    private final List<JLabel> communityCards = new ArrayList<>();
    private final JLayeredPane communityCardDeck = new JLayeredPane();
    private final JLayeredPane communityCard = new JLayeredPane();
    private final JLayeredPane communityCardButtons = new JLayeredPane();
    private final JLayeredPane communityCardDescription = new JLayeredPane();
    private final int communityX = 131;
    private final int communityY = 784;
    private final int communityWidth = 350 - communityX;
    private final int communityHeight = 953  -  communityY;

    private final List<JLabel> busCards = new ArrayList<>();
    private final JLayeredPane busCardDeck = new JLayeredPane();
    private final JLayeredPane busCard = new JLayeredPane();
    private final JButton busCardButton = JUtils.addButton("Karte nehmen", 10, 953 - 784 - 30, 620 - 401 - 20, 20, true,
            actionEvent -> PacketManager.sendC2S(new BusCardC2SPacket(clientSup.get().player().getName()), clientSup.get(), Throwable::printStackTrace));
    private final JLayeredPane busCardDescription = new JLayeredPane();
    private final int busX = 401;
    private final int busY = 784;
    private final int busWidth = 620 - busX;
    private final int busHeight = 953 - busY;

    private final List<JLabel> eventCards = new ArrayList<>();
    private final JLayeredPane eventCardDeck = new JLayeredPane();
    private final JLayeredPane eventCard = new JLayeredPane();
    private final JLayeredPane eventCardButtons = new JLayeredPane();
    private final JLayeredPane eventCardDescription = new JLayeredPane();
    private final int eventX = 671;
    private final int eventY = 784;
    private final int eventWidth = 890 - eventX;
    private final int eventHeight = 953 - eventY;

    public CardDecksPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        communityCardDeck.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(communityCardDeck, DEFAULT_LAYER);
        communityCard.setBounds(JUtils.getX(communityX), JUtils.getY(communityY), JUtils.getX(communityWidth), JUtils.getY(communityHeight));
        JLabel communityCardBackground = JUtils.addImage("images/felder/karte.png", 0, 0, communityWidth, communityHeight);
        communityCard.add(communityCardBackground, DEFAULT_LAYER);
        JLabel communityCardTitle = JUtils.addText("Gemeinschaftskarte", 0, 8, communityWidth, 16, SwingConstants.CENTER, Color.RED);
        communityCardTitle.setFont(communityCardTitle.getFont().deriveFont(Font.BOLD));
        communityCard.add(communityCardTitle, PALETTE_LAYER);
        communityCardDescription.setBounds(0, 0, communityCard.getWidth(), communityCard.getHeight());
        communityCardDescription.setVisible(true);
        communityCard.add(communityCardDescription, PALETTE_LAYER);
        communityCardButtons.setBounds(0, 0, communityCard.getWidth(), communityCard.getHeight());
        communityCardButtons.setVisible(true);
        communityCard.add(communityCardButtons, PALETTE_LAYER);
        add(communityCard, PALETTE_LAYER);

        busCardDeck.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(busCardDeck, DEFAULT_LAYER);
        busCard.setBounds(JUtils.getX(busX), JUtils.getY(busY), JUtils.getX(busWidth), JUtils.getY(busHeight));
        JLabel busCardBackground = JUtils.addImage("images/felder/karte.png", 0, 0, busWidth, busHeight);
        busCard.add(busCardBackground, DEFAULT_LAYER);
        JLabel busCardTitle = JUtils.addText("Busfahrkarte", 0, 8, busWidth, 16, SwingConstants.CENTER, Color.RED);
        busCardTitle.setFont(busCardTitle.getFont().deriveFont(Font.BOLD));
        busCard.add(busCardTitle, PALETTE_LAYER);
        busCardDescription.setBounds(0, 0, busCard.getWidth(), busCard.getHeight());
        busCardDescription.setVisible(true);
        busCard.add(busCardDescription, PALETTE_LAYER);
        busCardButton.setVisible(true);
        busCard.add(busCardButton, PALETTE_LAYER);
        add(busCard, PALETTE_LAYER);

        eventCardDeck.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(eventCardDeck, DEFAULT_LAYER);
        eventCard.setBounds(JUtils.getX(eventX), JUtils.getY(eventY), JUtils.getX(eventWidth), JUtils.getY(eventHeight));
        JLabel eventCardBackground = JUtils.addImage("images/felder/karte.png", 0, 0, eventWidth, eventHeight);
        eventCard.add(eventCardBackground, DEFAULT_LAYER);
        JLabel eventCardTitle = JUtils.addText("Ereigniskarte", 0, 8, eventWidth, 16, SwingConstants.CENTER, Color.RED);
        eventCardTitle.setFont(eventCardTitle.getFont().deriveFont(Font.BOLD));
        eventCard.add(eventCardTitle, PALETTE_LAYER);
        eventCardDescription.setBounds(0, 0, eventCard.getWidth(), eventCard.getHeight());
        eventCardDescription.setVisible(true);
        eventCard.add(eventCardDescription, PALETTE_LAYER);
        eventCardButtons.setBounds(0, 0, eventCard.getWidth(), eventCard.getHeight());
        eventCardButtons.setVisible(true);
        eventCard.add(eventCardButtons, PALETTE_LAYER);
        add(eventCard, PALETTE_LAYER);
    }

    public void reset() {
        setVisible(false);
    }

    public void init(Supplier<Client> clientSup) {
        this.clientSup = clientSup;
        setVisible(true);
    }

    public synchronized void updateCommunityCards(String player, List<String> description, List<String> buttons, int size) {
        updateCommunityCardDeck(size);
        if(player == null) {
            communityCard.setVisible(false);
        } else {
            communityCard.setVisible(true);
            communityCardButtons.setVisible(player.equals(clientSup.get().player().getName()));
            communityCard.setBounds(communityCard.getX(), JUtils.getY(communityY - 2 * (size - 1)), communityCard.getWidth(), communityCard.getHeight());
            setCommunityCardDescription(description);
            setCommunityCardButtons(buttons);
            communityCard.repaint();
        }
    }

    private void updateCommunityCardDeck(int size) {
        while(size > communityCards.size()) {
            JLabel label = JUtils.addImage("images/felder/gemeinschaftskarte.png", communityX - 2, communityY - 2 - 2 * communityCards.size(), communityWidth + 4, communityHeight + 4);
            communityCards.add(label);
            communityCardDeck.add(label, DEFAULT_LAYER, 0);
        }
        while(size < communityCards.size()) {
            communityCardDeck.remove(communityCards.remove(communityCards.size() - 1));
        }
        communityCardDeck.repaint();
    }

    private void setCommunityCardDescription(List<String> description) {
        int distance = 7;
        int height = 14;
        communityCardDescription.removeAll();
        for(int i = 0; i < description.size(); i++) {
            communityCardDescription.add(JUtils.addText(description.get(i), distance, 25+distance+i*(distance+height), communityWidth-2*distance, height, SwingConstants.CENTER), DEFAULT_LAYER);
        }
    }

    private void setCommunityCardButtons(List<String> buttons) {
        int distance = 10;
        int height = 20;
        communityCardButtons.removeAll();
        for(int i = 0; i < buttons.size(); i++) {
            String button = buttons.get(i);
            communityCardButtons.add(JUtils.addButton(button, distance+i*((communityWidth-distance)/buttons.size()), communityHeight-distance-height, (communityWidth-distance)/buttons.size()-distance, height, true,
                    actionEvent -> PacketManager.sendC2S(new CommunityCardC2SPacket(clientSup.get().player().getName(), button), clientSup.get(), Throwable::printStackTrace)), DEFAULT_LAYER);
        }
    }

    public synchronized void updateBusCards(String player, boolean expiration, int size) {
        setBusCardDeck(size);
        if(player == null) {
            busCard.setVisible(false);
        } else {
            busCard.setVisible(true);
            busCardButton.setVisible(player.equals(clientSup.get().player().getName()));
            busCard.setBounds(busCard.getX(), JUtils.getY(busY - 2 * (size - 1)), busCard.getWidth(), busCard.getHeight());
            setBusCardDescription(expiration);
            busCard.repaint();
        }
    }

    private void setBusCardDeck(int size) {
        while(size > busCards.size()) {
            JLabel label = JUtils.addImage("images/felder/busfahrkarte.png", busX - 2, busY - 2 - 2 * busCards.size(), 620 - busX + 4, 953 - busY + 4);
            busCards.add(label);
            busCardDeck.add(label, DEFAULT_LAYER, 0);
        }
        while(size < busCards.size()) {
            busCardDeck.remove(busCards.remove(busCards.size() - 1));
        }
        busCardDeck.repaint();
    }

    private void setBusCardDescription(boolean expiration) {
        int distance = 7;
        int height = 14;
        List<String> description = expiration ? List.of(
                "Alle Busfahrkarten",
                "verfallen - Bis auf diese.",
                "",
                "Rücke auf ein beliebiges",
                "Feld auf deiner Seite vor."
        ) : List.of(
                "",
                "Rücke auf ein beliebiges",
                "Feld auf deiner Seite vor."
        );
        busCardDescription.removeAll();
        for(int i = 0; i < description.size(); i++) {
            busCardDescription.add(JUtils.addText(description.get(i), distance, 25+distance+i*(distance+height), busWidth-2*distance, height, SwingConstants.CENTER), DEFAULT_LAYER);
        }
    }

    public synchronized void updateEventCards(String player, List<String> description, List<String> buttons, int size) {
        updateEventCardDeck(size);
        if(player == null) {
            eventCard.setVisible(false);
        } else {
            eventCard.setVisible(true);
            eventCardButtons.setVisible(player.equals(clientSup.get().player().getName()));
            eventCard.setBounds(eventCard.getX(), JUtils.getY(eventY - 2 * (size - 1)), eventCard.getWidth(), eventCard.getHeight());
            setEventCardDescription(description);
            setEventCardButtons(buttons);
            eventCard.repaint();
        }
    }

    private void updateEventCardDeck(int size) {
        while(size > eventCards.size()) {
            JLabel label = JUtils.addImage("images/felder/ereigniskarte.png", eventX - 2, eventY - 2 - 2 * eventCards.size(), 890 - eventX + 4, 953 - eventY + 4);
            eventCards.add(label);
            eventCardDeck.add(label, DEFAULT_LAYER, 0);
        }
        while(size < eventCards.size()) {
            eventCardDeck.remove(eventCards.remove(eventCards.size() - 1));
        }
        eventCardDeck.repaint();
    }

    private void setEventCardDescription(List<String> description) {
        int distance = 7;
        int height = 14;
        eventCardDescription.removeAll();
        for(int i = 0; i < description.size(); i++) {
            eventCardDescription.add(JUtils.addText(description.get(i), distance, 25+distance+i*(distance+height), eventWidth-2*distance, height, SwingConstants.CENTER), DEFAULT_LAYER);
        }
    }

    private void setEventCardButtons(List<String> buttons) {
        int distance = 10;
        int height = 20;
        eventCardButtons.removeAll();
        for(int i = 0; i < buttons.size(); i++) {
            String button = buttons.get(i);
            eventCardButtons.add(JUtils.addButton(button, distance+i*((eventWidth-distance)/buttons.size()), eventHeight-distance-height, (eventWidth-distance)/buttons.size()-distance, height, true,
                    actionEvent -> PacketManager.sendC2S(new EventCardC2SPacket(clientSup.get().player().getName(), button), clientSup.get(), Throwable::printStackTrace)), DEFAULT_LAYER);
        }
    }
}
