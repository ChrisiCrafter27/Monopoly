package monopol.screen;

import com.fasterxml.jackson.core.JsonProcessingException;
import monopol.client.Client;
import monopol.client.ClientEvents;
import monopol.client.TradeState;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.data.IPurchasable;
import monopol.data.Street;
import monopol.data.TrainStation;
import monopol.data.Plant;
import monopol.message.PacketManager;
import monopol.message.packets.ToastPacket;
import monopol.server.ServerPlayer;
import monopol.utils.JUtils;
import monopol.utils.Json;
import monopol.utils.KeyHandler;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

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

        root.menuPane.init(clients, this::prepareLobby);
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
                    clients.removeIf(Client::closed);
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
                        root.lobbyPane.update(client.serverMethod().getServerPlayers(), client, clients, ip, keyHandler, false);
                        root.playerPane.update(client, clients, root.lobbyPane.mustUpdate());
                        root.pingPane.update(client.getPing(), keyHandler);
                    } catch (RemoteException e) {
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
        root.playerDisplayPane.init(Map.of("Player1", Color.YELLOW, "Player2", Color.RED, "Player3", Color.GREEN, "Player4", Color.BLUE, "Player5", Color.ORANGE, "Player6", Color.MAGENTA));

        new Thread(() -> {
            while (true) {
                String name = switch (new Random().nextInt(6)) {
                    case 0 -> "Player1";
                    case 1 -> "Player2";
                    case 2 -> "Player3";
                    case 3 -> "Player4";
                    case 4 -> "Player5";
                    case 5 -> "Player6";
                    default -> "";
                };
                root.playerDisplayPane.setPos(name, root.playerDisplayPane.getPos(name) >= 13*4 - 1 ? 0 : root.playerDisplayPane.getPos(name) + 1);
                if (keyHandler.isKeyDown(KeyEvent.VK_K)) {
                    PacketManager.sendS2C(new ToastPacket("Hi"), serverPlayer -> true, e -> {});
                    return;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();

        new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    if(root.playerPane.getClient() != null) {
                        Client oldClient = client;
                        client = root.playerPane.getClient();
                        if(client != oldClient) ClientEvents.trade(() -> client, root.tradePane);
                    }
                    root.playerPane.update(client, clients, false);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();

        //no repaint
        //frame.repaint();
        JButton handel = new JButton();
        frame.add(JUtils.addButton(handel,null, 1060, 450+90*5, 400, 80, true,"images/Main_pictures/3d_button.png", actionEvent -> {
            JOptionPane.showMessageDialog(frame, "Not available. Still in development.", "Trade", JOptionPane.WARNING_MESSAGE);
            if(true) return;
            client.tradeData.tradeState = TradeState.CHOOSE_PLAYER;
            ClientEvents.trade(() -> client, root.tradePane);
        }), 0);
        frame.add(JUtils.addText("Handel", 1060, 450+90*5+13,400,40,true),0);
        //frame.repaint();

        //TODO  \/  FABIANS PART  \/

        frame.add(JUtils.addImage("images/Main_pictures/Background_Right.png", 1020, 60));
        frame.add(JUtils.addImage("images/Main_pictures/hintergrund_links_mitte2.png", 90, 150));

        int[] currentPlayer = new int[1];

        JButton button1 = new JButton();
        JButton button2 = new JButton();
        JLabel label_button1 = new JLabel();
        JLabel label_button2 = new JLabel();
        JButton Würfeln = new JButton();
        JButton zugbeenden = new JButton();
        JButton straße_kaufen = new JButton();
        JButton hypotheken_aufnehmen = new JButton();
        JButton haus_bauen  = new JButton();
        JButton haus_verkaufen = new JButton();
        JButton einstellungen  =new JButton();



        int X = 1160;
        JLabel label_moneyCommpanion = JUtils.addText("----",X+95,342,200,30,false);
        JLabel busfahrkarten_Commpanion = JUtils.addText("-",X-57,307,20,30,false);
        JLabel gefaengnisfreikarte_Commpanion = JUtils.addText("-",X+243,315,13,24,false);

        X = 1579;
        JLabel label_moneyPlayer = JUtils.addText("----",X+95,342,200,30,false);
        JLabel busfahrkarten_player = JUtils.addText("-",X-57,307,20,30,false);
        JLabel gefaengnisfreikarte_player = JUtils.addText("-",X+243,315,13,24,false);

        int x = 1060 + 15;
        int y = 148;

        JLabel BADSTRASSE = JUtils.addImage("images/kleine_karten/brown.png",x+15,y,20,40);
        JLabel TURMSTRASSE = JUtils.addImage("images/kleine_karten/brown.png",x+45,y,20,40);
        JLabel STADIONSTRASSE = JUtils.addImage("images/kleine_karten/brown.png",x+75,y,20,40);

        JLabel CHAUSSESTRASSE = JUtils.addImage("images/kleine_karten/cyan.png",x+115,y,20,40);
        JLabel ELISENSTRASSE = JUtils.addImage("images/kleine_karten/cyan.png",x+145,y,20,40);
        JLabel POSTSTRASSE = JUtils.addImage("images/kleine_karten/cyan.png",x+175,y,20,40);
        JLabel TIERGARTENSTRASSE = JUtils.addImage("images/kleine_karten/cyan.png",x+205,y,20,40);

        JLabel SEESTRASSE = JUtils.addImage("images/kleine_karten/pink.png",x+245,y,20,40);
        JLabel HAFENSTRASSE = JUtils.addImage("images/kleine_karten/pink.png",x+275,y,20,40);
        JLabel NEUESTRASSE = JUtils.addImage("images/kleine_karten/pink.png",x+305,y,20,40);
        JLabel MARKTPLATZ = JUtils.addImage("images/kleine_karten/pink.png",x+335,y,20,40);

        JLabel MUENCHENERSTRASSE = JUtils.addImage("images/kleine_karten/orange.png",x,y+50,20,40);
        JLabel WIENERSTRASSE = JUtils.addImage("images/kleine_karten/orange.png",x+30,y+50,20,40);
        JLabel BERLINERSTRASSE = JUtils.addImage("images/kleine_karten/orange.png",x+60,y+50,20,40);
        JLabel HAMBURGERSTRASSE = JUtils.addImage("images/kleine_karten/orange.png",x+90,y+50,20,40);

        JLabel THEATERSTRASSE = JUtils.addImage("images/kleine_karten/red.png",x+130,y+50,20,40);
        JLabel MUSEUMSTRASSE = JUtils.addImage("images/kleine_karten/red.png",x+160,y+50,20,40);
        JLabel OPERNPLATZ = JUtils.addImage("images/kleine_karten/red.png",x+190,y+50,20,40);
        JLabel KONZERTHAUSSTRASSE = JUtils.addImage("images/kleine_karten/red.png",x+220,y+50,20,40);

        JLabel LESSINGSTRASSE = JUtils.addImage("images/kleine_karten/yellow.png",x+260,y+50,20,40);
        JLabel SCHILLERSTRASSE = JUtils.addImage("images/kleine_karten/yellow.png",x+290,y+50,20,40);
        JLabel GOETHESTRASSE = JUtils.addImage("images/kleine_karten/yellow.png",x+320,y+50,20,40);
        JLabel RILKESTRASSE = JUtils.addImage("images/kleine_karten/yellow.png",x+350,y+50,20,40);

        JLabel RATHAUSPLATZ = JUtils.addImage("images/kleine_karten/green.png",x+80,y+100,20,40);
        JLabel HAUPSTRASSE = JUtils.addImage("images/kleine_karten/green.png",x+110,y+100,20,40);
        JLabel BOERSENPLATZ = JUtils.addImage("images/kleine_karten/green.png",x+140,y+100,20,40);
        JLabel BAHNHOFSTRASSE = JUtils.addImage("images/kleine_karten/green.png",x+170,y+100,20,40);

        JLabel DOMPLATZ = JUtils.addImage("images/kleine_karten/blue.png",x+210,y+100,20,40);
        JLabel PARKSTRASSE = JUtils.addImage("images/kleine_karten/blue.png",x+240,y+100,20,40);
        JLabel SCHLOSSALLEE = JUtils.addImage("images/kleine_karten/blue.png",x+270,y+100,20,40);

        JLabel GASWERK = JUtils.addImage("images/kleine_karten/gas.png",x+210,y+150,20,40);
        JLabel ELEKTRIZITAETSWERK = JUtils.addImage("images/kleine_karten/elec.png",x+240,y+150,20,40);
        JLabel WASSERWERK = JUtils.addImage("images/kleine_karten/water.png",x+270,y+150,20,40);

        JLabel SUEDBAHNHOF = JUtils.addImage("images/kleine_karten/train.png",x+80,y+150,20,40);
        JLabel WESTBAHNHOF = JUtils.addImage("images/kleine_karten/train.png",x+110,y+150,20,40);
        JLabel NORDBAHNHOF = JUtils.addImage("images/kleine_karten/train.png",x+140,y+150,20,40);
        JLabel HAUPTBAHNHOF = JUtils.addImage("images/kleine_karten/train.png",x+170,y+150,20,40);

        x = 1479 + 15;
        y = 148;

        JLabel BADSTRASSE_Companion = JUtils.addImage("images/kleine_karten/brown.png",x+15,y,20,40);
        JLabel TURMSTRASSE_Companion = JUtils.addImage("images/kleine_karten/brown.png",x+45,y,20,40);
        JLabel STADIONSTRASSE_Companion = JUtils.addImage("images/kleine_karten/brown.png",x+75,y,20,40);

        JLabel CHAUSSESTRASSE_Companion = JUtils.addImage("images/kleine_karten/cyan.png",x+115,y,20,40);
        JLabel ELISENSTRASSE_Companion = JUtils.addImage("images/kleine_karten/cyan.png",x+145,y,20,40);
        JLabel POSTSTRASSE_Companion = JUtils.addImage("images/kleine_karten/cyan.png",x+175,y,20,40);
        JLabel TIERGARTENSTRASSE_Companion = JUtils.addImage("images/kleine_karten/cyan.png",x+205,y,20,40);

        JLabel SEESTRASSE_Companion = JUtils.addImage("images/kleine_karten/pink.png",x+245,y,20,40);
        JLabel HAFENSTRASSE_Companion = JUtils.addImage("images/kleine_karten/pink.png",x+275,y,20,40);
        JLabel NEUESTRASSE_Companion = JUtils.addImage("images/kleine_karten/pink.png",x+305,y,20,40);
        JLabel MARKTPLATZ_Companion = JUtils.addImage("images/kleine_karten/pink.png",x+335,y,20,40);

        JLabel MUENCHENERSTRASSE_Companion = JUtils.addImage("images/kleine_karten/orange.png",x,y+50,20,40);
        JLabel WIENERSTRASSE_Companion = JUtils.addImage("images/kleine_karten/orange.png",x+30,y+50,20,40);
        JLabel BERLINERSTRASSE_Companion = JUtils.addImage("images/kleine_karten/orange.png",x+60,y+50,20,40);
        JLabel HAMBURGERSTRASSE_Companion = JUtils.addImage("images/kleine_karten/orange.png",x+90,y+50,20,40);

        JLabel THEATERSTRASSE_Companion = JUtils.addImage("images/kleine_karten/red.png",x+130,y+50,20,40);
        JLabel MUSEUMSTRASSE_Companion = JUtils.addImage("images/kleine_karten/red.png",x+160,y+50,20,40);
        JLabel OPERNPLATZ_Companion = JUtils.addImage("images/kleine_karten/red.png",x+190,y+50,20,40);
        JLabel KONZERTHAUSSTRASSE_Companion = JUtils.addImage("images/kleine_karten/red.png",x+220,y+50,20,40);

        JLabel LESSINGSTRASSE_Companion = JUtils.addImage("images/kleine_karten/yellow.png",x+260,y+50,20,40);
        JLabel SCHILLERSTRASSE_Companion = JUtils.addImage("images/kleine_karten/yellow.png",x+290,y+50,20,40);
        JLabel GOETHESTRASSE_Companion = JUtils.addImage("images/kleine_karten/yellow.png",x+320,y+50,20,40);
        JLabel RILKESTRASSE_Companion = JUtils.addImage("images/kleine_karten/yellow.png",x+350,y+50,20,40);

        JLabel RATHAUSPLATZ_Companion = JUtils.addImage("images/kleine_karten/green.png",x+80,y+100,20,40);
        JLabel HAUPSTRASSE_Companion = JUtils.addImage("images/kleine_karten/green.png",x+110,y+100,20,40);
        JLabel BOERSENPLATZ_Companion = JUtils.addImage("images/kleine_karten/green.png",x+140,y+100,20,40);
        JLabel BAHNHOFSTRASSE_Companion = JUtils.addImage("images/kleine_karten/green.png",x+170,y+100,20,40);

        JLabel DOMPLATZ_Companion = JUtils.addImage("images/kleine_karten/blue.png",x+210,y+100,20,40);
        JLabel PARKSTRASSE_Companion = JUtils.addImage("images/kleine_karten/blue.png",x+240,y+100,20,40);
        JLabel SCHLOSSALLEE_Companion = JUtils.addImage("images/kleine_karten/blue.png",x+270,y+100,20,40);

        JLabel GASWERK_Companion = JUtils.addImage("images/kleine_karten/gas.png",x+210,y+150,20,40);
        JLabel ELEKTRIZITAETSWERK_Companion = JUtils.addImage("images/kleine_karten/elec.png",x+240,y+150,20,40);
        JLabel WASSERWERK_Companion = JUtils.addImage("images/kleine_karten/water.png",x+270,y+150,20,40);

        JLabel SUEDBAHNHOF_Companion = JUtils.addImage("images/kleine_karten/train.png",x+80,y+150,20,40);
        JLabel WESTBAHNHOF_Companion = JUtils.addImage("images/kleine_karten/train.png",x+110,y+150,20,40);
        JLabel NORDBAHNHOF_Companion = JUtils.addImage("images/kleine_karten/train.png",x+140,y+150,20,40);
        JLabel HAUPTBAHNHOF_Companion = JUtils.addImage("images/kleine_karten/train.png",x+170,y+150,20,40);


        gameThread = new Thread() {
            @Override
            public void run(){
                ServerPlayer serverPlayer;
                ServerPlayer oldServerPlayerSelected = null;
                ServerPlayer oldServerPlayerPlaying = null;
                Street street = Street.values()[0];
                while(!isInterrupted()) {

                    //If the selected player disconnected, check if there is another
                    clients.removeIf(Client::closed);
                    if(!clients.contains(client)) {
                        if(!clients.isEmpty()) {//MonopolyScreen.png
                            client = clients.get(0);
                        } else {
                            interrupt();
                            prepareMenu();
                            return;
                        }
                    }

                    try {
                        serverPlayer = client.serverMethod().getServerPlayers().get(currentPlayer[0]);
                    } catch (IndexOutOfBoundsException | RemoteException e) {
                        currentPlayer[0] = 0;
                        try {
                            serverPlayer = client.serverMethod().getServerPlayers().get(currentPlayer[0]);
                        } catch (IndexOutOfBoundsException | RemoteException e2) {
                            client.close();
                            continue;
                        }
                    }

                    try {
                        if(oldServerPlayerSelected == null) oldServerPlayerSelected = serverPlayer;
                        if(oldServerPlayerPlaying == null) oldServerPlayerPlaying = client.serverMethod().getServerPlayer(client.player.getName());
                    } catch (RemoteException e) {
                        client.close();
                        continue;
                    }

                    label_button1.setText(serverPlayer.getName());

                    //ClientEvents.updateOwner(client);
                    //client.player.getName()

                    label_moneyCommpanion.setIcon(new ImageIcon());
                    //System.out.println(label_moneyCommpanion.getIcon());

                    try {
                        label_moneyPlayer.setText(client.serverMethod().getServerPlayer(client.player.getName()).getMoney() + "€");
                        busfahrkarten_player.setText(client.serverMethod().getServerPlayer(client.player.getName()).getBusfahrkarten() + "");
                        gefaengnisfreikarte_player.setText(client.serverMethod().getServerPlayer(client.player.getName()).getGefaengniskarten() + "");
                    } catch (RemoteException e) {
                        client.close();
                    }
                    label_moneyCommpanion.setText(serverPlayer.getMoney() + "€");
                    busfahrkarten_Commpanion.setText(serverPlayer.getBusfahrkarten() + "");
                    gefaengnisfreikarte_Commpanion.setText(serverPlayer.getGefaengniskarten() + "");

                    BADSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png"));
                    TURMSTRASSE.setIcon(new ImageIcon(Street.TURMSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png"));
                    STADIONSTRASSE.setIcon(new ImageIcon(Street.STADIONSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png"));

                    CHAUSSESTRASSE.setIcon(new ImageIcon(Street.CHAUSSESTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));
                    ELISENSTRASSE.setIcon(new ImageIcon(Street.ELISENSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));
                    POSTSTRASSE.setIcon(new ImageIcon(Street.POSTSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));
                    TIERGARTENSTRASSE.setIcon(new ImageIcon(Street.TIERGARTENSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));

                    SEESTRASSE.setIcon(new ImageIcon(Street.SEESTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));
                    HAFENSTRASSE.setIcon(new ImageIcon(Street.HAFENSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));
                    NEUESTRASSE.setIcon(new ImageIcon(Street.NEUESTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));
                    MARKTPLATZ.setIcon(new ImageIcon(Street.MARKTPLATZ.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));

                    MUENCHENERSTRASSE.setIcon(new ImageIcon(Street.MUENCHENERSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));
                    WIENERSTRASSE.setIcon(new ImageIcon(Street.WIENERSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));
                    BERLINERSTRASSE.setIcon(new ImageIcon(Street.BERLINERSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));
                    HAMBURGERSTRASSE.setIcon(new ImageIcon(Street.HAMBURGERSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));

                    THEATERSTRASSE.setIcon(new ImageIcon(Street.THEATERSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));
                    MUSEUMSTRASSE.setIcon(new ImageIcon(Street.MUSEUMSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));
                    OPERNPLATZ.setIcon(new ImageIcon(Street.OPERNPLATZ.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));
                    KONZERTHAUSSTRASSE.setIcon(new ImageIcon(Street.KONZERTHAUSSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));

                    LESSINGSTRASSE.setIcon(new ImageIcon(Street.LESSINGSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));
                    SCHILLERSTRASSE.setIcon(new ImageIcon(Street.SCHILLERSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));
                    GOETHESTRASSE.setIcon(new ImageIcon(Street.GOETHESTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));
                    RILKESTRASSE.setIcon(new ImageIcon(Street.RILKESTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));

                    RATHAUSPLATZ.setIcon(new ImageIcon(Street.RATHAUSPLATZ.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));
                    HAUPSTRASSE.setIcon(new ImageIcon(Street.HAUPSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));
                    BOERSENPLATZ.setIcon(new ImageIcon(Street.BOERSENPLATZ.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));
                    BAHNHOFSTRASSE.setIcon(new ImageIcon(Street.BAHNHOFSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));

                    DOMPLATZ.setIcon(new ImageIcon(Street.DOMPLATZ.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png"));
                    PARKSTRASSE.setIcon(new ImageIcon(Street.PARKSTRASSE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png"));
                    SCHLOSSALLEE.setIcon(new ImageIcon(Street.SCHLOSSALLEE.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png"));

                    GASWERK.setIcon(new ImageIcon(Plant.GASWERK.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/gas_filled.png" : "images/kleine_karten/gas.png"));
                    ELEKTRIZITAETSWERK.setIcon(new ImageIcon(Plant.ELEKTRIZITAETSWERK.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/elec_filled.png" : "images/kleine_karten/elec.png"));
                    WASSERWERK.setIcon(new ImageIcon(Plant.WASSERWERK.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/water_filled.png" : "images/kleine_karten/water.png"));

                    SUEDBAHNHOF.setIcon(new ImageIcon(TrainStation.SUEDBAHNHOF.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));
                    WESTBAHNHOF.setIcon(new ImageIcon(TrainStation.WESTBAHNHOF.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));
                    NORDBAHNHOF.setIcon(new ImageIcon(TrainStation.NORDBAHNHOF.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));
                    HAUPTBAHNHOF.setIcon(new ImageIcon(TrainStation.HAUPTBAHNHOF.getOwner().equals(serverPlayer.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));


                    BADSTRASSE_Companion.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png"));
                    TURMSTRASSE_Companion.setIcon(new ImageIcon(Street.TURMSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png"));
                    STADIONSTRASSE_Companion.setIcon(new ImageIcon(Street.STADIONSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png"));

                    CHAUSSESTRASSE_Companion.setIcon(new ImageIcon(Street.CHAUSSESTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));
                    ELISENSTRASSE_Companion.setIcon(new ImageIcon(Street.ELISENSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));
                    POSTSTRASSE_Companion.setIcon(new ImageIcon(Street.POSTSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));
                    TIERGARTENSTRASSE_Companion.setIcon(new ImageIcon(Street.TIERGARTENSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png"));

                    SEESTRASSE_Companion.setIcon(new ImageIcon(Street.SEESTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));
                    HAFENSTRASSE_Companion.setIcon(new ImageIcon(Street.HAFENSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));
                    NEUESTRASSE_Companion.setIcon(new ImageIcon(Street.NEUESTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));
                    MARKTPLATZ_Companion.setIcon(new ImageIcon(Street.MARKTPLATZ.getOwner().equals(client.player.getName()) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png"));

                    MUENCHENERSTRASSE_Companion.setIcon(new ImageIcon(Street.MUENCHENERSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));
                    WIENERSTRASSE_Companion.setIcon(new ImageIcon(Street.WIENERSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));
                    BERLINERSTRASSE_Companion.setIcon(new ImageIcon(Street.BERLINERSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));
                    HAMBURGERSTRASSE_Companion.setIcon(new ImageIcon(Street.HAMBURGERSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png"));

                    THEATERSTRASSE_Companion.setIcon(new ImageIcon(Street.THEATERSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));
                    MUSEUMSTRASSE_Companion.setIcon(new ImageIcon(Street.MUSEUMSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));
                    OPERNPLATZ_Companion.setIcon(new ImageIcon(Street.OPERNPLATZ.getOwner().equals(client.player.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));
                    KONZERTHAUSSTRASSE_Companion.setIcon(new ImageIcon(Street.KONZERTHAUSSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png"));

                    LESSINGSTRASSE_Companion.setIcon(new ImageIcon(Street.LESSINGSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));
                    SCHILLERSTRASSE_Companion.setIcon(new ImageIcon(Street.SCHILLERSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));
                    GOETHESTRASSE_Companion.setIcon(new ImageIcon(Street.GOETHESTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));
                    RILKESTRASSE_Companion.setIcon(new ImageIcon(Street.RILKESTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png"));

                    RATHAUSPLATZ_Companion.setIcon(new ImageIcon(Street.RATHAUSPLATZ.getOwner().equals(client.player.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));
                    HAUPSTRASSE_Companion.setIcon(new ImageIcon(Street.HAUPSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));
                    BOERSENPLATZ_Companion.setIcon(new ImageIcon(Street.BOERSENPLATZ.getOwner().equals(client.player.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));
                    BAHNHOFSTRASSE_Companion.setIcon(new ImageIcon(Street.BAHNHOFSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png"));

                    DOMPLATZ_Companion.setIcon(new ImageIcon(Street.DOMPLATZ.getOwner().equals(client.player.getName()) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png"));
                    PARKSTRASSE_Companion.setIcon(new ImageIcon(Street.PARKSTRASSE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png"));
                    SCHLOSSALLEE_Companion.setIcon(new ImageIcon(Street.SCHLOSSALLEE.getOwner().equals(client.player.getName()) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png"));

                    GASWERK_Companion.setIcon(new ImageIcon(Plant.GASWERK.getOwner().equals(client.player.getName()) ? "images/kleine_karten/gas_filled.png" : "images/kleine_karten/gas.png"));
                    ELEKTRIZITAETSWERK_Companion.setIcon(new ImageIcon(Plant.ELEKTRIZITAETSWERK.getOwner().equals(client.player.getName()) ? "images/kleine_karten/elec_filled.png" : "images/kleine_karten/elec.png"));
                    WASSERWERK_Companion.setIcon(new ImageIcon(Plant.WASSERWERK.getOwner().equals(client.player.getName()) ? "images/kleine_karten/water_filled.png" : "images/kleine_karten/water.png"));

                    SUEDBAHNHOF_Companion.setIcon(new ImageIcon(TrainStation.SUEDBAHNHOF.getOwner().equals(client.player.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));
                    WESTBAHNHOF_Companion.setIcon(new ImageIcon(TrainStation.WESTBAHNHOF.getOwner().equals(client.player.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));
                    NORDBAHNHOF_Companion.setIcon(new ImageIcon(TrainStation.NORDBAHNHOF.getOwner().equals(client.player.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));
                    HAUPTBAHNHOF_Companion.setIcon(new ImageIcon(TrainStation.HAUPTBAHNHOF.getOwner().equals(client.player.getName()) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png"));

                    boolean shouldRepaint = false;
                    try {
                        for (Map.Entry<IPurchasable, String> entry : client.serverMethod().getOwnerMap().entrySet()) {
                            if(entry.getKey() instanceof Street value) {
                                for(Street value2 : Street.values()) {
                                    if(value2.name.equals(value.name)) shouldRepaint = shouldRepaint || !(value2.getOwner().equals(value.getOwner()));
                                }
                            }
                            else if(entry.getKey() instanceof TrainStation value) {
                                for(TrainStation value2 : TrainStation.values()) {
                                    if(value2.name.equals(value.name)) shouldRepaint = shouldRepaint || !(value2.getOwner().equals(value.getOwner()));
                                }
                            }
                            else if(entry.getKey() instanceof Plant value) {
                                for(Plant value2 : Plant.values()) {
                                    if(value2.name.equals(value.name)) shouldRepaint = shouldRepaint || !(value2.getOwner().equals(value.getOwner()));
                                }
                            } else System.out.println("error");
                        }
                    } catch (RemoteException e) {
                        client.close();
                        client.close();
                    }
                    if(shouldRepaint) ClientEvents.updateOwner(client);
                    if(shouldRepaint) System.out.println("Should repaint"); //Debug output
                    try {
                        if (!Json.toString(serverPlayer, false).equals(Json.toString(oldServerPlayerSelected, false)) || !Json.toString(client.serverMethod().getServerPlayer(client.player.getName()), false).equals(Json.toString(oldServerPlayerPlaying, false)) || shouldRepaint) {
                            frame.repaint();
                        }
                        oldServerPlayerSelected = serverPlayer;
                        oldServerPlayerPlaying = client.serverMethod().getServerPlayer(client.player.getName());
                    } catch (RemoteException e) {
                        client.close();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        if(!isInterrupted()) sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

            }
        };
        gameThread.start();



        frame.add(JUtils.addButton(button1,null,1060,90,400,60,true,"images/Main_pictures/Player_display.png", actionevent ->  {
            int maxPlayers;
            try {
                //Get the amount of players on the server
                maxPlayers = client.serverMethod().getServerPlayers().size();
            } catch (RemoteException e) {
                //If that's impossible, go to main menu
                gameThread.interrupt();
                prepareMenu();
                return;
            }

            if(currentPlayer[0] < maxPlayers - 1){
                //Try to switch to next player
                currentPlayer[0] = currentPlayer[0] + 1;
            } else if (maxPlayers > 0) {
                //If there is no one set to first player
                currentPlayer[0] = 0;
            } else {
                //If there is no one, go to main menu
                gameThread.interrupt();
                prepareMenu();
            }
        }),0);

        frame.add(JUtils.addButton(button2,null,1479,90,400,60,true,"images/Main_pictures/Player_display.png",actionevent ->  {}),0);
        frame.add(JUtils.addText(label_button1,"","Arial",1060,90 + 13,400,30,true),0);
        frame.add(JUtils.addText(label_button2, client.player.getName(),"Arial",1479,90 + 13,400,30,true),0);
        frame.add(JUtils.addImage("images/Main_pictures/Player_property.png",1060,135,400,247),0);
        frame.add(JUtils.addImage("images/Main_pictures/Player_property.png",1479,135,400,247),0);

        X = 1160;
        frame.add(JUtils.addImage("images/Main_pictures/money_underlay.png",X,340,200,33),0);
        frame.add(JUtils.addText("Geld: ",X+25,342,152,30,false),0);
        frame.add(label_moneyCommpanion,0);
        frame.add(JUtils.addImage("images/Main_pictures/busfahrkarte_rechts.png",X-100+15,250,70,90),0);
        frame.add(JUtils.addText("Busfahrkarte",X-100+15,259,100,12,false),0);
        frame.add(busfahrkarten_Commpanion,0);
        frame.add(JUtils.addImage("images/Main_pictures/gefängnisfrei_rechts.png",X+215,250,70,90),0);
        //frame.add(JUtils.addText("",X+223,253,160,10,false),0);
        frame.add(JUtils.addText("Knastfreikarte",X+218,255,160,10,false),0);
        frame.add(gefaengnisfreikarte_Commpanion,0);

        X = 1579;
        frame.add(JUtils.addImage("images/Main_pictures/money_underlay.png",X,340,200,33),0);
        frame.add(JUtils.addText("Geld: ",X+25,342,152,30,false),0);
        frame.add(label_moneyPlayer,0);
        frame.add(JUtils.addImage("images/Main_pictures/busfahrkarte_rechts.png",X-100+15,250,70,90),0);
        frame.add(JUtils.addText("Busfahrkarte",X-100+15,259,100,12,false),0);
        frame.add(busfahrkarten_player,0);
        frame.add(JUtils.addImage("images/Main_pictures/gefängnisfrei_rechts.png",X+215,250,70,90),0);
        //frame.add(JUtils.addText("",X+223,253,160,10,false),0);
        frame.add(JUtils.addText("Knastfreikarte",X+218,255,160,10,false),0);
        frame.add(gefaengnisfreikarte_player,0);

        frame.add(JUtils.addButton(Würfeln,null,1060,450,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //WÜRFELN
        }),0);
        frame.add(JUtils.addText("Würfeln",1260-70, 463,160,40,false),0);

        frame.add(JUtils.addButton(zugbeenden,null,1479,450,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //zugbeenden
        }),0);
        frame.add(JUtils.addText("Zugbeenden",1679-105, 460,400,40,false),0);

        int x_actoins = 1060;
        int y_actions = 450;

        frame.add(JUtils.addButton(straße_kaufen,null,x_actoins,y_actions+90,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //Straße kaufen
        }),0);
        frame.add(JUtils.addText("Straße kaufen",x_actoins,y_actions+90+13,400,40,true),0);

        frame.add(JUtils.addButton(haus_bauen,null,x_actoins,y_actions+90*2,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //haus bauen
        }),0);
        frame.add(JUtils.addText("Haus bauen",x_actoins,y_actions+90*2+13,400,40,true),0);

        frame.add(JUtils.addButton(haus_verkaufen,null,x_actoins,y_actions+90*3,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //haus verkaufen
        }),0);
        frame.add(JUtils.addText("Haus verkaufen",x_actoins,y_actions+90*3+13,400,40,true),0);

        frame.add(JUtils.addButton(hypotheken_aufnehmen,null,x_actoins,y_actions+90*4,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //hypotheken_aufnehmen
        }),0);
        frame.add(JUtils.addText("hypotheken",x_actoins,y_actions+90*4+13,400,40,true),0);

        frame.add(JUtils.addButton(einstellungen,null,x_actoins,y_actions+90*6,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //einstellungen
        }),0);
        frame.add(JUtils.addText("Einstellungen",x_actoins,y_actions+90*6+13,400,40,true),0);


        frame.add(BADSTRASSE,0);
        frame.add(TURMSTRASSE,0);
        frame.add(STADIONSTRASSE,0);

        frame.add(CHAUSSESTRASSE,0);
        frame.add(ELISENSTRASSE,0);
        frame.add(POSTSTRASSE,0);
        frame.add(TIERGARTENSTRASSE,0);

        frame.add(SEESTRASSE,0);
        frame.add(HAFENSTRASSE,0);
        frame.add(NEUESTRASSE,0);
        frame.add(MARKTPLATZ,0);

        frame.add(MUENCHENERSTRASSE,0);
        frame.add(WIENERSTRASSE,0);
        frame.add(BERLINERSTRASSE,0);
        frame.add(HAMBURGERSTRASSE,0);

        frame.add(THEATERSTRASSE,0);
        frame.add(MUSEUMSTRASSE,0);
        frame.add(OPERNPLATZ,0);
        frame.add(KONZERTHAUSSTRASSE,0);

        frame.add(LESSINGSTRASSE,0);
        frame.add(SCHILLERSTRASSE,0);
        frame.add(GOETHESTRASSE,0);
        frame.add(RILKESTRASSE,0);

        frame.add(RATHAUSPLATZ,0);
        frame.add(HAUPSTRASSE,0);
        frame.add(BOERSENPLATZ,0);
        frame.add(BAHNHOFSTRASSE,0);

        frame.add(DOMPLATZ,0);
        frame.add(PARKSTRASSE,0);
        frame.add(SCHLOSSALLEE,0);

        frame.add(GASWERK,0);
        frame.add(ELEKTRIZITAETSWERK,0);
        frame.add(WASSERWERK,0);

        frame.add(SUEDBAHNHOF,0);
        frame.add(WESTBAHNHOF,0);
        frame.add(NORDBAHNHOF,0);
        frame.add(HAUPTBAHNHOF,0);


        frame.add(BADSTRASSE_Companion,0);
        frame.add(TURMSTRASSE_Companion,0);
        frame.add(STADIONSTRASSE_Companion,0);

        frame.add(CHAUSSESTRASSE_Companion,0);
        frame.add(ELISENSTRASSE_Companion,0);
        frame.add(POSTSTRASSE_Companion,0);
        frame.add(TIERGARTENSTRASSE_Companion,0);

        frame.add(SEESTRASSE_Companion,0);
        frame.add(HAFENSTRASSE_Companion,0);
        frame.add(NEUESTRASSE_Companion,0);
        frame.add(MARKTPLATZ_Companion,0);

        frame.add(MUENCHENERSTRASSE_Companion,0);
        frame.add(WIENERSTRASSE_Companion,0);
        frame.add(BERLINERSTRASSE_Companion,0);
        frame.add(HAMBURGERSTRASSE_Companion,0);

        frame.add(THEATERSTRASSE_Companion,0);
        frame.add(MUSEUMSTRASSE_Companion,0);
        frame.add(OPERNPLATZ_Companion,0);
        frame.add(KONZERTHAUSSTRASSE_Companion,0);

        frame.add(LESSINGSTRASSE_Companion,0);
        frame.add(SCHILLERSTRASSE_Companion,0);
        frame.add(GOETHESTRASSE_Companion,0);
        frame.add(RILKESTRASSE_Companion,0);

        frame.add(RATHAUSPLATZ_Companion,0);
        frame.add(HAUPSTRASSE_Companion,0);
        frame.add(BOERSENPLATZ_Companion,0);
        frame.add(BAHNHOFSTRASSE_Companion,0);

        frame.add(DOMPLATZ_Companion,0);
        frame.add(PARKSTRASSE_Companion,0);
        frame.add(SCHLOSSALLEE_Companion,0);

        frame.add(GASWERK_Companion,0);
        frame.add(ELEKTRIZITAETSWERK_Companion,0);
        frame.add(WASSERWERK_Companion,0);

        frame.add(SUEDBAHNHOF_Companion,0);
        frame.add(WESTBAHNHOF_Companion,0);
        frame.add(NORDBAHNHOF_Companion,0);
        frame.add(HAUPTBAHNHOF_Companion,0);



        //frame.repaint();

        JPanel panel = new JPanel();
        //frame.add(panel, 0);
        addFreeParkingMoney(420+20, 476+90+60, 90, frame);
        frame.repaint();
    }

    private void addFreeParkingMoney(int x, int y, int rotation, JFrame frame) {
        int noteWidth = 50;
        int noteHeight = 100;
        //TODO update game data
        int amount = Monopoly.GAME_DATA.getFreeParkingAmount();
        if(amount <= 0) return;
        int note1 = 0;
        int note5 = 0;
        int note10 = 0;
        int note20 = 0;
        int note50 = 0;
        int note100 = 0;
        int note500 = 0;
        int note1000 = 0;
        int noteAll = 0;
        int angle = 0;
        while (amount >= 1000) {
            amount -= 1000;
            note1000 += 1;
            noteAll += 1;
        }
        while (amount >= 500) {
            amount -= 500;
            note500 += 1;
            noteAll += 1;
        }
        while (amount >= 100) {
            amount -= 100;
            note100 += 1;
            noteAll += 1;
        }
        while (amount >= 50) {
            amount -= 50;
            note50 += 1;
            noteAll += 1;
        }
        while (amount >= 20) {
            amount -= 20;
            note20 += 1;
            noteAll += 1;
        }
        while (amount >= 10) {
            amount -= 10;
            note10 += 1;
            noteAll += 1;
        }
        while (amount >= 5) {
            amount -= 5;
            note5 += 1;
            noteAll += 1;
        }
        while (amount >= 1) {
            amount -= 1;
            note1 += 1;
            noteAll += 1;
        }
        if(noteAll == 0) return;
        x -= noteWidth / 2;
        y -= noteHeight / 2;
        angle -= ((noteAll - 1) * 5);
        angle += rotation;
        for(int i = 0; i < note1000; i++) {
            frame.add(JUtils.addImage("images/banknotes/1000_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note500; i++) {
            frame.add(JUtils.addImage("images/banknotes/500_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note100; i++) {
            frame.add(JUtils.addImage("images/banknotes/100_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note50; i++) {
            frame.add(JUtils.addImage("images/banknotes/50_vm.png", x, y, angle, 50,  25), 0);
            angle += 10;
        }
        for(int i = 0; i < note20; i++) {
            frame.add(JUtils.addImage("images/banknotes/20_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note10; i++) {
            frame.add(JUtils.addImage("images/banknotes/10_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note5; i++) {
            frame.add(JUtils.addImage("images/banknotes/5_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note1; i++) {
            frame.add(JUtils.addImage("images/banknotes/1_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
        for(Street street : Street.values()) street.setOwner("Player 1");
        for(TrainStation trainStation : TrainStation.values()) trainStation.setOwner("Player 1");
        for(Plant plant : Plant.values()) plant.setOwner("Player 1");
    }
}

