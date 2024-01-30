package monopol.client.screen;

import monopol.client.Client;
import monopol.common.data.Player;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Supplier;

public class PlayerInfoPane extends JLayeredPane {

    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init()  was not called");};
    private Supplier<RootPane> displaySup = () -> {throw new IllegalStateException("init()  was not called");};
    private String currentPlayer = null;

    private final JLayeredPane topLeft = new JLayeredPane();
    private final JLayeredPane topRight = new JLayeredPane();
    private final JLayeredPane bottom = new JLayeredPane();

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

    private final JLabel action1L = JUtils.addText("Würfeln",1260-70, 463,160,40,false);
    private final JButton action1B = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1060, 450, 400, 80, true, false, actionevent ->  {
        //würfeln
    });
    private final JLabel action2L = JUtils.addText("Zug beenden",1679-105, 460,400,40,false);
    private final JButton action2B = JUtils.addButton(null,"images/Main_pictures/3d_button.png", 1479,450,400,80,true,false,actionevent ->  {
        //zug beenden
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
        bottom.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addBottom();
        add(bottom, DEFAULT_LAYER);

    }

    public void reset() {
        setVisible(false);
        thread.interrupt();
    }

    public void init(Supplier<Client> clientSup, Supplier<RootPane> displaySup) {
        try {
            this.clientSup = clientSup;
            this.displaySup = displaySup;
            currentPlayer = clientSup.get().serverMethod().getPlayers().get(0).getName();
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

    public void update() {
        if(!isVisible()) return;
        updateTexts();
        updateImages();
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
}
