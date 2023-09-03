package monopol.screen;

import monopol.client.Client;
import monopol.client.ClientEvents;
import monopol.client.TradeState;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.constants.IPurchasable;
import monopol.constants.Street;
import monopol.constants.TrainStation;
import monopol.constants.Plant;
import monopol.server.DisconnectReason;
import monopol.server.ServerPlayer;
import monopol.server.ServerSettings;
import monopol.utils.JUtils;
import monopol.utils.KeyHandler;
import monopol.utils.JRotatedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

public class PrototypeMenu {
    public final JFrame frame = new JFrame("Monopoly - PrototypeWindow");
    private final ArrayList<Client> clients = new ArrayList<>();
    private final ArrayList<Client> clientsTemp = new ArrayList<>();
    private boolean serverAcceptsNewClient;
    public Client client;
    private Client clientTemp;
    private ArrayList<ServerPlayer> displayedServerPlayers = new ArrayList<>();
    private String ip;
    private KeyHandler keyHandler = new KeyHandler();
    private IPurchasable selectedCard = Street.BADSTRASSE;

    public PrototypeMenu() {
        if((int) JUtils.SCREEN_WIDTH / (int) JUtils.SCREEN_HEIGHT != 16 / 9) System.err.println("[WARNING]: Your screen resolution is not 16/9. This may causes wrong screen drawing. Please change your screen device!");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setSize(new Dimension((int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT));
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.addKeyListener(keyHandler);
    }

    public void prepareMenu() {
        Monopoly.INSTANCE.setState(GameState.MAIN_MENU);
        displayedServerPlayers = new ArrayList<>();
        frame.getContentPane().removeAll();
        frame.repaint();

        frame.add(addButton("invisible", 0, 0, 0, 0, true, actionEvent -> {}));
        frame.add(addButton("Host game", 50, 50, 200, 50, true, actionEvent -> {
            boolean canKick = JOptionPane.showConfirmDialog(null, "Allow all players to kick each other?", "Host game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
            boolean canSet = JOptionPane.showConfirmDialog(null, "Allow all players to access settings?", "Host game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
            try {
                ip = "localhost";
                Monopoly.INSTANCE.openServer(new ServerSettings(canKick, canSet));
                client = new Client(ip, 25565, true);
                clients.add(client);
                prepareLobby();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to start server. Make sure there are no other running server on your PC!", "Failed to start server", JOptionPane.WARNING_MESSAGE);
            }
        }));
        frame.add(addButton("Join game", 50, 150, 200, 50, true, actionEvent -> {
            do {
                ip = JOptionPane.showInputDialog(null, "Please enter the IP-Address:", "Join game", JOptionPane.QUESTION_MESSAGE);
                if(ip == null) return;
            } while(ip.isEmpty());
            try {
                client = new Client(ip, 25565, (ip.equals("localhost")));
                clients.add(client);
                prepareLobby();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Server not found. Make sure the IP-Address is correct!", "Server not found", JOptionPane.WARNING_MESSAGE);
            }
        }));
        frame.add(addButton("Close", 50, 250, 200, 50, true, actionEvent -> {
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        }));
        frame.add(addImage("images/Monopoly_client1.png", 0, 0, 1920, 1080));
        frame.repaint();
    }

    public void prepareLobby() {
        Monopoly.INSTANCE.setState(GameState.LOBBY);
        frame.getContentPane().removeAll();
        frame.repaint();

        Thread lobbyThread = new Thread() {
            @Override
            public void run() {

                frame.add(addText("Connecting to server...", (1920 / 2) - 250, 1080 / 2, 500, 25, true));
                frame.repaint();

                while(!isInterrupted() && client.player.getName() == null) {
                    if(client.closed()) {
                        interrupt();
                        prepareMenu();
                        return;
                    }
                }

                while(!isInterrupted()) {

                    //if(keyHandler.isKeyPressed(KeyEvent.VK_W)) System.out.println("HI");

                    if(client.closed()) {
                        clients.remove(client);
                        if(!clients.isEmpty()) {//MonopolyScreen.png
                            client = clients.get(0);
                        } else {
                            interrupt();
                            prepareMenu();
                            return;
                        }
                    }

                    clients.removeIf(Client::closed);

                    try {
                        boolean shouldUpdate = true;
                        for(ServerPlayer serverPlayer1 : displayedServerPlayers) {
                            String name = serverPlayer1.getName();
                            boolean okay = false;
                            for(ServerPlayer serverPlayer2 : client.serverMethod().getServerPlayers()) {
                                if (serverPlayer2.getName().equals(name)) {
                                    okay = true;
                                    break;
                                }
                            }
                            if(!okay) {
                                shouldUpdate = true;
                                break;
                            } else shouldUpdate = false;
                        }
                        if(!clientsTemp.containsAll(clients) || !clients.containsAll(clientsTemp)) shouldUpdate = true;
                        clientsTemp.removeAll(clientsTemp);
                        clientsTemp.addAll(clients);
                        if(serverAcceptsNewClient != client.serverMethod().acceptsNewClient()) shouldUpdate = true;
                        serverAcceptsNewClient = client.serverMethod().acceptsNewClient();
                        if(clientTemp != client) shouldUpdate = true;
                        clientTemp = client;

                        if(shouldUpdate) {
                            boolean ableToKick;
                            boolean ableToAccessSettings;
                            try {
                                if (client.player.isHost) ableToKick = true;
                                else ableToKick = client.serverMethod().getServerSettings().allPlayersCanKick;
                                if (client.player.isHost) ableToAccessSettings = true;
                                else ableToAccessSettings = client.serverMethod().getServerSettings().allPlayersCanAccessSettings;
                            } catch (RemoteException e) {
                                ableToKick = client.player.isHost;
                                ableToAccessSettings = client.player.isHost;
                            }
                            frame.getContentPane().removeAll();
                            int y = 150;
                            for (ServerPlayer serverPlayer : client.serverMethod().getServerPlayers()) {
                                frame.add(addText(serverPlayer.getName(), 50, y, 500, 25));
                                if(!serverPlayer.getName().equals(client.player.getName())) {
                                    frame.add(addButton("Kick", 600, y, 150, 25, ableToKick, actionEvent -> {
                                        try {
                                            client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.KICKED);
                                        } catch (Exception ignored) {}
                                    }));
                                } else {
                                    frame.add(addButton("change name", 600, y, 150, 25, true, actionEvent -> {
                                        try {
                                            String name = JOptionPane.showInputDialog(null, "New name:", "Change name", JOptionPane.QUESTION_MESSAGE);
                                            if(client.serverMethod().changeName(client.player.getName(), name)) client.player.setName(name); else JOptionPane.showMessageDialog(null, "This name is either already in use or too long!", "Change name", JOptionPane.WARNING_MESSAGE);
                                        } catch (Exception ignored) {
                                        }
                                    }));
                                }
                                y += 50;
                            }
                            frame.add(addButton("add player", 50, 1080 - 100, 200, 50, true, actionEvent -> {
                                try {
                                    if(client.serverMethod().acceptsNewClient()) {
                                        client = new Client(ip, 25565, false);
                                        clients.add(clients.size(), client);
                                    }
                                } catch (Exception ignored) {}
                            }));
                            frame.add(addButton("add bot", 50, 1080 - 200, 200, 50, client.player.isHost, actionEvent -> {
                                JOptionPane.showMessageDialog(null, "This option is not possible yet", "Add bot", JOptionPane.WARNING_MESSAGE);
                            }));
                            frame.add(addButton("leave", 1920 - 250, 1080 - 100, 200, 50, true, actionEvent -> {
                                try {
                                    client.serverMethod().kick(client.player.getName(), DisconnectReason.CLIENT_CLOSED);
                                } catch (Exception ignored) {}
                            }));
                            frame.add(addButton("start", 1920 - 250, 1080 - 200, 200, 50, client.player.isHost, actionEvent -> {
                                try {
                                    client.serverMethod().start();
                                } catch (Exception ignored) {}
                            }));
                            frame.add(addText("IP-Address: " + client.serverMethod().getIp(), (1920/2)-250, 1080-70, 500, 30, true));
                            displayedServerPlayers = client.serverMethod().getServerPlayers();
                            for(int i = 0; i < clients.size(); i++) {
                                frame.add(addPlayerButton(i));
                            }
                            frame.repaint();
                        }
                    } catch (RemoteException e) {
                        System.err.println(e.getMessage());
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
                    } catch (InterruptedException ignored) {}
                }
            }
        };
        lobbyThread.start();
    }

    public void prepareGame() {
        Monopoly.INSTANCE.setState(GameState.RUNNING);
        frame.getContentPane().removeAll();
        frame.repaint();

        for(int i = 0; i < clients.size(); i++) {
            frame.add(addPlayerButton(i));
        }

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
        //REPAINT
        frame.repaint();

        frame.add(addButton("Handeln", JUtils.getX(300), JUtils.getY(500), 200, 50, true, actionEvent -> {
            try {
                ClientEvents.trade(this, null, TradeState.CHOOSE_PLAYER);
            } catch (RemoteException ignored) {}
        }));
        frame.repaint();

        if(client.tradeState != TradeState.NULL) {
            try {
                ClientEvents.trade(this, client.tradePlayer, client.tradeState);
            } catch (RemoteException ignored) {}
        }

        //TODO  \/  FABIANS PART  \/

        frame.add(addImage("images/Main_pictures/Background_Right.png", 1020, 60));

        int[] currentPlayer = new int[1];
        currentPlayer[0] = 0;
        int[] maxplayers;

        try {
            maxplayers = new int[client.serverMethod().getServerPlayers().size()];
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        JButton button1 = new JButton();
        JButton button2 = new JButton();
        JLabel label_button1 = new JLabel();
        JLabel label_button2 = new JLabel();

        JLabel BADSTRASSE = addImage("",10,10,20,40);
        JLabel TURMSTRASSE = addImage("",10,10,20,40);
        JLabel STADIONSTRASSE = addImage("",10,10,20,40);

        JLabel CHAUSSESTRASSE = addImage("",10,10,20,40);
        JLabel ELISENSTRASSE = addImage("",10,10,20,40);
        JLabel POSTSTRASSE = addImage("",10,10,20,40);
        JLabel TIERGARTENSTRASSE = addImage("",10,10,20,40);

        JLabel SEESTRASSE = addImage("",10,10,20,40);
        JLabel HAFENSTRASSE = addImage("",10,10,20,40);
        JLabel NEUESTRASSE = addImage("",10,10,20,40);
        JLabel MARKTPLATZ = addImage("",10,10,20,40);

        JLabel MUENCHENERSTRASSE = addImage("",10,10,20,40);
        JLabel WIENERSTRASSE = addImage("",10,10,20,40);
        JLabel BERLINERSTRASSE = addImage("",10,10,20,40);
        JLabel HAMBURGERSTRASSE = addImage("",10,10,20,40);

        JLabel THEATERSTRASSE = addImage("",10,10,20,40);
        JLabel MUSEUMSTRASSE = addImage("",10,10,20,40);
        JLabel OPERNPLATZ = addImage("",10,10,20,40);
        JLabel KONZERTHAUSSTRASSE = addImage("",10,10,20,40);

        JLabel LESSINGSTRASSE = addImage("",10,10,20,40);
        JLabel SCHILLERSTRASSE = addImage("",10,10,20,40);
        JLabel GOETHESTRASSE = addImage("",10,10,20,40);
        JLabel RILKESTRASSE = addImage("",10,10,20,40);

        JLabel RATHAUSPLATZ = addImage("",10,10,20,40);
        JLabel HAUPSTRASSE = addImage("",10,10,20,40);
        JLabel BOERSENPLATZ = addImage("",10,10,20,40);
        JLabel BAHNHOFSTRASSE = addImage("",10,10,20,40);

        JLabel DOMPLATZ = addImage("",10,10,20,40);
        JLabel PARKSTRASSE = addImage("",10,10,20,40);
        JLabel SCHLOSSALLEE = addImage("",10,10,20,40);


        Thread lobbyThread = new Thread(){
            @Override
            public void run(){
                ServerPlayer serverPlayer;
                Street street = Street.values()[0];
                while(!interrupted()){
                    try {
                        serverPlayer = client.serverMethod().getServerPlayers().get(currentPlayer[0]);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    label_button1.setText(serverPlayer.getName());
                    /*
                    BADSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    TURMSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    STADIONSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    CHAUSSESTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    ELISENSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    POSTSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    TIERGARTENSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    SEESTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    HAFENSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    NEUESTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    MARKTPLATZ.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    MUENCHENERSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    WIENERSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    BERLINERSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    HAMBURGERSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    THEATERSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    MUSEUMSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    OPERNPLATZ.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    KONZERTHAUSSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    LESSINGSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    SCHILLERSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    GOETHESTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    RILKESTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    RATHAUSPLATZ.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    HAUPSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    BOERSENPLATZ.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    BAHNHOFSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));

                    DOMPLATZ.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    PARKSTRASSE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                    SCHLOSSALLEE.setIcon(new ImageIcon(Street.BADSTRASSE.getOwner().equals(client.player.getName()) ? "w" : ""));
                     */

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };
        lobbyThread.start();

        frame.add(addButton(button1,null,1060,90,400,60,true,"images/Main_pictures/Player_display.png",actionevent ->  {
            if(currentPlayer[0] < maxplayers.length - 1){
                currentPlayer[0] = currentPlayer[0] + 1;
            }else{
                currentPlayer[0] = 0;
            }
        }),0);
        frame.add(addButton(button2,null,1479,90,400,60,true,"images/Main_pictures/Player_display.png",actionevent ->  {}),0);

        frame.add(addText(label_button1,"","Arial",button1.getX(),button1.getY() + 13,400,30,true),0);
        frame.add(addText(label_button2,client.player.getName(),"Arial",button2.getX(),button2.getY() + 13,400,30,true),0);
        frame.add(addImage("images/Main_pictures/Player_property.png",button1.getX(),button1.getY()+56,400,217),0);
        frame.add(addImage("images/Main_pictures/Player_property.png",button2.getX(),button2.getY()+56,400,217),0);
        frame.repaint();
    }

    private void setClient(int i) {
        client = clients.get(i);
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
        //button.setDisabledIcon(new ImageIcon(new ImageIcon("images/DO_NOT_CHANGE/plain_button_2.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        //button.setPressedIcon(new ImageIcon(new ImageIcon("images/DO_NOT_CHANGE/plain_button_0.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        //button.setRolloverIcon(new ImageIcon(new ImageIcon("images/DO_NOT_CHANGE/plain_button_1.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        //button.setSelectedIcon(new ImageIcon(new ImageIcon("images/DO_NOT_CHANGE/plain_button_0.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
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

    private JButton addPlayerButton(int i) {
        int step = 1920 / clients.size();
        int[] value = {i};
        JButton button = addButton(clients.get(i).player.getName(), i * step, 0, step, 60, true, (client == clients.get(value[0])), actionEvent -> {
            setClient(value[0]);
            switch (Monopoly.INSTANCE.getState()) {
                case RUNNING -> prepareGame();
            }
        });
        step = Math.max(step, 1);
        button.setIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setDisabledIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setPressedIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setRolloverIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setSelectedIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
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
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
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
