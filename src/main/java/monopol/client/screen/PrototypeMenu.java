package monopol.client.screen;

import monopol.common.data.TrainStation;
import monopol.client.Client;
import monopol.common.core.GameState;
import monopol.common.core.Monopoly;
import monopol.common.data.Street;
import monopol.common.data.Plant;
import monopol.common.utils.JUtils;
import monopol.common.utils.KeyHandler;
import monopol.common.message.DisconnectReason;
import monopol.common.utils.StartupProgressBar;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PrototypeMenu {
    public final JFrame frame = new JFrame("Monopoly");
    private final ArrayList<Client> clients = new ArrayList<>();
    public Client client;
    private String ip;
    private final KeyHandler keyHandler = new KeyHandler();
    private final RootPane display = new RootPane(this::prepareGame);

    public PrototypeMenu(StartupProgressBar bar) {
        if((int) JUtils.SCREEN_WIDTH / (int) JUtils.SCREEN_HEIGHT != 16 / 9) System.err.println("[WARN]: Deine Bildschirmauflösung ist nicht 16/9. Dadurch werden einige Dinge nicht richtig angezeigt. Es ist allerdings trotzdem möglich, so zu spielen.");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setSize(new Dimension((int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT));
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setOpacity(0);
        frame.setVisible(true);
        frame.addKeyListener(keyHandler);
        frame.setFocusTraversalKeysEnabled(false);
        ImageIcon icon = new ImageIcon(JUtils.imageIcon("images/Main_pictures/frame_icon.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        frame.setIconImage(icon.getImage());
        frame.add(display);
        focusThread();
        if(bar != null) opacityThread(bar);
    }
    private void focusThread() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                KeyboardFocusManager kbdFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                if(kbdFocusManager.getFocusOwner() != kbdFocusManager.getFocusedWindow() && kbdFocusManager.getFocusedWindow() == frame) frame.requestFocus();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }

    private void opacityThread(StartupProgressBar bar) {
        new Thread(() -> {
            while (!Thread.interrupted() && frame.getOpacity() <= 0.999f) {
                frame.setOpacity(frame.getOpacity() + 0.003f);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
            }
            frame.setOpacity(1);
            bar.close();
        }).start();
    }

    public void prepareMenu() {
        Monopoly.INSTANCE.setState(GameState.MENU);
        display.resetStart();

        display.lobbyPane.reset();
        display.pingPane.reset();
        display.playerSelectPane.reset();
        display.selectedCardPane.reset();
        display.playerDisplayPane.reset();
        display.infoBoxPane.reset();
        display.rejoinPane.reset();
        display.boardPane.reset();
        display.freeParkingPane.reset();
        display.playerInfoPane.reset();
        display.buttonsPane.reset();
        display.dicePane.reset();
        display.cardDecksPane.reset();
        display.housePane.reset();

        display.menuPane.init(clients, this::prepareLobby, display);
        display.rejoinPane.init(() -> client, newClient -> clients.add(clients.size(), newClient));
    }

    public void prepareLobby(Client currentClient) {
        Monopoly.INSTANCE.setState(GameState.LOBBY);

        client = currentClient;
        try {
            ip = client.serverMethod().getIp();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        Thread lobbyThread = new Thread() {
            @Override
            public void run() {

                //Interrupt if game state changed
                if(Monopoly.INSTANCE.getState() != GameState.LOBBY) {
                    interrupt();
                    return;
                }

                //Reset menu pane
                display.menuPane.reset();

                //Initiate panes
                display.lobbyPane.init();
                display.playerSelectPane.init();

                //Wait for the server connection
                while(!isInterrupted() && client.player().getName() == null) {
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
                    if(display.lobbyPane.getClient() != null) client = display.lobbyPane.getClient();
                    if(display.playerSelectPane.getClient() != null && client.equals(oldClient)) client = display.playerSelectPane.getClient();
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
                        display.lobbyPane.update(client.serverMethod().getPlayers(), client, clients, ip, keyHandler, false, display);
                        display.playerSelectPane.update(client, clients, display.lobbyPane.mustUpdate());
                        display.pingPane.update(client.getPing(), keyHandler, display, () -> {
                            try {
                                client.serverMethod().kick(client.player().getName(), DisconnectReason.CLIENT_CLOSED);
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

                    //Wait 100ms
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        lobbyThread.start();
    }

    public void prepareGame() {
        Monopoly.INSTANCE.setState(GameState.RUNNING);

        //Reset lobby pane
        display.lobbyPane.reset();

        //keep PlayerPane enabled
        //keep PingPane enabled

        //Initiate panes
        display.boardPane.init(() -> display);
        display.playerDisplayPane.init(() -> client, () -> display);
        display.infoBoxPane.init(() -> client);
        display.freeParkingPane.init();
        display.playerInfoPane.init(() -> client);
        display.buttonsPane.init(() -> client, () -> display);
        display.dicePane.showWithoutAnim(6, 6, 6);
        display.selectedCardPane.init(() -> display);
        display.housePane.init();
        display.cardDecksPane.init(() -> client);

        new Thread(() -> {
            //While game is running
            while (Monopoly.INSTANCE.getState() == GameState.RUNNING) {

                //Get the client from the panels
                Client oldClient = client;
                if(display.lobbyPane.getClient() != null) client = display.lobbyPane.getClient();
                if(display.playerSelectPane.getClient() != null && client.equals(oldClient)) client = display.playerSelectPane.getClient();

                //Remove clients that left the game
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

                //Try to get information from the server and update
                display.playerSelectPane.update(client, clients, display.lobbyPane.mustUpdate());
                display.pingPane.update(client.getPing(), keyHandler, display, () -> {
                    try {
                        client.serverMethod().kick(client.player().getName(), DisconnectReason.CLIENT_CLOSED);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                });

                //Wait 100ms
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        for(Street street : Street.values()) street.setOwner("Spieler 1");
        for(TrainStation trainStation : TrainStation.values()) trainStation.setOwner("Spieler 2");
        for(Plant plant : Plant.values()) plant.setOwner("Spieler 2");
        Monopoly.main(args);
    }
}
