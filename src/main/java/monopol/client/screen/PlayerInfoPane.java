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

public class PlayerInfoPane extends JLayeredPane {
    private static final List<IPurchasable> PURCHASABLES;
    static {
        PURCHASABLES = new ArrayList<>();
        PURCHASABLES.addAll(List.of(Street.values()));
        PURCHASABLES.addAll(List.of(TrainStation.values()));
        PURCHASABLES.addAll(List.of(Plant.values()));
    }
    
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};
    private Supplier<RootPane> displaySup = () -> {throw new IllegalStateException("init() was not called");};
    private String currentPlayer = null;

    private final JLayeredPane topLeft = new JLayeredPane();
    private final JLayeredPane topRight = new JLayeredPane();
    private final JLayeredPane purchasableThis = new JLayeredPane();
    private final JLayeredPane purchasableOther = new JLayeredPane();

    private final JButton buttonL = JUtils.addButton(null,"images/Main_pictures/Player_display.png", 1060,90,400,60,true,false, actionevent -> {});
    private final JLabel nameL = JUtils.addText("-",1060,90 + 13,400,30, SwingConstants.CENTER);
    private final JLabel moneyL = JUtils.addText("-",1160,340,200,28, SwingConstants.CENTER);
    private final JLabel busL = JUtils.addText("-",1160-100+14,343,70,25, SwingConstants.CENTER);
    private final JLabel freiL = JUtils.addText("-",1160+214,343,70,25, SwingConstants.CENTER);
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
    private final JLabel busR = JUtils.addText("-",1579-100+14,343,70,25, SwingConstants.CENTER);
    private final JLabel freiR = JUtils.addText("-",1579+214,343,70,25, SwingConstants.CENTER);

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
        add(topLeft, JLayeredPane.DEFAULT_LAYER);
        topRight.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addTopRight();
        add(topRight, JLayeredPane.DEFAULT_LAYER);
        purchasableThis.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addPurchasableThis();
        add(purchasableThis, JLayeredPane.PALETTE_LAYER);
        purchasableOther.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        addPurchasableOther();
        add(purchasableOther, JLayeredPane.PALETTE_LAYER);
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

    public void update() {
        if(!isVisible()) return;
        updateTexts();
        updateImages();
    }

    private void updateTexts() {
        try {
            Player playerL = clientSup.get().serverMethod().getPlayer(clientSup.get().player().getName());
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
            if(purchasableThis.getComponent(i-1) instanceof JButton button) {
                ImageIcon icon = JUtils.imageIcon(card.getOwnerNotNull().equals(clientSup.get().player().getName()) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
            if(purchasableOther.getComponent(i-1) instanceof JButton button) {
                ImageIcon icon = JUtils.imageIcon(card.getOwnerNotNull().equals(currentPlayer) ? "images/kleine_karten/" + getColor(i) + "_filled.png" : "images/kleine_karten/" + getColor(i) + ".png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT)));
            }
        }
    }

    private void addTopLeft() {
        topLeft.add(buttonL, JLayeredPane.PALETTE_LAYER);
        topLeft.add(nameL, JLayeredPane.MODAL_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/Player_property.png",1060,135,400,247), JLayeredPane.DEFAULT_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/money_underlay.png",1160,340,200,33), JLayeredPane.PALETTE_LAYER);
        topLeft.add(moneyL, JLayeredPane.MODAL_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/busfahrkarte_rechts.png",1160-100+15,250,70,90), JLayeredPane.PALETTE_LAYER);
        topLeft.add(JUtils.addText("Buskarte",1160-100+15,253,70,12,true), JLayeredPane.MODAL_LAYER);
        topLeft.add(busL, JLayeredPane.MODAL_LAYER);
        topLeft.add(JUtils.addImage("images/Main_pictures/gefängnisfrei_rechts.png",1160+215,250,70,90), JLayeredPane.PALETTE_LAYER);
        topLeft.add(JUtils.addText("Freikarte",1160+215,253,70,12,true), JLayeredPane.MODAL_LAYER);
        topLeft.add(freiL, JLayeredPane.MODAL_LAYER);
    }

    private void addTopRight() {
        topRight.add(buttonR, JLayeredPane.PALETTE_LAYER);
        topRight.add(nameR, JLayeredPane.MODAL_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/Player_property.png",1479,135,400,247), JLayeredPane.DEFAULT_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/money_underlay.png",1579,340,200,33), JLayeredPane.PALETTE_LAYER);
        topRight.add(moneyR, JLayeredPane.MODAL_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/busfahrkarte_rechts.png",1579-100+15,250,70,90), JLayeredPane.PALETTE_LAYER);
        topRight.add(JUtils.addText("Buskarte",1579-100+15,253,70,12,true), JLayeredPane.MODAL_LAYER);
        topRight.add(busR, JLayeredPane.MODAL_LAYER);
        topRight.add(JUtils.addImage("images/Main_pictures/gefängnisfrei_rechts.png",1579+215,250,70,90), JLayeredPane.PALETTE_LAYER);
        topRight.add(JUtils.addText("Freikarte",1579+215,253,70,12,true), JLayeredPane.MODAL_LAYER);
        topRight.add(freiR, JLayeredPane.MODAL_LAYER);
    }

    private void addPurchasableThis() {
        for(int i = 1; i <= PURCHASABLES.size(); i++) {
            IPurchasable purchasable = PURCHASABLES.get(i-1);
            purchasableThis.add(JUtils.addButton("", "images/kleine_karten/disabled.png", 1075 + getX(i), 148 + getY(i), 20, 40, true, false, actionEvent -> displaySup.get().selectedCardPane.select(purchasable)), i-1);
        }
    }

    private void addPurchasableOther() {
        for(int i = 1; i <= PURCHASABLES.size(); i++) {
            IPurchasable purchasable = PURCHASABLES.get(i-1);
            purchasableOther.add(JUtils.addButton("", "images/kleine_karten/disabled.png", 1494 + getX(i), 148 + getY(i), 20, 40, true, false, actionEvent -> displaySup.get().selectedCardPane.select(purchasable)), i-1);
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
