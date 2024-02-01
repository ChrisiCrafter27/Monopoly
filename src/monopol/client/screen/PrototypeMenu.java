package monopol.client.screen;

import monopol.client.Client;
import monopol.client.ClientTrade;
import monopol.common.core.GameState;
import monopol.common.core.Monopoly;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.data.Plant;
import monopol.common.packets.PacketManager;
import monopol.common.packets.custom.RollDiceC2SPacket;
import monopol.common.utils.JUtils;
import monopol.common.utils.KeyHandler;
import monopol.common.message.DisconnectReason;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PrototypeMenu {
    public final JFrame frame = new JFrame("Monopoly - PrototypeWindow");
    private final ArrayList<Client> clients = new ArrayList<>();
    public Client client;
    private String ip;
    private final KeyHandler keyHandler = new KeyHandler();
    private final RootPane root = new RootPane();

    public PrototypeMenu() {
        if((int) JUtils.SCREEN_WIDTH / (int) JUtils.SCREEN_HEIGHT != 16 / 9) System.err.println("[WARN]: Deine Bildschirmauflösung ist nicht 16/9. Dadurch werden einige Dinge nicht richtig angezeigt. Es ist allerdings trotzdem möglich, so zu spielen.");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setSize(new Dimension((int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT));
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.addKeyListener(keyHandler);
        frame.setFocusTraversalKeysEnabled(false);
        ImageIcon icon = new ImageIcon(new ImageIcon("images/Main_pictures/icon.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        frame.setIconImage(icon.getImage());
        frame.add(root);
        focusThread();
    }

    private void focusThread() {
        new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    KeyboardFocusManager kbdFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                    if(kbdFocusManager.getFocusOwner() != kbdFocusManager.getFocusedWindow() && kbdFocusManager.getFocusedWindow() == frame) frame.requestFocus();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();
    }

    public void prepareMenu() {
        Monopoly.INSTANCE.setState(GameState.MAIN_MENU);
        if(gameThread.isAlive()) gameThread.interrupt();

        root.lobbyPane.reset();
        root.pingPane.reset();
        root.playerPane.reset();
        root.selectedCardPane.reset();
        root.playerDisplayPane.reset();
        root.infoPane.reset();
        root.rejoinPane.reset();
        root.boardPane.reset();
        root.freeParkingPane.reset();
        root.playerInfoPane.reset();
        root.playerDicePane.reset();

        root.menuPane.init(clients, this::prepareLobby, root);
        root.rejoinPane.init(() -> client, newClient -> {
            clients.add(clients.size(), newClient);
        });
    }

    public void prepareLobby(Client currentClient) {
        client = currentClient;
        try {
            ip = client.serverMethod().getIp();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        Monopoly.INSTANCE.setState(GameState.LOBBY);
        if(gameThread.isAlive()) gameThread.interrupt();

        Thread lobbyThread = new Thread() {
            @Override
            public void run() {

                //Reset menu pane
                root.menuPane.reset();

                //Initiate panes
                root.lobbyPane.init();
                root.playerPane.init();

                //Wait for the server connection
                while(!isInterrupted() && client.player.getName() == null) {
                    if(client.closed()) {
                        interrupt();
                        prepareMenu();
                        return;
                    }
                }

                //While on the server and in lobby
                while(!isInterrupted()) {

                    //Get the client from the panels
                    Client oldClient = client;
                    if(root.lobbyPane.getClient() != null) client = root.lobbyPane.getClient();
                    if(root.playerPane.getClient() != null && client.equals(oldClient)) client = root.playerPane.getClient();

                    //Remove clients that left the game
                    for (int i = 0; i < clients.size(); i++) {
                        if(clients.get(i).closed()) clients.remove(clients.get(i));
                    }
                    if(!clients.contains(client)) {
                        if(!clients.isEmpty()) {
                            client = clients.get(0);
                        } else {
                            interrupt();
                            prepareMenu();
                            return;
                        }
                    }

                    //Try to get information from the server and update
                    try {
                        root.lobbyPane.update(client.serverMethod().getPlayers(), client, clients, ip, keyHandler, false, root);
                        root.playerPane.update(client, clients, root.lobbyPane.mustUpdate());
                        root.pingPane.update(client.getPing(), keyHandler, root, () -> {
                            try {
                                client.serverMethod().kick(client.player.getName(), DisconnectReason.CLIENT_CLOSED);
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                            }
                        });
                    } catch (RemoteException e) {
                        e.printStackTrace(System.err);
                        client.close();
                        interrupt();
                        prepareMenu();
                        return;
                    }
                    if(Monopoly.INSTANCE.getState() == GameState.RUNNING) {
                        interrupt();
                        prepareGame();
                        return;
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        lobbyThread.start();
    }

    Thread gameThread = new Thread(() -> {throw new IllegalStateException();});

    public void prepareGame() {
        Monopoly.INSTANCE.setState(GameState.RUNNING);
        gameThread.interrupt();

        root.lobbyPane.reset();
        //keep PlayerPane enabled
        //keep PingPane enabled

        root.boardPane.init(root.selectedCardPane::init);
        root.playerDisplayPane.init(() -> root);
        root.infoPane.init(() -> client);
        root.freeParkingPane.init();
        root.playerInfoPane.init(() -> client, () -> root);

        new Thread(() -> {
            boolean keyDown = false;
            while (Monopoly.INSTANCE.getState() == GameState.RUNNING) {
                if (keyHandler.isKeyDown(KeyEvent.VK_M)) {
                    if(!keyDown) {
                        PacketManager.sendC2S(new RollDiceC2SPacket(client.player.getName()), client, e -> {});
                    }
                    keyDown = true;
                } else keyDown = false;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();

        new Thread(() -> {
            while (Monopoly.INSTANCE.getState() == GameState.RUNNING) {
                for (int i = 0; i < clients.size(); i++) {
                    if(clients.get(i).closed()) clients.remove(clients.get(i));
                }
                if(!clients.contains(client)) {
                    if(!clients.isEmpty()) {
                        client = clients.get(0);
                    } else {
                        prepareMenu();
                        return;
                    }
                }
                if(root.playerPane.getClient() != null) {
                    Client oldClient = client;
                    client = root.playerPane.getClient();
                    if(client != oldClient) ClientTrade.trade(() -> client, root.tradePane);
                }
                root.playerPane.update(client, clients, false);
                root.pingPane.update(client.getPing(), keyHandler, root, () -> {
                    try {
                        client.serverMethod().kick(client.player.getName(), DisconnectReason.CLIENT_CLOSED);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
        frame.repaint();
    }

    public static void main(String[] args) {
        for(Street street : Street.values()) street.setOwner("Spieler 1");
        for(TrainStation trainStation : TrainStation.values()) trainStation.setOwner("Spieler 1");
        for(Plant plant : Plant.values()) plant.setOwner("Spieler 1");
        Monopoly.main(args);
    }
}

