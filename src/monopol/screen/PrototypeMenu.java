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
import monopol.server.ServerPlayer;
import monopol.utils.JUtils;
import monopol.utils.Json;
import monopol.utils.KeyHandler;
import monopol.utils.JRotatedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public class PrototypeMenu {
    public final JFrame frame = new JFrame("Monopoly - PrototypeWindow");
    private final ArrayList<Client> clients = new ArrayList<>();
    private final ArrayList<Client> clientsTemp = new ArrayList<>();
    private boolean serverAcceptsNewClient;
    public Client client;
    private Client clientTemp;
    private String ip;
    private KeyHandler keyHandler = new KeyHandler();
    private IPurchasable selectedCard = Street.BADSTRASSE;
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
    }

    public void prepareMenu() {
        Monopoly.INSTANCE.setState(GameState.MAIN_MENU);
        if(gameThread.isAlive()) gameThread.interrupt();

        root.lobbyPane.reset();
        root.pingPane.reset();
        root.playerPane.reset();

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
                    if(!client.equals(oldClient)) frame.requestFocus();

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
                        frame.repaint();
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

    Thread gameThread = new Thread(() -> {/*do nothing*/});

    public void prepareGame() {
        Monopoly.INSTANCE.setState(GameState.RUNNING);
        gameThread.interrupt();

        root.lobbyPane.reset();
        //keep PlayerPane enabled
        //keep PingPane enabled

        //LEFT
        addStreetButton(frame, Street.TIERGARTENSTRASSE, 0, 150, Direction.RIGHT);
        addStreetButton(frame, Street.POSTSTRASSE, 0, 220, Direction.RIGHT);
        addPlantButton(frame, Plant.GASWERK, 0, 290, Direction.RIGHT);
        addEreignisfeld(frame, 0, 360, Direction.RIGHT);
        addStreetButton(frame, Street.ELISENSTRASSE, 0, 430, Direction.RIGHT);
        addStreetButton(frame, Street.CHAUSSESTRASSE, 0, 500, Direction.RIGHT);
        addTrainStationButton(frame, TrainStation.SUEDBAHNHOF, 0, 570, Direction.RIGHT);
        addSteuerfeld(frame, 0, 640, Direction.RIGHT);
        addStreetButton(frame, Street.STADIONSTRASSE, 0, 710, Direction.RIGHT);
        addStreetButton(frame, Street.TURMSTRASSE, 0, 780, Direction.RIGHT);
        addGemeinschaftsfeld(frame, 0, 850, Direction.RIGHT);
        addStreetButton(frame, Street.BADSTRASSE, 0, 920, Direction.RIGHT);
        //UP
        addSpecialField(frame, 90, 60, Direction.DOWN);
        addStreetButton(frame, Street.SEESTRASSE, 160, 60, Direction.DOWN);
        addStreetButton(frame, Street.HAFENSTRASSE, 230, 60, Direction.DOWN);
        addPlantButton(frame, Plant.ELEKTRIZITAETSWERK, 300, 60, Direction.DOWN);
        addStreetButton(frame, Street.NEUESTRASSE, 370, 60, Direction.DOWN);
        addStreetButton(frame, Street.MARKTPLATZ, 440, 60, Direction.DOWN);
        addTrainStationButton(frame, TrainStation.WESTBAHNHOF, 510, 60, Direction.DOWN);
        addStreetButton(frame, Street.MUENCHENERSTRASSE, 580, 60, Direction.DOWN);
        addGemeinschaftsfeld(frame, 650, 60, Direction.DOWN);
        addStreetButton(frame, Street.WIENERSTRASSE, 720, 60, Direction.DOWN);
        addStreetButton(frame, Street.BERLINERSTRASSE, 790, 60, Direction.DOWN);
        addStreetButton(frame, Street.HAMBURGERSTRASSE, 860, 60, Direction.DOWN);
        //RIGHT
        addStreetButton(frame, Street.THEATERSTRASSE, 930, 150, Direction.LEFT);
        addEreignisfeld(frame, 930, 220, Direction.LEFT);
        addStreetButton(frame, Street.MUSEUMSTRASSE, 930, 290, Direction.LEFT);
        addStreetButton(frame, Street.OPERNPLATZ, 930, 360, Direction.LEFT);
        addStreetButton(frame, Street.KONZERTHAUSSTRASSE, 930, 430, Direction.LEFT);
        addSpecialField(frame, 930, 500, Direction.LEFT);
        addTrainStationButton(frame, TrainStation.NORDBAHNHOF, 930, 570, Direction.LEFT);
        addStreetButton(frame, Street.LESSINGSTRASSE, 930, 640, Direction.LEFT);
        addStreetButton(frame, Street.SCHILLERSTRASSE, 930, 710, Direction.LEFT);
        addPlantButton(frame, Plant.WASSERWERK, 930, 780, Direction.LEFT);
        addStreetButton(frame, Street.GOETHESTRASSE, 930, 850, Direction.LEFT);
        addStreetButton(frame, Street.RILKESTRASSE, 930, 920, Direction.LEFT);
        //DOWN
        addStreetButton(frame, Street.SCHLOSSALLEE, 90, 990, Direction.UP);
        addGemeinschaftsfeld(frame, 160, 990, Direction.UP);
        addStreetButton(frame, Street.PARKSTRASSE, 230, 990, Direction.UP);
        addStreetButton(frame, Street.DOMPLATZ, 300, 990, Direction.UP);
        addSpecialField(frame, 370, 990, Direction.UP);
        addSteuerfeld(frame, 440, 990, Direction.UP);
        addTrainStationButton(frame, TrainStation.HAUPTBAHNHOF, 510, 990, Direction.UP);
        addStreetButton(frame, Street.BAHNHOFSTRASSE, 580, 990, Direction.UP);
        addEreignisfeld(frame, 650, 990, Direction.UP);
        addStreetButton(frame, Street.BOERSENPLATZ, 720, 990, Direction.UP);
        addStreetButton(frame, Street.HAUPSTRASSE, 790, 990, Direction.UP);
        addStreetButton(frame, Street.RATHAUSPLATZ, 860, 990, Direction.UP);
        //CORNERS
        frame.add(addImage("images/felder/gefaengnis.png", 0, 60));
        frame.add(addImage("images/felder/los.png", 0, 990));
        frame.add(addImage("images/felder/freiparken.png", 930, 60));
        frame.add(addImage("images/felder/ins_gefaengnis.png", 930, 990));
        //no repaint
        //frame.repaint();
        JButton Handeln = new JButton();

        frame.add(addButton(Handeln,null, JUtils.getX(1060), JUtils.getY(450+90*5), 400, 80, true,"images/Main_pictures/3d_button.png", actionEvent -> {
            try {
                ClientEvents.trade(this, null, TradeState.CHOOSE_PLAYER);
            } catch (RemoteException ignored) {}
        }));
        frame.add(addText("Handeln",JUtils.getX(1060), JUtils.getY(450+90*5+13),400,40,true),0);
        //frame.repaint();

        //TODO  \/  FABIANS PART  \/

        frame.add(addImage("images/Main_pictures/Background_Right.png", 1020, 60));
        frame.add(addImage("images/Main_pictures/hintergrund_links_mitte2.png", 90, 150));

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
        JLabel label_moneyCommpanion = addText("----",X+95,342,200,30,false);
        JLabel busfahrkarten_Commpanion = addText("-",X-57,307,20,30,false);
        JLabel gefaengnisfreikarte_Commpanion = addText("-",X+243,315,13,24,false);

        X = 1579;
        JLabel label_moneyPlayer = addText("----",X+95,342,200,30,false);
        JLabel busfahrkarten_player = addText("-",X-57,307,20,30,false);
        JLabel gefaengnisfreikarte_player = addText("-",X+243,315,13,24,false);

        int x = 1060 + 15;
        int y = 148;

        JLabel BADSTRASSE = addImage("images/kleine_karten/brown.png",x+15,y,20,40);
        JLabel TURMSTRASSE = addImage("images/kleine_karten/brown.png",x+45,y,20,40);
        JLabel STADIONSTRASSE = addImage("images/kleine_karten/brown.png",x+75,y,20,40);

        JLabel CHAUSSESTRASSE = addImage("images/kleine_karten/cyan.png",x+115,y,20,40);
        JLabel ELISENSTRASSE = addImage("images/kleine_karten/cyan.png",x+145,y,20,40);
        JLabel POSTSTRASSE = addImage("images/kleine_karten/cyan.png",x+175,y,20,40);
        JLabel TIERGARTENSTRASSE = addImage("images/kleine_karten/cyan.png",x+205,y,20,40);

        JLabel SEESTRASSE = addImage("images/kleine_karten/pink.png",x+245,y,20,40);
        JLabel HAFENSTRASSE = addImage("images/kleine_karten/pink.png",x+275,y,20,40);
        JLabel NEUESTRASSE = addImage("images/kleine_karten/pink.png",x+305,y,20,40);
        JLabel MARKTPLATZ = addImage("images/kleine_karten/pink.png",x+335,y,20,40);

        JLabel MUENCHENERSTRASSE = addImage("images/kleine_karten/orange.png",x,y+50,20,40);
        JLabel WIENERSTRASSE = addImage("images/kleine_karten/orange.png",x+30,y+50,20,40);
        JLabel BERLINERSTRASSE = addImage("images/kleine_karten/orange.png",x+60,y+50,20,40);
        JLabel HAMBURGERSTRASSE = addImage("images/kleine_karten/orange.png",x+90,y+50,20,40);

        JLabel THEATERSTRASSE = addImage("images/kleine_karten/red.png",x+130,y+50,20,40);
        JLabel MUSEUMSTRASSE = addImage("images/kleine_karten/red.png",x+160,y+50,20,40);
        JLabel OPERNPLATZ = addImage("images/kleine_karten/red.png",x+190,y+50,20,40);
        JLabel KONZERTHAUSSTRASSE = addImage("images/kleine_karten/red.png",x+220,y+50,20,40);

        JLabel LESSINGSTRASSE = addImage("images/kleine_karten/yellow.png",x+260,y+50,20,40);
        JLabel SCHILLERSTRASSE = addImage("images/kleine_karten/yellow.png",x+290,y+50,20,40);
        JLabel GOETHESTRASSE = addImage("images/kleine_karten/yellow.png",x+320,y+50,20,40);
        JLabel RILKESTRASSE = addImage("images/kleine_karten/yellow.png",x+350,y+50,20,40);

        JLabel RATHAUSPLATZ = addImage("images/kleine_karten/green.png",x+80,y+100,20,40);
        JLabel HAUPSTRASSE = addImage("images/kleine_karten/green.png",x+110,y+100,20,40);
        JLabel BOERSENPLATZ = addImage("images/kleine_karten/green.png",x+140,y+100,20,40);
        JLabel BAHNHOFSTRASSE = addImage("images/kleine_karten/green.png",x+170,y+100,20,40);

        JLabel DOMPLATZ = addImage("images/kleine_karten/blue.png",x+210,y+100,20,40);
        JLabel PARKSTRASSE = addImage("images/kleine_karten/blue.png",x+240,y+100,20,40);
        JLabel SCHLOSSALLEE = addImage("images/kleine_karten/blue.png",x+270,y+100,20,40);

        JLabel GASWERK = addImage("images/kleine_karten/gas.png",x+210,y+150,20,40);
        JLabel ELEKTRIZITAETSWERK = addImage("images/kleine_karten/elec.png",x+240,y+150,20,40);
        JLabel WASSERWERK = addImage("images/kleine_karten/water.png",x+270,y+150,20,40);

        JLabel SUEDBAHNHOF = addImage("images/kleine_karten/train.png",x+80,y+150,20,40);
        JLabel WESTBAHNHOF = addImage("images/kleine_karten/train.png",x+110,y+150,20,40);
        JLabel NORDBAHNHOF = addImage("images/kleine_karten/train.png",x+140,y+150,20,40);
        JLabel HAUPTBAHNHOF = addImage("images/kleine_karten/train.png",x+170,y+150,20,40);

        x = 1479 + 15;
        y = 148;

        JLabel BADSTRASSE_Companion = addImage("images/kleine_karten/brown.png",x+15,y,20,40);
        JLabel TURMSTRASSE_Companion = addImage("images/kleine_karten/brown.png",x+45,y,20,40);
        JLabel STADIONSTRASSE_Companion = addImage("images/kleine_karten/brown.png",x+75,y,20,40);

        JLabel CHAUSSESTRASSE_Companion = addImage("images/kleine_karten/cyan.png",x+115,y,20,40);
        JLabel ELISENSTRASSE_Companion = addImage("images/kleine_karten/cyan.png",x+145,y,20,40);
        JLabel POSTSTRASSE_Companion = addImage("images/kleine_karten/cyan.png",x+175,y,20,40);
        JLabel TIERGARTENSTRASSE_Companion = addImage("images/kleine_karten/cyan.png",x+205,y,20,40);

        JLabel SEESTRASSE_Companion = addImage("images/kleine_karten/pink.png",x+245,y,20,40);
        JLabel HAFENSTRASSE_Companion = addImage("images/kleine_karten/pink.png",x+275,y,20,40);
        JLabel NEUESTRASSE_Companion = addImage("images/kleine_karten/pink.png",x+305,y,20,40);
        JLabel MARKTPLATZ_Companion = addImage("images/kleine_karten/pink.png",x+335,y,20,40);

        JLabel MUENCHENERSTRASSE_Companion = addImage("images/kleine_karten/orange.png",x,y+50,20,40);
        JLabel WIENERSTRASSE_Companion = addImage("images/kleine_karten/orange.png",x+30,y+50,20,40);
        JLabel BERLINERSTRASSE_Companion = addImage("images/kleine_karten/orange.png",x+60,y+50,20,40);
        JLabel HAMBURGERSTRASSE_Companion = addImage("images/kleine_karten/orange.png",x+90,y+50,20,40);

        JLabel THEATERSTRASSE_Companion = addImage("images/kleine_karten/red.png",x+130,y+50,20,40);
        JLabel MUSEUMSTRASSE_Companion = addImage("images/kleine_karten/red.png",x+160,y+50,20,40);
        JLabel OPERNPLATZ_Companion = addImage("images/kleine_karten/red.png",x+190,y+50,20,40);
        JLabel KONZERTHAUSSTRASSE_Companion = addImage("images/kleine_karten/red.png",x+220,y+50,20,40);

        JLabel LESSINGSTRASSE_Companion = addImage("images/kleine_karten/yellow.png",x+260,y+50,20,40);
        JLabel SCHILLERSTRASSE_Companion = addImage("images/kleine_karten/yellow.png",x+290,y+50,20,40);
        JLabel GOETHESTRASSE_Companion = addImage("images/kleine_karten/yellow.png",x+320,y+50,20,40);
        JLabel RILKESTRASSE_Companion = addImage("images/kleine_karten/yellow.png",x+350,y+50,20,40);

        JLabel RATHAUSPLATZ_Companion = addImage("images/kleine_karten/green.png",x+80,y+100,20,40);
        JLabel HAUPSTRASSE_Companion = addImage("images/kleine_karten/green.png",x+110,y+100,20,40);
        JLabel BOERSENPLATZ_Companion = addImage("images/kleine_karten/green.png",x+140,y+100,20,40);
        JLabel BAHNHOFSTRASSE_Companion = addImage("images/kleine_karten/green.png",x+170,y+100,20,40);

        JLabel DOMPLATZ_Companion = addImage("images/kleine_karten/blue.png",x+210,y+100,20,40);
        JLabel PARKSTRASSE_Companion = addImage("images/kleine_karten/blue.png",x+240,y+100,20,40);
        JLabel SCHLOSSALLEE_Companion = addImage("images/kleine_karten/blue.png",x+270,y+100,20,40);

        JLabel GASWERK_Companion = addImage("images/kleine_karten/gas.png",x+210,y+150,20,40);
        JLabel ELEKTRIZITAETSWERK_Companion = addImage("images/kleine_karten/elec.png",x+240,y+150,20,40);
        JLabel WASSERWERK_Companion = addImage("images/kleine_karten/water.png",x+270,y+150,20,40);

        JLabel SUEDBAHNHOF_Companion = addImage("images/kleine_karten/train.png",x+80,y+150,20,40);
        JLabel WESTBAHNHOF_Companion = addImage("images/kleine_karten/train.png",x+110,y+150,20,40);
        JLabel NORDBAHNHOF_Companion = addImage("images/kleine_karten/train.png",x+140,y+150,20,40);
        JLabel HAUPTBAHNHOF_Companion = addImage("images/kleine_karten/train.png",x+170,y+150,20,40);


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

                    if(isInterrupted()) break;

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



        frame.add(addButton(button1,null,1060,90,400,60,true,"images/Main_pictures/Player_display.png", actionevent ->  {
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

        frame.add(addButton(button2,null,1479,90,400,60,true,"images/Main_pictures/Player_display.png",actionevent ->  {}),0);
        frame.add(addText(label_button1,"","Arial",1060,90 + 13,400,30,true),0);
        frame.add(addText(label_button2, client.player.getName(),"Arial",1479,90 + 13,400,30,true),0);
        frame.add(addImage("images/Main_pictures/Player_property.png",1060,135,400,247),0);
        frame.add(addImage("images/Main_pictures/Player_property.png",1479,135,400,247),0);

        X = 1160;
        frame.add(addImage("images/Main_pictures/money_underlay.png",X,340,200,33),0);
        frame.add(addText("Geld: ",X+25,342,152,30,false),0);
        frame.add(label_moneyCommpanion,0);
        frame.add(addImage("images/Main_pictures/busfahrkarte_rechts.png",X-100+15,250,70,90),0);
        frame.add(addText("Busfahrkarte",X-100+15,259,100,12,false),0);
        frame.add(busfahrkarten_Commpanion,0);
        frame.add(addImage("images/Main_pictures/gefängnisfrei_rechts.png",X+215,250,70,90),0);
        //frame.add(addText("",X+223,253,160,10,false),0);
        frame.add(addText("Knastfreikarte",X+218,255,160,10,false),0);
        frame.add(gefaengnisfreikarte_Commpanion,0);

        X = 1579;
        frame.add(addImage("images/Main_pictures/money_underlay.png",X,340,200,33),0);
        frame.add(addText("Geld: ",X+25,342,152,30,false),0);
        frame.add(label_moneyPlayer,0);
        frame.add(addImage("images/Main_pictures/busfahrkarte_rechts.png",X-100+15,250,70,90),0);
        frame.add(addText("Busfahrkarte",X-100+15,259,100,12,false),0);
        frame.add(busfahrkarten_player,0);
        frame.add(addImage("images/Main_pictures/gefängnisfrei_rechts.png",X+215,250,70,90),0);
        //frame.add(addText("",X+223,253,160,10,false),0);
        frame.add(addText("Knastfreikarte",X+218,255,160,10,false),0);
        frame.add(gefaengnisfreikarte_player,0);

        frame.add(addButton(Würfeln,null,1060,450,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //WÜRFELN
        }),0);
        frame.add(addText("Würfeln",1260-70, 463,160,40,false),0);

        frame.add(addButton(zugbeenden,null,1479,450,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //zugbeenden
        }),0);
        frame.add(addText("Zugbeenden",1679-105, 460,400,40,false),0);

        int x_actoins = 1060;
        int y_actions = 450;

        frame.add(addButton(straße_kaufen,null,x_actoins,y_actions+90,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //Straße kaufen
        }),0);
        frame.add(addText("Straße kaufen",x_actoins,y_actions+90+13,400,40,true),0);

        frame.add(addButton(haus_bauen,null,x_actoins,y_actions+90*2,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //haus bauen
        }),0);
        frame.add(addText("Haus bauen",x_actoins,y_actions+90*2+13,400,40,true),0);

        frame.add(addButton(haus_verkaufen,null,x_actoins,y_actions+90*3,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //haus verkaufen
        }),0);
        frame.add(addText("Haus verkaufen",x_actoins,y_actions+90*3+13,400,40,true),0);

        frame.add(addButton(hypotheken_aufnehmen,null,x_actoins,y_actions+90*4,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //hypotheken_aufnehmen
        }),0);
        frame.add(addText("hypotheken",x_actoins,y_actions+90*4+13,400,40,true),0);

        frame.add(addButton(einstellungen,null,x_actoins,y_actions+90*6,400,80,true,"images/Main_pictures/3d_button.png",actionevent ->  {
            //einstellungen
        }),0);
        frame.add(addText("Einstellungen",x_actoins,y_actions+90*6+13,400,40,true),0);


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

        if(client.tradeData.tradeState != TradeState.NULL) {
            try {
                ClientEvents.trade(this, client.tradeData.tradePlayer, client.tradeData.tradeState);
            } catch (RemoteException e) {
                client.close();
            }
        } else {
            frame.repaint();
        }
    }


    private void setClient(int i) {
        client = clients.get(i);
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
            frame.add(addImage("images/banknotes/1000_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note500; i++) {
            frame.add(addImage("images/banknotes/500_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note100; i++) {
            frame.add(addImage("images/banknotes/100_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note50; i++) {
            frame.add(addImage("images/banknotes/50_vm.png", x, y, angle, 50,  25), 0);
            angle += 10;
        }
        for(int i = 0; i < note20; i++) {
            frame.add(addImage("images/banknotes/20_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note10; i++) {
            frame.add(addImage("images/banknotes/10_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note5; i++) {
            frame.add(addImage("images/banknotes/5_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
        for(int i = 0; i < note1; i++) {
            frame.add(addImage("images/banknotes/1_vm.png", x, y, angle, 50, 25), 0);
            angle += 10;
        }
    }

    public JButton addButton(String display, int x, int y, int width, int height, boolean enabled, ActionListener actionEvent) {
        JButton button = new JButton(display);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        button.addActionListener(actionEvent);
        button.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        button.setEnabled(enabled);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        button.setIcon(new ImageIcon(new ImageIcon("images/DO_NOT_CHANGE/plain_button_2.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    public JButton addButton(JButton button, String display, int x, int y, int width, int height, boolean enabled, String icon, ActionListener actionEvent) {
        button.setText(display);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        button.addActionListener(actionEvent);
        button.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        button.setEnabled(enabled);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        button.setIcon(new ImageIcon(new ImageIcon(icon).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    public JButton addButton(JButton button, String display, int x, int y, int width, int height, boolean enabled, String icon,String disabled_icon, ActionListener actionEvent) {
        button = addButton(button,display,x,y,width,height,enabled,icon,actionEvent);
        button.setDisabledIcon(new ImageIcon(new ImageIcon(disabled_icon).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        return button;
    }

    private void addStreetButton(JFrame frame, Street street, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, JUtils.getX(1920), JUtils.getY(1080));
        switch (direction) {
            case LEFT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = street;
                });
                ImageIcon icon = new ImageIcon("images/felder/left_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+2, y+1), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x-5, y+2,11, -90, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = street;
                });
                ImageIcon icon = new ImageIcon("images/felder/up_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+2), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x+2, y-5,11, 0, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x+2, y+45, 13, 0, 66), 1);
            }
            case RIGHT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = street;
                });
                ImageIcon icon = new ImageIcon("images/felder/right_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+70, y+1), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x+28, y+2,11, 90, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = street;
                });
                ImageIcon icon = new ImageIcon("images/felder/down_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+70), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x+2, y+28,11, 180, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        frame.add(pane);
    }

    private void addTrainStationButton(JFrame frame, TrainStation station, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = station;
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/left_train.png", x+25, y+10), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x-25, y+2,11, -90, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = station;
                });
                ImageIcon icon = new ImageIcon("images/felder/high_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/up_train.png", x+10, y+25), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x+2, y-25,11, 0, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x+2, y+45, 13, 0, 66), 1);
            }
            case RIGHT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = station;
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/right_train.png", x+25, y+10), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x+48, y+2,11, 90, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = station;
                });
                ImageIcon icon = new ImageIcon("images/felder/high_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/down_train.png", x+10, y+27), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x+2, y+48,11, 180, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        frame.add(pane);
    }

    private void addPlantButton(JFrame frame, Plant plant, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = plant;
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/water_icon.png", x+15, y+10), 2);
                pane.add(addRotatedText(plant.name, Font.BOLD, x-25, y+2,11, -90, 66), 1);
                pane.add(addRotatedText(plant.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                throw new RuntimeException();
            }
            case RIGHT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = plant;
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/gas_icon.png", x+25, y+10), 2);
                pane.add(addRotatedText(plant.name, Font.BOLD, x+48, y+2,11, 90, 66), 1);
                pane.add(addRotatedText(plant.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = plant;
                });
                ImageIcon icon = new ImageIcon("images/felder/high_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(addImage("images/felder/power_icon.png", x+10, y+20), 2);
                pane.add(addRotatedText(plant.name, Font.BOLD, x+2, y+48,11, 180, 66), 1);
                pane.add(addRotatedText(plant.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        frame.add(pane);
    }

    private void addEreignisfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                label = addImage("images/felder/wide_background.png", x, y);
                pane.add(addImage("images/felder/right_ereignis.png", x+30, y+10), 2);
                pane.add(addRotatedText("Ereignisfeld", Font.BOLD, x-25, y+2,11, -90, 66), 1);
            }
            case UP -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/down_ereignis.png", x+10, y+25), 2);
                pane.add(addRotatedText("Ereignisfeld", Font.BOLD, x+2, y-25,11, 0, 66), 1);
            }
            case RIGHT -> {
                label = addImage("images/felder/wide_background.png", x, y);
                pane.add(addImage("images/felder/left_ereignis.png", x+10, y+10), 2);
                pane.add(addRotatedText("Ereignisfeld", Font.BOLD, x+48, y+2,11, 90, 66), 1);
            }
            case DOWN -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/up_ereignis.png", x+10, y+10), 2);
                pane.add(addRotatedText("Ereignisfeld", Font.BOLD, x+2, y+48,11, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 3);
        frame.add(pane);
    }

    private void addGemeinschaftsfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                label = addImage("images/felder/wide_background.png", x, y);
                pane.add(addImage("images/felder/left_gemeinschaft.png", x+30, y+10), 2);
                pane.add(addRotatedText("Gemeinschaftsfeld", Font.BOLD, x-25, y+2,11, -90, 66), 1);
            }
            case UP -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/up_gemeinschaft.png", x+10, y+25), 2);
                pane.add(addRotatedText("Gemeinschaftsfeld", Font.BOLD, x+2, y-25,11, 0, 66), 1);
            }
            case RIGHT -> {
                label = addImage("images/felder/wide_background.png", x, y);
                pane.add(addImage("images/felder/right_gemeinschaft.png", x+10, y+10), 2);
                pane.add(addRotatedText("Gemeinschaftsfeld", Font.BOLD, x+48, y+2,11, 90, 66), 1);
            }
            case DOWN -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/down_gemeinschaft.png", x+10, y+10), 2);
                pane.add(addRotatedText("Gemeinschaftsfeld", Font.BOLD, x+2, y+48,11, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 3);
        frame.add(pane);
    }

    private void addSteuerfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case UP -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/down_steuer.png", x+10, y+15), 3);
                pane.add(addRotatedText("Zusatzsteuer", Font.BOLD, x+2, y-25,11, 0, 66), 2);
                pane.add(addRotatedText("Zahle 75€", Font.BOLD, x+2, y+45, 11, 0, 66), 1);
            }
            case RIGHT -> {
                label = addImage("images/felder/wide_background.png", x, y);
                pane.add(addImage("images/felder/left_steuer.png", x+25, y+10), 3);
                pane.add(addRotatedText("Einkommenssteuer", Font.BOLD, x+48, y+2,11, 90, 66), 2);
                pane.add(addRotatedText("Zahle 10%", Font.BOLD, x-15, y+2, 11, 90, 66), 1);
                pane.add(addRotatedText("oder 200€", Font.BOLD, x-25, y+2, 11, 90, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 4);
        frame.add(pane);
    }

    private void addSpecialField(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                label = addImage("images/felder/wide_background.png", x, y);
                pane.add(addImage("images/felder/bus.png", x+30, y+10), 2);
                pane.add(addRotatedText("Busfahrkarte", Font.BOLD, x-25, y+2,11, -90, 66), 1);
            }
            case UP -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/geschenk.png", x+10, y+25), 2);
                pane.add(addRotatedText("Geschenk", Font.BOLD, x+2, y-25,11, 0, 66), 1);
            }
            case DOWN -> {
                label = addImage("images/felder/high_background.png", x, y);
                pane.add(addImage("images/felder/auktion.png", x+10, y+10), 2);
                pane.add(addRotatedText("Auktion", Font.BOLD, x+2, y+48,11, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 3);
        frame.add(pane);
    }

    public JButton addButton(String display, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        JButton button = addButton(display, x, y, width, height, enabled, actionEvent);
        button.setSelected(selected);
        return button;
    }

    public JButton addButton(JButton button, String display, String icon, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        button = addButton(button, display, x, y, width, height, enabled, icon, actionEvent);
        button.setSelected(selected);
        return button;
    }

    public JLabel addText(String display, int x, int y, int width, int height, boolean centered) {
        JLabel label;
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        if(centered) label = new JLabel(display, SwingConstants.CENTER); else label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, (int) ( height*1.2));
        return label;
    }

    public JLabel addText(JLabel label, String display,String font, int x, int y, int width, int height, boolean centered) {
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        if(centered) label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText(display);
        label.setFont(new Font(font, Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public JLabel addImage(String src, int x, int y) {
        ImageIcon icon = new ImageIcon(src);
        icon = new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT));
        JLabel label = new JLabel(icon);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), icon.getIconWidth(), icon.getIconHeight());
        return label;
    }

    public JLabel addImage(String src, int x, int y, int rotation, int rotX, int rotY) {
        ImageIcon icon = new ImageIcon(src);
        return new JRotatedLabel(icon, rotation, JUtils.getX(x), JUtils.getY(y), 0, rotX, rotY);
    }

    public JLabel addImage(String src, int x, int y, int width, int height) {
        JLabel label = addImage(src, x, y);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        label.setIcon(new ImageIcon(((ImageIcon) label.getIcon()).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT)));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public JLabel addText(String display, int x, int y, int width, int height) {
        JLabel label = new JLabel(display);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        label.setFont(new Font("Arial", Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public JLabel addRotatedText(String display, int font, int x, int y, int size, double angle, int maxLength) {
        x = JUtils.getX(x);
        y = JUtils.getY(y);
        if(angle == 0 || angle == 180) {
            maxLength = JUtils.getX(maxLength);
            size = JUtils.getX(size);
        } else if(angle == 90 || angle == -90) {
            maxLength = JUtils.getY(maxLength);
            size = JUtils.getY(size);
        }
        return new JRotatedLabel(display, size, font, angle, x, y, maxLength);
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
        for(Street street : Street.values()) street.setOwner("Player 1");
        for(TrainStation trainStation : TrainStation.values()) trainStation.setOwner("Player 1");
        for(Plant plant : Plant.values()) plant.setOwner("Player 1");
    }
}

