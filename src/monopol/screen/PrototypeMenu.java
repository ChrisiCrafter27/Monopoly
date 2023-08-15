package monopol.screen;

import monopol.client.Client;
import monopol.core.GameState;
import monopol.core.Monopoly;
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
    private final KeyHandler keyHandler = new KeyHandler();
    private ArrayList<ServerPlayer> displayedServerPlayers = new ArrayList<>();
    private String ip;

    public PrototypeMenu() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setSize(new Dimension(1920, 1080));
        frame.addKeyListener(keyHandler);
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public void prepareMenu() {
        Monopoly.INSTANCE.setState(GameState.MAIN_MENU);
        displayedServerPlayers = new ArrayList<>();
        frame.getContentPane().removeAll();
        frame.repaint();

        addButton(frame, "Host game", 50, 50, 200, 50, true, actionEvent -> {
            boolean canKick = JOptionPane.showConfirmDialog(null, "Sollen alle Spieler Andere kicken können?", "Host game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
            boolean canSet = JOptionPane.showConfirmDialog(null, "Sollen alle Spieler Einstellungen ändern können?", "Host game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
            try {
                Monopoly.INSTANCE.openServer(new ServerSettings(canKick, canSet));
                client = new Client("localhost", 25565, true);
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
                client = new Client(ip, 25565, false);
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

                addText(frame, "Connecting to server...", frame.getWidth() / 2, frame.getHeight() / 2, 500, 25, true);
                frame.repaint();

                while(!isInterrupted() && client.name == null) {
                    if(client.closed()) {
                        interrupt();
                        prepareMenu();
                        return;
                    }
                }

                while(!isInterrupted()) {

                    //if(keyHandler.isKeyPressed(KeyEvent.VK_W)) JOptionPane.showMessageDialog(null, "You pressed W!", "W pressed", JOptionPane.PLAIN_MESSAGE);

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
                                        } catch (Exception ignored) {
                                        }
                                    });
                                } else {
                                    addButton(frame, "Namen ändern", 600, y, 150, 25, true, actionEvent -> {
                                        try {
                                            String name = JOptionPane.showInputDialog(null, "Neuer Name:", "Namen ändern", JOptionPane.QUESTION_MESSAGE);
                                            if(client.serverMethod().changeName(client.name, name)) client.name = name; else JOptionPane.showMessageDialog(null, "Dieser Name ist schon vergeben oder nicht erlaubt!", "Namen ändern", JOptionPane.WARNING_MESSAGE);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                }
                                y += 50;
                            }
                            addButton(frame, "Spieler hinzufügen", 50, frame.getHeight() - 100, 200, 50, true, actionEvent -> {
                                try {
                                    if(client.serverMethod().acceptsNewClient()) {
                                        client = new Client(ip, 25565, false);
                                        clients.add(clients.size(), client);
                                    }
                                } catch (Exception ignored) {}
                            });
                            addButton(frame, "Verlassen", frame.getWidth() - 250, frame.getHeight() - 100, 200, 50, true, actionEvent -> {
                                try {
                                    client.serverMethod().kick(client.name, DisconnectReason.CLIENT_CLOSED);
                                } catch (Exception ignored) {
                                }
                            });
                            displayedServerPlayers = client.serverMethod().getServerPlayers();
                            int step = frame.getWidth() / clients.size();
                            for(int i = 0; i < clients.size(); i++) {
                                int[] value = {i};
                                addButton(frame, clients.get(i).name, i * step, 0, step, 50, true, actionEvent -> {
                                    setClient(value[0]);
                                });
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
                    try {
                        sleep(100);
                    } catch (InterruptedException ignored) {}
                }
                System.out.println("The game should now start!");
                System.exit(0);
            }
        };
        lobbyThread.start();
    }

    private void setClient(int i) {
        client = clients.get(i);
    }

    private static void addButton(JFrame frame, String display, int x, int y, int width, int height, boolean enabled, ActionListener actionEvent){
        JButton button = new JButton(display);
        button.addActionListener(actionEvent);
        button.setBounds(x, y, width, height);
        button.setEnabled(enabled);
        frame.add(button);
    }

    private static void addText(JFrame frame, String display, int x, int y, int width, int size, boolean centered) {
        JLabel label;
        if(centered) label = new JLabel(display, JLabel.CENTER); else label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        frame.add(label);
    }

    private static void addText(JFrame frame, String display, int x, int y, int width, int size) {
        JLabel label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setBounds(x, y, width, size);
        frame.add(label);
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }
}
