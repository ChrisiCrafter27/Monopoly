package monopol.screen;

import monopol.client.Client;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.rules.ISelectedCard;
import monopol.rules.Street;
import monopol.rules.TrainStation;
import monopol.rules.Works;
import monopol.server.DisconnectReason;
import monopol.server.ServerPlayer;
import monopol.server.ServerSettings;
import monopol.utils.KeyHandler;
import monopol.utils.RotateJLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PrototypeMenu {
    private final JFrame frame = new JFrame("Monopoly - PrototypeWindow");
    private final ArrayList<Client> clients = new ArrayList<>();
    private final ArrayList<Client> clientsTemp = new ArrayList<>();
    private boolean serverAcceptsNewClient;
    private Client client;
    private Client clientTemp;
    private ArrayList<ServerPlayer> displayedServerPlayers = new ArrayList<>();
    private String ip;
    private KeyHandler keyHandler = new KeyHandler();
    private ISelectedCard selectedCard = Street.BADSTRASSE;

    public PrototypeMenu() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setSize(new Dimension(1920, 1080));
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
        frame.repaint();
    }

    public void prepareLobby() {
        Monopoly.INSTANCE.setState(GameState.LOBBY);
        frame.getContentPane().removeAll();
        frame.repaint();

        Thread lobbyThread = new Thread() {
            @Override
            public void run() {

                frame.add(addText("Connecting to server...", (frame.getWidth() / 2) - 250, frame.getHeight() / 2, 500, 25, true));
                frame.repaint();

                while(!isInterrupted() && client.name == null) {
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
                        if(!clients.isEmpty()) {
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
                                if (client.isHost) ableToKick = true;
                                else ableToKick = client.serverMethod().getServerSettings().allPlayersCanKick;
                                if (client.isHost) ableToAccessSettings = true;
                                else ableToAccessSettings = client.serverMethod().getServerSettings().allPlayersCanAccessSettings;
                            } catch (RemoteException e) {
                                ableToKick = client.isHost;
                                ableToAccessSettings = client.isHost;
                            }
                            frame.getContentPane().removeAll();
                            int y = 150;
                            for (ServerPlayer serverPlayer : client.serverMethod().getServerPlayers()) {
                                frame.add(addText(serverPlayer.getName(), 50, y, 500, 25));
                                if(!serverPlayer.getName().equals(client.name)) {
                                    frame.add(addButton("Kick", 600, y, 150, 25, ableToKick, actionEvent -> {
                                        try {
                                            client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.KICKED);
                                        } catch (Exception ignored) {}
                                    }));
                                } else {
                                    frame.add(addButton("change name", 600, y, 150, 25, true, actionEvent -> {
                                        try {
                                            String name = JOptionPane.showInputDialog(null, "New name:", "Change name", JOptionPane.QUESTION_MESSAGE);
                                            if(client.serverMethod().changeName(client.name, name)) client.name = name; else JOptionPane.showMessageDialog(null, "This name is either already in use or too long!", "Change name", JOptionPane.WARNING_MESSAGE);
                                        } catch (Exception ignored) {
                                        }
                                    }));
                                }
                                y += 50;
                            }
                            frame.add(addButton("add player", 50, frame.getHeight() - 100, 200, 50, true, actionEvent -> {
                                try {
                                    if(client.serverMethod().acceptsNewClient()) {
                                        client = new Client(ip, 25565, false);
                                        clients.add(clients.size(), client);
                                    }
                                } catch (Exception ignored) {}
                            }));
                            frame.add(addButton("add bot", 50, frame.getHeight() - 200, 200, 50, client.isHost, actionEvent -> {
                                JOptionPane.showMessageDialog(null, "This option is not possible yet", "Add bot", JOptionPane.WARNING_MESSAGE);
                            }));
                            frame.add(addButton("leave", frame.getWidth() - 250, frame.getHeight() - 100, 200, 50, true, actionEvent -> {
                                try {
                                    client.serverMethod().kick(client.name, DisconnectReason.CLIENT_CLOSED);
                                } catch (Exception ignored) {}
                            }));
                            frame.add(addButton("start", frame.getWidth() - 250, frame.getHeight() - 200, 200, 50, client.isHost, actionEvent -> {
                                try {
                                    client.serverMethod().start();
                                } catch (Exception ignored) {}
                            }));
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
                        System.out.println("The game should now start!");
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
        addWorksButton(frame, Works.GASWERK, 0, 290, Direction.RIGHT);
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
        addSpecialfeld(frame, 90, 60, Direction.DOWN);
        addStreetButton(frame, Street.SEESTRASSE, 160, 60, Direction.DOWN);
        addStreetButton(frame, Street.HAFENSTRASSE, 230, 60, Direction.DOWN);
        addWorksButton(frame, Works.ELEKTRIZITAETSWERK, 300, 60, Direction.DOWN);
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
        addSpecialfeld(frame, 930, 500, Direction.LEFT);
        addTrainStationButton(frame, TrainStation.NORDBAHNHOF, 930, 570, Direction.LEFT);
        addStreetButton(frame, Street.LESSINGSTRASSE, 930, 640, Direction.LEFT);
        addStreetButton(frame, Street.SCHILLERSTRASSE, 930, 710, Direction.LEFT);
        addWorksButton(frame, Works.WASSERWERK, 930, 780, Direction.LEFT);
        addStreetButton(frame, Street.GOETHESTRASSE, 930, 850, Direction.LEFT);
        addStreetButton(frame, Street.RILKESTRASSE, 930, 920, Direction.LEFT);
        //DOWN
        addStreetButton(frame, Street.SCHLOSSALLEE, 90, 990, Direction.UP);
        addGemeinschaftsfeld(frame, 160, 990, Direction.UP);
        addStreetButton(frame, Street.PARKSTRASSE, 230, 990, Direction.UP);
        addStreetButton(frame, Street.DOMPLATZ, 300, 990, Direction.UP);
        addEreignisfeld(frame, 370, 990, Direction.UP);
        addSteuerfeld(frame, 440, 990, Direction.UP);
        addTrainStationButton(frame, TrainStation.HAUPTBAHNHOF, 510, 990, Direction.UP);
        addStreetButton(frame, Street.BAHNHOFSTRASSE, 580, 990, Direction.UP);
        addSpecialfeld(frame, 650, 990, Direction.UP);
        addStreetButton(frame, Street.BOERSENPLATZ, 720, 990, Direction.UP);
        addStreetButton(frame, Street.HAUPSTRASSE, 790, 990, Direction.UP);
        addStreetButton(frame, Street.RATHAUSPLATZ, 860, 990, Direction.UP);
        //CORNERS
        frame.add(addImage("images/felder/gefaengnis.png", 0, 60));
        frame.add(addImage("images/felder/los.png", 0, 990));
        frame.add(addImage("images/felder/freiparken.png", 930, 60));
        frame.add(addImage("images/felder/ins_gefaengnis.png", 930, 990));
        frame.repaint();
    }

    private void setClient(int i) {
        client = clients.get(i);
    }

    private static JButton addButton(String display, int x, int y, int width, int height, boolean enabled, ActionListener actionEvent) {
        JButton button = new JButton(display);
        button.addActionListener(actionEvent);
        button.setBounds(x, y, width, height);
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

    private JButton addPlayerButton(int i) {
        int step = frame.getWidth() / clients.size();
        int[] value = {i};
        JButton button = addButton(clients.get(i).name, i * step, 0, step, 60, true, (client == clients.get(value[0])), actionEvent -> {
            setClient(value[0]);
        });
        button.setIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setDisabledIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setPressedIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setRolloverIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        button.setSelectedIcon(new ImageIcon(new ImageIcon("images/playerselect/playerselect_0_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
        return button;
    }

    private JLayeredPane addStreetButton(JFrame frame, Street street, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        switch (direction) {
            case LEFT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = street;
                });
                button.setIcon(new ImageIcon("images/felder/left_background.png"));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+2, y+1), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x-5, y+2,11, -90, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = street;
                });
                button.setIcon(new ImageIcon("images/felder/up_background.png"));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+2), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x+2, y-5,11, 0, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x+2, y+45, 13, 0, 66), 1);
            }
            case RIGHT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = street;
                });
                button.setIcon(new ImageIcon("images/felder/right_background.png"));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+70, y+1), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x+28, y+2,11, 90, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = street;
                });
                button.setIcon(new ImageIcon("images/felder/down_background.png"));
                pane.add(addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+70), 2);
                pane.add(addRotatedText(street.name, Font.BOLD, x+2, y+28,11, 180, 66), 1);
                pane.add(addRotatedText(street.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        frame.add(pane);
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private JLayeredPane addTrainStationButton(JFrame frame, TrainStation station, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        switch (direction) {
            case LEFT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = station;
                });
                button.setIcon(new ImageIcon("images/felder/wide_background.png"));
                pane.add(addImage("images/felder/left_train.png", x+25, y+10), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x-25, y+2,11, -90, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = station;
                });
                button.setIcon(new ImageIcon("images/felder/high_background.png"));
                pane.add(addImage("images/felder/up_train.png", x+10, y+25), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x+2, y-25,11, 0, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x+2, y+45, 13, 0, 66), 1);
            }
            case RIGHT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = station;
                });
                button.setIcon(new ImageIcon("images/felder/wide_background.png"));
                pane.add(addImage("images/felder/right_train.png", x+25, y+10), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x+48, y+2,11, 90, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = station;
                });
                button.setIcon(new ImageIcon("images/felder/high_background.png"));
                pane.add(addImage("images/felder/down_train.png", x+10, y+27), 2);
                pane.add(addRotatedText(station.name, Font.BOLD, x+2, y+48,11, 180, 66), 1);
                pane.add(addRotatedText(station.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        frame.add(pane);
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private JLayeredPane addWorksButton(JFrame frame, Works works, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        switch (direction) {
            case LEFT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = works;
                });
                button.setIcon(new ImageIcon("images/felder/wide_background.png"));
                pane.add(addImage("images/felder/water_icon.png", x+15, y+10), 2);
                pane.add(addRotatedText(works.name, Font.BOLD, x-25, y+2,11, -90, 66), 1);
                pane.add(addRotatedText(works.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                throw new RuntimeException();
            }
            case RIGHT -> {
                button = addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectedCard = works;
                });
                button.setIcon(new ImageIcon("images/felder/wide_background.png"));
                pane.add(addImage("images/felder/gas_icon.png", x+25, y+10), 2);
                pane.add(addRotatedText(works.name, Font.BOLD, x+48, y+2,11, 90, 66), 1);
                pane.add(addRotatedText(works.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectedCard = works;
                });
                button.setIcon(new ImageIcon("images/felder/high_background.png"));
                pane.add(addImage("images/felder/power_icon.png", x+10, y+20), 2);
                pane.add(addRotatedText(works.name, Font.BOLD, x+2, y+48,11, 180, 66), 1);
                pane.add(addRotatedText(works.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        frame.add(pane);
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private JLayeredPane addEreignisfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
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
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private JLayeredPane addGemeinschaftsfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
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
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private JLayeredPane addSteuerfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
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
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private JLayeredPane addSpecialfeld(JFrame frame, int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
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
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private static JButton addButton(String display, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        JButton button = addButton(display, x, y, width, height, enabled, actionEvent);
        button.setSelected(selected);
        return button;
    }

    private static JLabel addText(String display, int x, int y, int width, int size, boolean centered) {
        JLabel label;
        if(centered) label = new JLabel(display, SwingConstants.CENTER); else label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        return label;
    }

    private static JLabel addImage(String src, int x, int y) {
        ImageIcon icon = new ImageIcon(src);
        JLabel label = new JLabel(icon);
        label.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        return label;
    }

    private static JLabel addImage(String src, int x, int y, int width, int height) {
        JLabel label = addImage(src, x, y);
        label.setIcon(new ImageIcon(((ImageIcon) label.getIcon()).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        label.setBounds(x, y, width, height);
        return label;
    }

    private static JLabel addText(String display, int x, int y, int width, int size) {
        JLabel label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        return label;
    }

    private static JLabel addRotatedText(String display, int font, int x, int y, int size, double angle, int maxLength) {
        RotateJLabel label = new RotateJLabel(display, size, font, angle, x, y, maxLength);
        //label.setHorizontalAlignment(SwingConstants.CENTER);
        //label.setVerticalAlignment(SwingConstants.CENTER);
        //label.setHorizontalTextPosition(SwingConstants.CENTER);
        //label.setVerticalTextPosition(SwingConstants.CENTER);
        return label;
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }
}
