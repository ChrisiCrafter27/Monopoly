package monopol.client.screen;

import monopol.client.Client;
import monopol.client.ClientEvents;
import monopol.client.TradeData;
import monopol.client.TradeState;
import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.message.Message;
import monopol.common.message.MessageType;
import monopol.common.Player;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TradePane extends JLayeredPane {
    private String player1 = "";
    private String player2 = "";
    private Supplier<Client> client = () -> {throw new IllegalStateException("init() was not called");};
    private final Supplier<TradeData> tradeData = () -> client.get().tradeData;
    private final Supplier<List<IPurchasable>> selected1 = () -> tradeData.get().offerCards;
    private final Supplier<List<IPurchasable>> selected2 = () -> tradeData.get().counterofferCards;
    private final Consumer<Message> sendMessage = message -> {
        try {
            client.get().serverMethod().sendMessage(player2, message.getMessageType(), message.getMessage());
        } catch (IOException e) {
            client.get().close();
        }
    };

    private final JLayeredPane background = new JLayeredPane();
    private final JLayeredPane buttons = new JLayeredPane();
    private final JLayeredPane texts = new JLayeredPane();
    private final JLayeredPane playerButtons = new JLayeredPane();
    private final JLayeredPane moneyButtons = new JLayeredPane();
    private final JLayeredPane tradeButtons = new JLayeredPane();
    private final JLayeredPane tradeInfoThis = new JLayeredPane();
    private final JLayeredPane tradeInfoOther = new JLayeredPane();

    private final JButton abortButton = JUtils.addButton("Handel abbrechen", 0, 0, 200, 50, true, actionEvent -> {
        setState(TradeState.NULL);
        reset();
        if(tradeData.get().tradePlayer != null) {
            sendMessage.accept(new Message(new Object[]{TradeState.ABORT, player1}, MessageType.TRADE));
            tradeData.get().tradePlayer = null;
        }
    });
    private final JButton acceptTradeButton = JUtils.addButton("Angebot annehmen", 1920/2-100+150, 1020/2+50, 200, 50, true, actionEvent -> {
        setState(TradeState.CHANGE_OFFER);
        sendMessage.accept(new Message(new Object[]{TradeState.ACCEPT, player1}, MessageType.TRADE));
    });
    private final JButton declineTradeButton = JUtils.addButton("Angebot ablehnen", 1920/2-100-150, 1020/2+50, 200, 50, true, actionEvent -> {
        tradeData.get().tradePlayer = null;
        setState(TradeState.NULL);
        sendMessage.accept(new Message(new Object[]{TradeState.DECLINE, player1}, MessageType.TRADE));
    });
    private final JButton okayButton = JUtils.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
        tradeData.get().tradePlayer = null;
        setState(TradeState.NULL);
        reset();
    });
    private final JButton sendOfferButton = JUtils.addButton("Angebot absenden", 1920/2-100+150, 1080-150, 200, 50, true, actionEvent -> {
        setState(TradeState.CONFIRM);
        sendMessage.accept(new Message(new Object[]{TradeState.SEND_OFFER, player1, tradeData.get().offerCards, tradeData.get().offerMoney}, MessageType.TRADE));
    });
    private final JButton changeOfferButton = JUtils.addButton("Angebot bearbeiten", 1920/2-100, 1080-150, 200, 50, false, actionEvent -> {
        setState(TradeState.CHANGE_OFFER);
        sendMessage.accept(new Message(new Object[]{TradeState.CHANGE_OFFER, player1}, MessageType.TRADE));
    });
    private final JButton confirmTradeButton = JUtils.addButton("Handel abschließen", 1920/2-100+300, 1080-150, 200, 50, false, actionEvent -> {
        setState(tradeData.get().tradePlayerConfirmed ? TradeState.PERFORM : TradeState.WAIT_FOR_CONFIRM);
        sendMessage.accept(new Message(new Object[]{TradeState.WAIT_FOR_CONFIRM, player1}, MessageType.TRADE));
    });

    private final JLabel waitText = JUtils.addText("Warte auf " + player2 + "...", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel tradeRequestText = JUtils.addText(player2 + " möchte mit dir handeln", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel declinedTradeText = JUtils.addText(player2 + " hat deine Einladung abgelehnt", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel abortedTradeText = JUtils.addText(player2 + " hat den Handel abgebrochen", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel alreadyTradingText = JUtils.addText(player2 + " handelt schon", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel firstOfferText = JUtils.addText("Angebot von dir", 0, 100, 1920/2, 20, true);
    private final JLabel secondOfferText = JUtils.addText("Angebot von " + player2, 1920/2, 100, 1920/2, 20, true);
    private final JLabel firstMoneyOfferText = JUtils.addText("€", 1920/4+1920/4+1920/4-100, 460, 200, 20, true);
    private final JLabel secondMoneyOfferText = JUtils.addText("€", 1920/4-100, 460, 200, 20, true);
    private final JLabel tradeCompleteText = JUtils.addText("Der Handel mit " + player2 + " wurder erfolgreich abgeschlossen!", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel tradeFailed1Text = JUtils.addText("Der Handel mit " + player2 + " konnte aus einem unbekannten Grund nich abgeschlossen werden.", 1920/2-500, 1020/2-50, 1000, 20, true);
    private final JLabel tradeFailed2Text = JUtils.addText("Bitte versuche es später erneut.", 1920/2-500, 1020/2-100, 1000, 20, true);

    public TradePane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        background.add(JUtils.addButton(new JButton(), null, "images/global/gray_background.png", 0, 60, 1920, 1020, true, false, actionEvent -> {}), 0);
        background.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(background, DEFAULT_LAYER);

        buttons.add(abortButton);
        buttons.add(acceptTradeButton);
        buttons.add(declineTradeButton);
        buttons.add(okayButton);
        buttons.add(sendOfferButton);
        buttons.add(changeOfferButton);
        buttons.add(confirmTradeButton);
        buttons.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        buttons.setVisible(true);
        add(buttons, PALETTE_LAYER);

        texts.add(waitText);
        texts.add(tradeRequestText);
        texts.add(declinedTradeText);
        texts.add(abortedTradeText);
        texts.add(alreadyTradingText);
        texts.add(firstOfferText);
        texts.add(secondOfferText);
        texts.add(firstMoneyOfferText);
        texts.add(secondMoneyOfferText);
        texts.add(tradeCompleteText);
        texts.add(tradeFailed1Text);
        texts.add(tradeFailed2Text);
        texts.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        texts.setVisible(true);
        add(texts, PALETTE_LAYER);

        playerButtons.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(playerButtons, PALETTE_LAYER);

        addMoneyButtons();
        moneyButtons.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(moneyButtons, PALETTE_LAYER);

        addTradeButtons();
        tradeButtons.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(tradeButtons, PALETTE_LAYER);

        addTradeInfoThis();
        tradeInfoThis.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(tradeInfoThis, PALETTE_LAYER);

        addTradeInfoOther();
        tradeInfoOther.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(tradeInfoOther, PALETTE_LAYER);

        reset();
    }

    public void reset() {
        Arrays.stream(buttons.getComponents()).forEach(component -> component.setVisible(false));
        Arrays.stream(texts.getComponents()).forEach(component -> component.setVisible(false));
        background.setVisible(false);
        playerButtons.setVisible(false);
        moneyButtons.setVisible(false);
        tradeButtons.setVisible(false);
        tradeInfoThis.setVisible(false);
        tradeInfoOther.setVisible(false);
        setVisible(false);

    }

    public void init(String player1, String player2, Supplier<Client> client) {
        this.player1 = player1;
        this.player2 = player2;
        this.client = client;
        waitText.setText("Warte auf " + player2 + "...");
        tradeRequestText.setText(player2 + " möchte mit dir handeln");
        declinedTradeText.setText(player2 + " hat deine Einladung abgelehnt");
        abortedTradeText.setText(player2 + " hat den Handel abgebrochen");
        alreadyTradingText.setText(player2 + " handelt schon");
        secondOfferText.setText("Angebot von " + player2);
        tradeCompleteText.setText("Der Handel mit " + player2 + " wurder erfolgreich abgeschlossen!");
        tradeFailed1Text.setText("Der Handel mit " + player2 + " konnte aus einem unbekannten Grund nich abgeschlossen werden.");
        setVisible(true);
    }

    private boolean isOwner(IPurchasable card) {
        return card.getOwner().equals(player1);
    }
    private boolean selected(IPurchasable card) {
        return selected1.get().contains(card);
    }
    private boolean otherIsOwner(IPurchasable card) {
        return card.getOwner().equals(player2);
    }
    private boolean otherSelected(IPurchasable card) {
        return selected2.get().contains(card);
    }
    private void select(IPurchasable card) {
        if(!selected(card)) selected1.get().add(card);
    }
    private void deselect(IPurchasable card) {
        if(selected(card)) selected1.get().remove(card);
    }
    private void setState(TradeState state) {
        tradeData.get().tradeState = state;
    }
    
    private void addMoneyButtons() {
        moneyButtons.add(JUtils.addButton("-1", 1920/4-50-50-50, 600, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 1) tradeData.get().offerMoney -= 1;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 8);
        moneyButtons.add(JUtils.addButton("+1", 1920/4-50+50+50, 600, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 1) tradeData.get().offerMoney += 1;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 0);
        moneyButtons.add(JUtils.addButton("-5", 1920/4-50-50-50, 600+30, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 5) tradeData.get().offerMoney -= 5;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 9);
        moneyButtons.add(JUtils.addButton("+5", 1920/4-50+50+50, 600+30, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 5) tradeData.get().offerMoney += 5;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 1);
        moneyButtons.add(JUtils.addButton("-10", 1920/4-50-50-50, 600+60, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 10) tradeData.get().offerMoney -= 10;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 10);
        moneyButtons.add(JUtils.addButton("+10", 1920/4-50+50+50, 600+60, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 10) tradeData.get().offerMoney += 10;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 2);
        moneyButtons.add(JUtils.addButton("-20", 1920/4-50-50-50, 600+90, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 20) tradeData.get().offerMoney -= 20;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 11);
        moneyButtons.add(JUtils.addButton("+20", 1920/4-50+50+50, 600+90, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 20) tradeData.get().offerMoney += 20;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 3);
        moneyButtons.add(JUtils.addButton("-50", 1920/4-50-50-50, 600+120, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 50) tradeData.get().offerMoney -= 50;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 12);
        moneyButtons.add(JUtils.addButton("+50", 1920/4-50+50+50, 600+120, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 50) tradeData.get().offerMoney += 50;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 4);
        moneyButtons.add(JUtils.addButton("-100", 1920/4-50-50-50, 600+150, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 100) tradeData.get().offerMoney -= 100;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 13);
        moneyButtons.add(JUtils.addButton("+100", 1920/4-50+50+50, 600+150, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 100) tradeData.get().offerMoney += 100;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 5);
        moneyButtons.add(JUtils.addButton("-500", 1920/4-50-50-50, 600+180, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 500) tradeData.get().offerMoney -= 500;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 14);
        moneyButtons.add(JUtils.addButton("+500", 1920/4-50+50+50, 600+180, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 500) tradeData.get().offerMoney += 500;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 6);
        moneyButtons.add(JUtils.addButton("-1000", 1920/4-50-50-50, 600+210, 100, 25, false, actionEvent -> {
            if(tradeData.get().offerMoney >= 1000) tradeData.get().offerMoney -= 1000;
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 15);
        moneyButtons.add(JUtils.addButton("+1000", 1920/4-50+50+50, 600+210, 100, 25, false, actionEvent -> {
            try {
                if(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 1000) tradeData.get().offerMoney += 1000;
            } catch (RemoteException e) {
                client.get().close();
                return;
            }
            tradeData.get().tradeState = TradeState.SEND_OFFER;
        }), 7);
    }

    private void addTradeButtons() {
        List<IPurchasable> cards = Field.getAll().stream().filter(card -> card instanceof IPurchasable).map(card -> ((IPurchasable) card)).toList();
        for(int i = 1; i <= cards.size(); i++) {
            IPurchasable card = cards.get(i-1);
            JButton button = new JButton();
            button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + getColor(i) + "_filled.png"));
            button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
            tradeButtons.add(JUtils.addButton(button, "", "images/kleine_karten/" + getColor(i) + ".png", 295 + getX(i), 200 + getY(i), 20, 40, false, false, actionEvent -> {
                if(selected(card)) deselect(card); else select(card);
                setState(TradeState.SEND_OFFER);
            }), i-1);
        }
    }

    private void addTradeInfoOther() {
        List<IPurchasable> cards = Field.getAll().stream().filter(card -> card instanceof IPurchasable).map(card -> ((IPurchasable) card)).toList();
        for(int i = 1; i <= cards.size(); i++) {
            tradeInfoOther.add(JUtils.addImage("images/kleine_karten/disabled.png", 1255 + getX(i), 200 + getY(i)), i-1);
        }
    }

    private void addTradeInfoThis() {
        List<IPurchasable> cards = Field.getAll().stream().filter(card -> card instanceof IPurchasable).map(card -> ((IPurchasable) card)).toList();
        for(int i = 1; i <= cards.size(); i++) {
            tradeInfoThis.add(JUtils.addImage("images/kleine_karten/disabled.png", 295 + getX(i), 200 + getY(i)), i-1);
        }
    }

    private String getColor(int id) {
        if(id <= 3) {
            return "brown";
        } else if(id <= 7) {
            return "cyan";
        } else if(id <= 11) {
            return "pink";
        } else if(id <= 15) {
            return "orange";
        } else if(id <= 19) {
            return "red";
        } else if(id <= 23) {
            return "yellow";
        } else if(id <= 27) {
            return "green";
        } else if(id <= 30) {
            return "blue";
        } else if(id <= 34) {
            return "train";
        } else if(id == 35) {
            return "gas";
        } else if(id == 36) {
            return "elec";
        } else if(id == 37) {
            return "water";
        } else return "";
    }

    private int getX(int id) {
        int toReturn;
        if (id <= 11) {
            toReturn = 30 * (id);
        } else if (id <= 23) {
            toReturn = 30 * (id-12);
        } else if (id <= 30) {
            toReturn = 30 * (id-24);
        } else {
            toReturn = 30 * (id-31);
        }
        if(id <= 3) {
            return toReturn - 15;
        } else if(id <= 7) {
            return toReturn - 15 + 10;
        } else if(id <= 11) {
            return toReturn - 15 + 20;
        } else if(id <= 15) {
            return toReturn;
        } else if(id <= 19) {
            return toReturn + 10;
        } else if(id <= 23) {
            return toReturn + 20;
        } else if(id <= 27) {
            return toReturn + 80;
        } else if(id <= 30) {
            return toReturn + 80 + 10;
        } else if(id <= 34) {
            return toReturn + 80;
        } else if(id <= 37) {
            return toReturn + 80 + 10;
        } else return 0;
    }

    private int getY(int id) {
        if(id <= 11) {
            return 0;
        } else if(id <= 23) {
            return 50;
        } else if(id <= 30) {
            return 100;
        } else if(id <= 37) {
            return 150;
        } else return 0;
    }

    public void darken() {
        background.setVisible(true);
    }

    public void enablePlayerButtons() throws RemoteException {
        resetPlayerButtons();
        Arrays.stream(playerButtons.getComponents()).forEach(component -> component.setVisible(true));
        playerButtons.setVisible(true);
    }

    public void resetPlayerButtons() throws RemoteException {
        playerButtons.removeAll();
        int i = 0;
        for(Player player : client.get().serverMethod().getServerPlayers()) {
            if(!player1.equals(player.getName())) {
                playerButtons.add(JUtils.addButton(player.getName(), 1920 / 2 - 250, 200 + (75 * i), 500, 50, true, actionEvent -> {
                    tradeData.get().tradePlayer = player.getName();
                    setState(TradeState.WAIT_FOR_ACCEPT);
                    player2 = player.getName();
                    sendMessage.accept(new Message(new Object[]{TradeState.WAIT_FOR_ACCEPT, player1}, MessageType.TRADE));
                    ClientEvents.trade(client, this);
                }), 0);
                i += 1;
            }
        }
    }

    public void enableAbortButton(int x, int y) {
        abortButton.setBounds(x, y, abortButton.getWidth(), abortButton.getHeight());
        abortButton.setVisible(true);
    }

    public void enableWaitText(int x, int y) {
        waitText.setBounds(x, y, waitText.getWidth(), waitText.getHeight());
        waitText.setVisible(true);
    }

    public void enableTradeRequest() {
        tradeRequestText.setVisible(true);
        acceptTradeButton.setVisible(true);
        declineTradeButton.setVisible(true);
    }

    public void enableDeclinedInvitation() {
        declinedTradeText.setVisible(true);
        okayButton.setVisible(true);
    }

    public void enableInvitationExpired() {
        abortedTradeText.setVisible(true);
        okayButton.setVisible(true);
    }

    public void enableAlreadyTrading() {
        alreadyTradingText.setVisible(true);
        okayButton.setVisible(true);
    }

    public void enableOfferTexts() {
        firstOfferText.setVisible(true);
        secondOfferText.setVisible(true);
    }

    public void enableChangeOfferButtons() throws RemoteException {
        List<IPurchasable> cards = Field.getAll().stream().filter(card -> card instanceof IPurchasable).map(card -> ((IPurchasable) card)).toList();
        for(int i = 1; i <= cards.size(); i++) {
            IPurchasable card = cards.get(i-1);
            if(tradeButtons.getComponent(i-1) instanceof JButton button) {
                button.setEnabled(isOwner(card));
                button.setSelected(isOwner(card));
            }
            if(tradeInfoOther.getComponent(i-1) instanceof JLabel label) {
                ImageIcon icon = new ImageIcon(otherIsOwner(card) ? otherSelected(card) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png" : "images/kleine_karten/disabled.png");
                label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
        }
        tradeButtons.setVisible(true);
        tradeInfoOther.setVisible(true);

        firstMoneyOfferText.setText(tradeData.get().counterOfferMoney + "€");
        secondMoneyOfferText.setText(tradeData.get().offerMoney + "€");
        firstMoneyOfferText.setVisible(true);
        secondMoneyOfferText.setVisible(true);

        for(int i = 0; i < 8; i++) {
            if(moneyButtons.getComponent(i) instanceof JButton button) button.setEnabled(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 1);
        }
        for(int i = 8; i < 15; i++) {
            if(moneyButtons.getComponent(i) instanceof JButton button) button.setEnabled(tradeData.get().offerMoney >= 1);
        }
        moneyButtons.setVisible(true);

        sendOfferButton.setVisible(true);
    }

    public void enableTradeInfo() throws RemoteException {
        List<IPurchasable> cards = Field.getAll().stream().filter(card -> card instanceof IPurchasable).map(card -> ((IPurchasable) card)).toList();
        for(int i = 1; i <= cards.size(); i++) {
            IPurchasable card = cards.get(i-1);
            if(tradeInfoThis.getComponent(i-1) instanceof JLabel label) {
                ImageIcon icon = new ImageIcon(isOwner(card) ? selected(card) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png" : "images/kleine_karten/disabled.png");
                label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
            if(tradeInfoOther.getComponent(i-1) instanceof JLabel label) {
                ImageIcon icon = new ImageIcon(otherIsOwner(card) ? otherSelected(card) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png" : "images/kleine_karten/disabled.png");
                label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
        }
        tradeInfoThis.setVisible(true);
        tradeInfoOther.setVisible(true);

        for(int i = 0; i < 8; i++) {
            if(moneyButtons.getComponent(i) instanceof JButton button) button.setEnabled(client.get().serverMethod().getServerPlayer(player1).getMoney() >= tradeData.get().offerMoney + 1);
        }
        for(int i = 8; i < 15; i++) {
            if(moneyButtons.getComponent(i) instanceof JButton button) button.setEnabled(tradeData.get().offerMoney >= 1);
        }
        firstMoneyOfferText.setText(tradeData.get().counterOfferMoney + "€");
        secondMoneyOfferText.setText(tradeData.get().offerMoney + "€");
        firstMoneyOfferText.setVisible(true);
        secondMoneyOfferText.setVisible(true);
    }

    public void enableConfirmButtons() {
        changeOfferButton.setEnabled(!tradeData.get().tradePlayerConfirmed);
        confirmTradeButton.setEnabled(tradeData.get().counterOfferSend);
        changeOfferButton.setVisible(true);
        confirmTradeButton.setVisible(true);
    }

    public void enableTradeComplete() {
        tradeCompleteText.setVisible(true);
    }

    public void enableShowTradeFailed() {
        tradeFailed1Text.setVisible(true);
        tradeFailed2Text.setVisible(true);
    }
}
