package monopol.client.screen;

import monopol.client.Client;
import monopol.common.data.*;
import monopol.common.message.DisconnectReason;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.ButtonC2SPacket;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ButtonsPane extends JLayeredPane {
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};
    private Supplier<RootPane> displaySup = () -> {throw new IllegalStateException("init() was not called");};
    private String activePlayer = null;
    private boolean diceRolled;
    private boolean hasToPayRent;
    private boolean inPrison;
    private boolean ready;
    private List<IPurchasable> purchasables;

    private final JLayeredPane bottom = new JLayeredPane();

    private final JLabel action1L = JUtils.addText("Würfeln",1060, 460,400,40,true);
    private final JButton action1B = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060, 450, 400, 80, true, false, actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player().getName(), displaySup.get().selectedCardPane.getSelected().getName(), ButtonC2SPacket.Button.ACTION_1), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel action2L = JUtils.addText("Zug beenden",1484, 460,400,40,true);
    private final JButton action2B = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1484,450,400,80,true,false,actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player().getName(), displaySup.get().selectedCardPane.getSelected().getName(), ButtonC2SPacket.Button.ACTION_2), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel purchasableL = JUtils.addText("Kaufen",1060,450+90+13,400,40,true);
    private final JButton purchasableB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90,400,80,true,false, actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player().getName(), displaySup.get().selectedCardPane.getSelected().getName(), ButtonC2SPacket.Button.PURCHASE), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel upgradeL = JUtils.addText("Aufwerten",1060,450+90*2+13,400,40,true);
    private final JButton upgradeB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*2,400,80,true,false, actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player().getName(), displaySup.get().selectedCardPane.getSelected().getName(), ButtonC2SPacket.Button.UPGRADE), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel downgradeL = JUtils.addText("Abwerten",1060,450+90*3+13,400,40,true);
    private final JButton downgradeB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*3,400,80,true,false, actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player().getName(), displaySup.get().selectedCardPane.getSelected().getName(), ButtonC2SPacket.Button.DOWNGRADE), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel mortgageL = JUtils.addText("Verpfänden",1060,450+90*4+13,400,40,true);
    private final JButton mortgageB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*4,400,80,true,false, actionevent ->  {
        PacketManager.sendC2S(new ButtonC2SPacket(clientSup.get().player().getName(), displaySup.get().selectedCardPane.getSelected().getName(), ButtonC2SPacket.Button.MORTGAGE), clientSup.get(), Throwable::printStackTrace);
    });
    private final JLabel tradeL = JUtils.addText("Handeln", 1060, 450+90*5+13,400,40,true);
    private final JButton tradeB = JUtils.addButton(null, "images/Main_pictures/3d_button.png", 1060, 450+90*5, 400, 80, true,false, actionEvent -> {
        JOptionPane.showMessageDialog(this, "Not available. Still in development.", "Trade", JOptionPane.WARNING_MESSAGE);
        //clientSup.get().tradeData.tradeState = TradeState.CHOOSE_PLAYER;
        //ClientTrade.trade(clientSup, displaySup.get().tradePane);
    });
    private final JLabel leaveL = JUtils.addText("Verlassen",1060,450+90*6+13,400,40,true);
    private final JButton leaveB = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060,450+90*6,400,80,true,false, actionevent ->  {
        if(JOptionPane.showConfirmDialog(this, "Möchtest du den Server wirklich verlassen?", "Server verlassen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
            try {clientSup.get().serverMethod().kick(clientSup.get().player().getName(), DisconnectReason.CLIENT_CLOSED);} catch (RemoteException e) {clientSup.get().closed();}
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

    public ButtonsPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        bottom.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addAll();
        add(bottom, JLayeredPane.DEFAULT_LAYER);
    }

    public void reset() {
        setVisible(false);
        thread.interrupt();
    }

    public void init(Supplier<Client> clientSup, Supplier<RootPane> displaySup) {
        this.clientSup = clientSup;
        this.displaySup = displaySup;
        thread.interrupt();
        thread = new Thread(task);
        thread.start();
        setVisible(true);
        update();
    }

    public void setCurrentAndUpdate(String name) {
        if(!isVisible()) return;
        try {
            if(clientSup.get().serverMethod().getPlayer(name) != null) {
                update();
            }
        } catch (RemoteException e) {
            clientSup.get().close();
        }
    }

    public void update() {
        if(isVisible()) update(activePlayer, diceRolled, hasToPayRent, inPrison, ready, purchasables);
    }

    public void update(String activePlayer, boolean diceRolled, boolean hasToPayRent, boolean inPrison, boolean ready, List<IPurchasable> purchasables) {
        this.diceRolled = diceRolled;
        this.activePlayer = activePlayer;
        this.hasToPayRent = hasToPayRent;
        this.inPrison = inPrison;
        this.ready = ready;
        this.purchasables = purchasables;
        boolean active = activePlayer != null && activePlayer.equals(clientSup.get().player().getName());
        action1L.setText(diceRolled ? "Zug beenden" : "Würfeln");
        action2L.setText(diceRolled ? "Miete zahlen" : inPrison ? "Freikaufen" : "Busfahren");
        setIcon(leaveB, true);
        if(active) {
            try {
                Player player = clientSup.get().serverMethod().getPlayer(clientSup.get().player().getName());
                if(player == null) return;
                if(player.getMoney() <= 0) enableGiveUp();
                IPurchasable purchasable = displaySup.get().selectedCardPane.getSelected();
                if(diceRolled) {
                    setIcon(action1B, ready);
                    setIcon(action2B, hasToPayRent);
                    mortgageL.setText(purchasable.isMortgaged() ? "Zurückkaufen" : "Verpfänden");
                    setIcon(purchasableB, purchasables.stream().map(IPurchasable::getName).anyMatch(purchasable.getName()::equals) && player.getPosition() == Field.fields().indexOf(purchasable) && purchasable.getOwner().isEmpty() && player.getMoney() >= purchasable.getPrice());
                    setIcon(upgradeB, purchasables.stream().map(IPurchasable::getName).anyMatch(purchasable.getName()::equals) && purchasable.getOwner().equals(player.getName()) && player.getMoney() >= purchasable.getUpgradeCost() && purchasable.getMaxLevel() > purchasable.getLevel());
                    setIcon(downgradeB, purchasables.stream().map(IPurchasable::getName).anyMatch(purchasable.getName()::equals) && purchasable.getOwner().equals(player.getName()) && purchasable.getLevel() > 0);
                    setIcon(mortgageB, purchasable.getOwner().equals(player.getName()));
                    setIcon(tradeB, false);
                } else {
                    setIcon(action1B, true);
                    setIcon(action2B, player.getBusCards() > 0 || inPrison);
                }
            } catch (RemoteException e) {
                clientSup.get().close();
            }
        } else {
            setIcon(action1B, false);
            setIcon(action2B, false);
            setIcon(purchasableB, false);
            setIcon(upgradeB, false);
            setIcon(downgradeB, false);
            setIcon(mortgageB, false);
            setIcon(tradeB, false);
        }
    }

    private void enableGiveUp() {
        action1B.setDisabledIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button_red.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        action1B.setPressedIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button_pressed_red.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        action1B.setIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button_hell_red.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        action1B.setEnabled(true);
        action1L.setText("Aufgeben");
    }

    private void setIcon(JButton button, boolean active) {
        button.setDisabledIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        button.setPressedIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button_pressed.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
        if(active) {
            button.setIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button_hell.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
            button.setEnabled(true);
        } else {
            button.setIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/3d_button.png").getImage().getScaledInstance(JUtils.getX(400), JUtils.getY(80), Image.SCALE_SMOOTH)));
            button.setEnabled(false);
        }
    }

    private void addAll() {
        bottom.add(action1B, JLayeredPane.DEFAULT_LAYER);
        bottom.add(action1L, JLayeredPane.PALETTE_LAYER);
        bottom.add(action2B, JLayeredPane.DEFAULT_LAYER);
        bottom.add(action2L, JLayeredPane.PALETTE_LAYER);
        bottom.add(purchasableB, JLayeredPane.DEFAULT_LAYER);
        bottom.add(purchasableL, JLayeredPane.PALETTE_LAYER);
        bottom.add(upgradeB, JLayeredPane.DEFAULT_LAYER);
        bottom.add(upgradeL, JLayeredPane.PALETTE_LAYER);
        bottom.add(downgradeB, JLayeredPane.DEFAULT_LAYER);
        bottom.add(downgradeL, JLayeredPane.PALETTE_LAYER);
        bottom.add(mortgageB, JLayeredPane.DEFAULT_LAYER);
        bottom.add(mortgageL, JLayeredPane.PALETTE_LAYER);
        bottom.add(tradeB, JLayeredPane.DEFAULT_LAYER);
        bottom.add(tradeL, JLayeredPane.PALETTE_LAYER);
        bottom.add(leaveB, JLayeredPane.DEFAULT_LAYER);
        bottom.add(leaveL, JLayeredPane.PALETTE_LAYER);
    }
}
