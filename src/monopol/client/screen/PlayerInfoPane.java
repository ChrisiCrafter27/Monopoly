package monopol.client.screen;

import monopol.client.Client;
import monopol.common.data.*;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.ButtonC2SPacket;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerInfoPane extends JLayeredPane {
    private static final List<IPurchasable> PURCHASABLES;
    static {
        PURCHASABLES = new ArrayList<>();
        PURCHASABLES.addAll(List.of(Street.values()));
        PURCHASABLES.addAll(List.of(TrainStation.values()));
        PURCHASABLES.addAll(List.of(Plant.values()));
    }

    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init()  was not called");};
    private String currentPlayer = null;
    private String activePlayer = null;
    private boolean diceRolled;
    private boolean hasToPayRent;
    private boolean ready;

    private final JLayeredPane topLeft = new JLayeredPane();
    private final JLayeredPane topRight = new JLayeredPane();
    private final JLayeredPane bottom = new JLayeredPane();
    private final JLayeredPane purchasableThis = new JLayeredPane();
    private final JLayeredPane purchasableOther = new JLayeredPane();

    private final JButton buttonL = JUtils.addButton(null,"images/Main_pictures/Player_display.png", 1060,90,400,60,true,false, actionevent -> {});
    private final JLabel nameL = JUtils.addText("-",1060,90 + 13,400,30, SwingConstants.CENTER);
    private final JLabel moneyL = JUtils.addText("-",1160,340,200,28, SwingConstants.CENTER);
    private final JLabel busL = JUtils.addText("-",1160-100+17,317,67,22, SwingConstants.CENTER);
    private final JLabel freiL = JUtils.addText("-",1160+217,317,67,22, SwingConstants.CENTER);
    private final JButton buttonR = JUtils.addButton(null,"images/Main_pictures/Player_display.png", 1479,90,400,60,true,false, actionevent ->  {
        try {
            List<Player> list = clientSup.get().serverMethod().getPlayers();
            if(list.isEmpty()) {
                currentPlayer = null;
                return;
            }
            int i = list.indexOf(clientSup.get().serverMethod().getPlayer(currentPlayer));
            if(i == -1 || i+1 >= list.size()) {
                currentPlayer = list.get(0).getName();
            } else {
                currentPlayer = list.get(i+1).getName();
            }
            update();
        } catch (RemoteException e) {
            clientSup.get().close();
        }
    });
    private final JLabel nameR = JUtils.addText("-",1479,90 + 13,400,30, SwingConstants.CENTER);
    private final JLabel moneyR = JUtils.addText("-",1579,340,200,28, SwingConstants.CENTER);
    private final JLabel busR = JUtils.addText("-",1579-100+17,317,67,22, SwingConstants.CENTER);
    private final JLabel freiR = JUtils.addText("-",1579+217,317,67,22, SwingConstants.CENTER);

    private final JLabel action1L = JUtils.addText("Würfeln",1060, 460,400,40,true);
    private final JButton action1B = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060, 450, 400, 80, true, false, actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player.getName(), ButtonC2SPacket.Button.ACTION_1), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel action2L = JUtils.addText("Zug beenden",1479, 460,400,40,true);
    private final JButton action2B = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1479,450,400,80,true,false,actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player.getName(), ButtonC2SPacket.Button.ACTION_2), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel purchasableL = JUtils.addText("Kaufen",1060,450+90+13,400,40,true);
    private final JButton purchasableB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90,400,80,true,false, actionevent ->  {
        //Straße kaufen
    });
    private final JLabel upgradeL = JUtils.addText("Aufwerten",1060,450+90*2+13,400,40,true);
    private final JButton upgradeB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*2,400,80,true,false, actionevent ->  {
        //haus bauen
    });
    private final JLabel downgradeL = JUtils.addText("Abwerten",1060,450+90*3+13,400,40,true);
    private final JButton downgradeB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*3,400,80,true,false, actionevent ->  {
        //haus verkaufen
    });
    private final JLabel mortgageL = JUtils.addText("Verpfänden",1060,450+90*4+13,400,40,true);
    private final JButton mortgageB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*4,400,80,true,false, actionevent ->  {
        //hypothek aufnehmen
    });
    private final JLabel tradeL = JUtils.addText("Handeln", 1060, 450+90*5+13,400,40,true);
    private final JButton tradeB = JUtils.addButton(null, "images/Main_pictures/3d_button.png", 1060, 450+90*5, 400, 80, true,false, actionEvent -> {
        JOptionPane.showMessageDialog(this, "Not available. Still in development.", "Trade", JOptionPane.WARNING_MESSAGE);
        //clientSup.get().tradeData.tradeState = TradeState.CHOOSE_PLAYER;
        //ClientTrade.trade(clientSup, displaySup.get().tradePane);
    });
    private final JLabel leaveL = JUtils.addText("Verlassen",1060,450+90*6+13,400,40,true);
    private final JButton leaveB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*6,400,80,true,false, actionevent ->  {
        //hypothek zurücksetzen
    });

    private final Runnable task = () -> {
        Client client = clientSup.get();
        while (!Thread.interrupted()) {
            if (clientSup.get() != client) {
                client = clientSup.get();
                update();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    };
    private Thread thread = new Thread(task);

    public PlayerInfoPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        topLeft.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addTopLeft();
        add(topLeft, DEFAULT_LAYER);
        topRight.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addTopRight();
        add(topRight, DEFAULT_LAYER);
        purchasableThis.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addPurchasableThis();
        add(purchasableThis, PALETTE_LAYER);
        purchasableOther.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addPurchasableOther();
        add(purchasableOther, PALETTE_LAYER);
        bottom.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addBottom();
        add(bottom, DEFAULT_LAYER);

    }

    public void reset() {
        setVisible(false);
        thread.interrupt();
    }

    public void init(Supplier<Client> clientSup) {
        try {
            this.clientSup = clientSup;
            currentPlayer = clientSup.get().serverMethod().getPlayers().get(0).getName();
            thread.interrupt();
            thread = new Thread(task);
            thread.start();
            setVisible(true);
            update();
        } catch (RemoteException e) {
            clientSup.get().close();
        }
    }

    public void setCurrentAndUpdate(String name) {
        if(!isVisible()) return;
        try {
            if(clientSup.get().serverMethod().getPlayer(name) != null) {
                currentPlayer = name;
                update();
            }
        } catch (RemoteException e) {
            clientSup.get().close();
        }
    }

    public void updateButtons(String activePlayer, boolean diceRolled, boolean hasToPayRent, boolean ready) {
        this.diceRolled = diceRolled;
        this.activePlayer = activePlayer;
        this.hasToPayRent = hasToPayRent;
        this.ready = ready;
        boolean active = activePlayer != null && activePlayer.equals(clientSup.get().player.getName());
        action1L.setText(diceRolled ? "Zug Beenden" : "Würfeln");
        action2L.setText(diceRolled ? "Miete Zahlen" : "Busfahren");
        setIcon(action1B, false);
        setIcon(action2B, false);
        setIcon(purchasableB, false);
        setIcon(upgradeB, false);
        setIcon(downgradeB, false);
        setIcon(mortgageB, false);
        setIcon(tradeB, false);
        setIcon(leaveB, true);
        if(active) {
            try {
                Player player = clientSup.get().serverMethod().getPlayer(clientSup.get().player.getName());
                IField field = Field.getAll().get(player.getPosition());
                if(diceRolled) {
                    setIcon(action1B, ready);
                    setIcon(action2B, hasToPayRent);
                    if(field instanceof IPurchasable purchasable) mortgageL.setText(purchasable.isMortgaged() ? "Zurückkaufen" : "Verpfänden");
                    setIcon(purchasableB, field instanceof IPurchasable purchasable && purchasable.getOwner() == null && player.getMoney() >= purchasable.getPrice());
                    setIcon(upgradeB, field instanceof IPurchasable purchasable && purchasable.getOwner().equals(player.getName()) && player.getMoney() >= purchasable.getUpgradeCost() && purchasable.getMaxLevel() > purchasable.getLevel());
                    setIcon(downgradeB, field instanceof IPurchasable purchasable && purchasable.getOwner().equals(player.getName()) && purchasable.getLevel() > 0);
                    setIcon(mortgageB, field instanceof IPurchasable purchasable && purchasable.getOwner().equals(player.getName()));
                    setIcon(tradeB, false);
                } else {
                    setIcon(action1B, true);
                    setIcon(action2B, player.getBusCards() > 0);
                }
            } catch (RemoteException e) {
                clientSup.get().close();
            }
        }
    }

    private void setIcon(JButton button, boolean active) {
        button.setDisabledIcon(new ImageIcon(new ImageIcon("images/Main_pictures/3d_button.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        button.setPressedIcon(new ImageIcon(new ImageIcon("images/Main_pictures/3d_button_pressed.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        if(active) {
            button.setIcon(new ImageIcon(new ImageIcon("images/Main_pictures/3d_button_hell.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
            button.setEnabled(true);
        } else {
            button.setIcon(new ImageIcon(new ImageIcon("images/Main_pictures/3d_button.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
            button.setEnabled(false);
        }
    }

    public void update() {
        if(!isVisible()) return;
        updateTexts();
        updateImages();
        updateImages();
        updateButtons(activePlayer, diceRolled, hasToPayRent, ready);
    }

    private void updateTexts() {
        try {
            Player playerL = clientSup.get().serverMethod().getPlayer(clientSup.get().player.getName());
            if(playerL != null) {
                nameL.setText(playerL.getName());
                moneyL.setText(playerL.getMoney() + " €");
                busL.setText("" + playerL.getBusCards());
                freiL.setText("" + playerL.getPrisonCards());
            }
            Player playerR = clientSup.get().serverMethod().getPlayer(currentPlayer);
            if(playerR != null) {
                nameR.setText(playerR.getName());
                moneyR.setText(playerR.getMoney() + " €");
                busR.setText("" + playerR.getBusCards());
                freiR.setText("" + playerR.getPrisonCards());
            }
        } catch (RemoteException e) {
            clientSup.get().close();
        }
    }

    private void updateImages() {
        for(int i = 1; i <= PURCHASABLES.size(); i++) {
            IPurchasable card = PURCHASABLES.get(i-1);
            if(purchasableThis.getComponent(i-1) instanceof JLabel label) {
                ImageIcon icon = new ImageIcon(card.getOwner().equals(clientSup.get().player.getName()) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png");
                label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
            if(purchasableOther.getComponent(i-1) instanceof JLabel label) {
                ImageIcon icon = new ImageIcon(card.getOwner().equals(currentPlayer) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png");
                label.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
        }
    }

    private void addTopLeft() {
        topLeft.add(buttonL, PALETTE_LAYER);
        topLeft.add(nameL, MODAL_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/Player_property.png",1060,135,400,247), DEFAULT_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/money_underlay.png",1160,340,200,33), PALETTE_LAYER);
        topLeft.add(moneyL, MODAL_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/busfahrkarte_rechts.png",1160-100+15,250,70,90), PALETTE_LAYER);
        topLeft.add(JUtils.addText("Buskarte",1160-100+15,253,70,12,true), MODAL_LAYER);
        topLeft.add(busL, MODAL_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/gefängnisfrei_rechts.png",1160+215,250,70,90), PALETTE_LAYER);
        topLeft.add(JUtils.addText("Freikarte",1160+215,253,70,12,true), MODAL_LAYER);
        topLeft.add(freiL, MODAL_LAYER);
    }

    private void addTopRight() {
        topRight.add(buttonR, PALETTE_LAYER);
        topRight.add(nameR, MODAL_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/Player_property.png",1479,135,400,247), DEFAULT_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/money_underlay.png",1579,340,200,33), PALETTE_LAYER);
        topRight.add(moneyR, MODAL_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/busfahrkarte_rechts.png",1579-100+15,250,70,90), PALETTE_LAYER);
        topRight.add(JUtils.addText("Buskarte",1579-100+15,253,70,12,true), MODAL_LAYER);
        topRight.add(busR, MODAL_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/gefängnisfrei_rechts.png",1579+215,250,70,90), PALETTE_LAYER);
        topRight.add(JUtils.addText("Freikarte",1579+215,253,70,12,true), MODAL_LAYER);
        topRight.add(freiR, MODAL_LAYER);
    }

    private void addBottom() {
        bottom.add(action1B, DEFAULT_LAYER);
        bottom.add(action1L, PALETTE_LAYER);
        bottom.add(action2B, DEFAULT_LAYER);
        bottom.add(action2L, PALETTE_LAYER);
        bottom.add(purchasableB, DEFAULT_LAYER);
        bottom.add(purchasableL, PALETTE_LAYER);
        bottom.add(upgradeB, DEFAULT_LAYER);
        bottom.add(upgradeL, PALETTE_LAYER);
        bottom.add(downgradeB, DEFAULT_LAYER);
        bottom.add(downgradeL, PALETTE_LAYER);
        bottom.add(mortgageB, DEFAULT_LAYER);
        bottom.add(mortgageL, PALETTE_LAYER);
        bottom.add(tradeB, DEFAULT_LAYER);
        bottom.add(tradeL, PALETTE_LAYER);
        bottom.add(leaveB, DEFAULT_LAYER);
        bottom.add(leaveL, PALETTE_LAYER);
    }

    private void addPurchasableThis() {
        for(int i = 1; i <= PURCHASABLES.size(); i++) {
            purchasableThis.add(JUtils.addImage("images/kleine_karten/disabled.png", 1075 + getX(i), 148 + getY(i)), i-1);
        }
    }

    private void addPurchasableOther() {
        for(int i = 1; i <= PURCHASABLES.size(); i++) {
            purchasableOther.add(JUtils.addImage("images/kleine_karten/disabled.png", 1494 + getX(i), 148 + getY(i)), i-1);
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
            return 5;
        } else if(id <= 23) {
            return 53;
        } else if(id <= 30) {
            return 99;
        } else if(id <= 37) {
            return 147;
        } else return 0;
    }
}
