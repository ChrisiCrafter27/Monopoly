package monopol.client.screen;

import monopol.client.Client;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.CommunityCardC2SPacket;
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

    private final List<JLabel> busCards = new ArrayList<>();
    private final JLayeredPane busCardDeck = new JLayeredPane();
    private final JLayeredPane busCard = new JLayeredPane();
    private final JLayeredPane busCardDescription = new JLayeredPane();

    private final List<JLabel> eventCards = new ArrayList<>();
    private final JLayeredPane eventCardDeck = new JLayeredPane();
    private final JLayeredPane eventCard = new JLayeredPane();
    private final JLayeredPane eventCardButtons = new JLayeredPane();
    private final JLayeredPane eventCardDescription = new JLayeredPane();

    public CardDecksPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        communityCardDeck.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(communityCardDeck, DEFAULT_LAYER);
        communityCard.setBounds(JUtils.getX(131), JUtils.getY(784), JUtils.getX(350 - 131), JUtils.getY(953 - 784));
        JLabel communityCardBackground = JUtils.addImage("images/felder/karte.png", 0, 0, JUtils.getX(350 - 131), JUtils.getY(953 - 784));
        communityCard.add(communityCardBackground, DEFAULT_LAYER);
        JLabel communityCardTitle = JUtils.addText("Gemeinschaftskarte", 0, 8, communityCard.getWidth(), 16, SwingConstants.CENTER, Color.RED);
        communityCardTitle.setFont(communityCardTitle.getFont().deriveFont(Font.BOLD));
        communityCard.add(communityCardTitle, PALETTE_LAYER);
        communityCardDescription.setBounds(0, 0, communityCard.getWidth(), communityCard.getHeight());
        communityCardDescription.setVisible(true);
        communityCard.add(communityCardDescription, PALETTE_LAYER);
        communityCardButtons.setBounds(0, 0, communityCard.getWidth(), communityCard.getHeight());
        communityCardButtons.setVisible(true);
        communityCard.add(communityCardButtons, PALETTE_LAYER);
        add(communityCard, PALETTE_LAYER);


    }

    public void reset() {
        setVisible(false);
    }

    public void init(Supplier<Client> clientSup) {
        this.clientSup = clientSup;
        setVisible(true);
    }

    public void updateCommunityCards(String player, List<String> description, List<String> buttons, int size) {
        setCommunityCardDeck(size);
        if(player == null) {
            communityCard.setVisible(false);
        } else {
            communityCard.setVisible(true);
            communityCardButtons.setVisible(player.equals(clientSup.get().player().getName()));
            communityCard.setBounds(communityCard.getX(), JUtils.getY(784 - 2 * size), communityCard.getWidth(), communityCard.getHeight());
            setCommunityCardDescription(description);
            setCommunityCardButtons(buttons);
            communityCard.repaint();
        }
    }

    private void setCommunityCardDeck(int size) {
        while(size > communityCards.size()) {
            JLabel label = JUtils.addImage("images/felder/gemeinschaftskarte.png", communityCard.getX() - 2, communityCard.getY() - 2 - 2 * communityCards.size(), communityCard.getWidth() + 4, communityCard.getHeight() + 4);
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
            communityCardDescription.add(JUtils.addText(description.get(i), distance, 25+distance+i*(distance+height), communityCard.getWidth()-2*distance, height, SwingConstants.CENTER), DEFAULT_LAYER);
        }
    }

    private void setCommunityCardButtons(List<String> buttons) {
        int distance = 10;
        int height = 20;
        communityCardButtons.removeAll();
        for(int i = 0; i < buttons.size(); i++) {
            String button = buttons.get(i);
            communityCardButtons.add(JUtils.addButton(button, distance+i*((communityCard.getWidth()-distance)/buttons.size()), communityCard.getHeight()-distance-height, (communityCard.getWidth()-distance)/buttons.size()-distance, height, true,
                    actionEvent -> PacketManager.sendC2S(new CommunityCardC2SPacket(clientSup.get().player().getName(), button), clientSup.get(), Throwable::printStackTrace)), DEFAULT_LAYER);
        }
    }
}
