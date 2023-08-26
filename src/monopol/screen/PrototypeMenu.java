package monopol.screen;

import monopol.client.Client;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.rules.Street;
import monopol.server.DisconnectReason;
import monopol.server.ServerPlayer;
import monopol.server.ServerSettings;
import monopol.utils.KeyHandler;

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
    private Street selectedStreet = Street.BADSTRASSE;

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

        addButton(frame, "invisible", 0, 0, 0, 0, true, actionEvent -> {});
        addButton(frame, "Host game", 50, 50, 200, 50, true, actionEvent -> {
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
        });
        addButton(frame, "Join game", 50, 150, 200, 50, true, actionEvent -> {
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
        });
        addButton(frame, "Close", 50, 250, 200, 50, true, actionEvent -> {
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        });
        frame.repaint();
    }

    public void prepareLobby() {
        Monopoly.INSTANCE.setState(GameState.LOBBY);
        frame.getContentPane().removeAll();
        frame.repaint();

        Thread lobbyThread = new Thread() {
            @Override
            public void run() {

                addText(frame, "Connecting to server...", (frame.getWidth() / 2) - 250, frame.getHeight() / 2, 500, 25, true);
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
                                addText(frame, serverPlayer.getName(), 50, y, 500, 25);
                                if(!serverPlayer.getName().equals(client.name)) {
                                    addButton(frame, "Kick", 600, y, 150, 25, ableToKick, actionEvent -> {
                                        try {
                                            client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.KICKED);
                                        } catch (Exception ignored) {}
                                    });
                                } else {
                                    addButton(frame, "change name", 600, y, 150, 25, true, actionEvent -> {
                                        try {
                                            String name = JOptionPane.showInputDialog(null, "New name:", "Change name", JOptionPane.QUESTION_MESSAGE);
                                            if(client.serverMethod().changeName(client.name, name)) client.name = name; else JOptionPane.showMessageDialog(null, "This name is either already in use or too long!", "Change name", JOptionPane.WARNING_MESSAGE);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                }
                                y += 50;
                            }
                            addButton(frame, "add player", 50, frame.getHeight() - 100, 200, 50, true, actionEvent -> {
                                try {
                                    if(client.serverMethod().acceptsNewClient()) {
                                        client = new Client(ip, 25565, false);
                                        clients.add(clients.size(), client);
                                    }
                                } catch (Exception ignored) {}
                            });
                            addButton(frame, "add bot", 50, frame.getHeight() - 200, 200, 50, client.isHost, actionEvent -> {
                                JOptionPane.showMessageDialog(null, "This option is not possible yet", "Add bot", JOptionPane.WARNING_MESSAGE);
                            });
                            addButton(frame, "leave", frame.getWidth() - 250, frame.getHeight() - 100, 200, 50, true, actionEvent -> {
                                try {
                                    client.serverMethod().kick(client.name, DisconnectReason.CLIENT_CLOSED);
                                } catch (Exception ignored) {}
                            });
                            addButton(frame, "start", frame.getWidth() - 250, frame.getHeight() - 200, 200, 50, client.isHost, actionEvent -> {
                                try {
                                    client.serverMethod().start();
                                } catch (Exception ignored) {}
                            });
                            displayedServerPlayers = client.serverMethod().getServerPlayers();
                            for(int i = 0; i < clients.size(); i++) {
                                addPlayerButton(i);
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

        addStreetButton(frame, Street.BADSTRASSE, 100, 100, Direction.LEFT);

        frame.repaint();
    }

    private void setClient(int i) {
        client = clients.get(i);
    }

    private static JButton addButton(JFrame frame, String display, int x, int y, int width, int height, boolean enabled, ActionListener actionEvent) {
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
        frame.add(button);
        return button;
    }

    private static JButton addButton(JLayeredPane frame, String display, int x, int y, int width, int height, boolean enabled, int pos, ActionListener actionEvent) {
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
        frame.add(button, pos);
        return button;
    }

    private JButton addPlayerButton(int i) {
        int step = frame.getWidth() / clients.size();
        int[] value = {i};
        JButton button = addButton(frame, clients.get(i).name, i * step, 0, step, 60,true, (client == clients.get(value[0])), actionEvent -> {
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
                button = addButton(pane, "", x, y, 90, 70, true, 1, actionEvent -> {
                    selectedStreet = street;
                });
                button.setIcon(new ImageIcon("images/felder/left_background.png"));
                addImage(pane, "images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+1, 1);
                JLabel label;
                label = addText(pane, street.name, x+25, y+60, 70, 20, 2);
                label = addText(pane, street.price + "€", x+75, y+60, 70, 20, 3);
            }
            case UP -> {
                button = addButton(frame, "", x, y, 70, 90, false, actionEvent -> {
                    selectedStreet = street;
                });
                button.setIcon(new ImageIcon("images/felder/up_background.png"));
            }
            case RIGHT -> {
                button = addButton(frame, "", x, y, 90, 70, false, actionEvent -> {
                    selectedStreet = street;
                });
                button.setIcon(new ImageIcon("images/felder/right_background.png"));
            }
            case DOWN -> {
                button = addButton(frame, "", x, y, 70, 90, false, actionEvent -> {
                    selectedStreet = street;
                });
                button.setIcon(new ImageIcon("images/felder/down_background.png"));
            }
            default -> throw new RuntimeException();
        }
        frame.add(pane);
        pane.repaint();
        pane.setVisible(true);
        return pane;
    }

    private static JButton addButton(JFrame frame, String display, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        JButton button = addButton(frame, display, x, y, width, height, enabled, actionEvent);
        button.setSelected(selected);
        return button;
    }

    private static JButton addButton(JLayeredPane frame, String display, int x, int y, int width, int height, boolean enabled, boolean selected, int pos, ActionListener actionEvent) {
        JButton button = addButton(frame, display, x, y, width, height, enabled, pos, actionEvent);
        button.setSelected(selected);
        return button;
    }

    private static void addText(JFrame frame, String display, int x, int y, int width, int size, boolean centered) {
        JLabel label;
        if(centered) label = new JLabel(display, SwingConstants.CENTER); else label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        frame.add(label);
    }

    private static JLabel addImage(JFrame frame, String src, int x, int y) {
        ImageIcon icon = new ImageIcon(src);
        JLabel label = new JLabel(icon);
        label.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        frame.add(label);
        frame.repaint();
        return label;
    }

    private static JLabel addImage(JLayeredPane frame, String src, int x, int y, int pos) {
        ImageIcon icon = new ImageIcon(src);
        JLabel label = new JLabel(icon);
        label.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        frame.add(label, pos);
        frame.repaint();
        return label;
    }

    private static JLabel addImage(JFrame frame, String src, int x, int y, int width, int height) {
        JLabel label = addImage(frame, src, x, y);
        label.setIcon(new ImageIcon(((ImageIcon) label.getIcon()).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        label.setBounds(x, y, width, height);
        frame.repaint();
        return label;
    }

    private static JLabel addText(JFrame frame, String display, int x, int y, int width, int size) {
        JLabel label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        frame.add(label);
        return label;
    }

    private static JLabel addText(JLayeredPane frame, String display, int x, int y, int width, int size, int pos) {
        JLabel label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        frame.add(label, pos);
        return label;
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }
}
