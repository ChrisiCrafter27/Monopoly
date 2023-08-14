package monopol.screen;

import com.sun.source.doctree.AttributeTree;
import monopol.client.Client;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.server.DisconnectReason;
import monopol.server.Server;
import monopol.server.ServerPlayer;
import monopol.server.ServerSettings;
import monopol.utils.KeyHandler;

import javax.swing.*;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PrototypeMenu {
    private final JFrame frame = new JFrame("Monopoly - PrototypeWindow");
    private Client client;
    private final KeyHandler keyHandler = new KeyHandler();
    private ArrayList<ServerPlayer> displayedServerPlayers = new ArrayList<>();

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

        addButton(frame, "Host game", 50, 50, 200, 50, actionEvent -> {
            String name;
            do {
                name = JOptionPane.showInputDialog(null, "Please enter your name:", "Host game", JOptionPane.QUESTION_MESSAGE);
                if(name == null) return;
            } while(name.isEmpty());
            try {
                Server server = new Server(25565, new ServerSettings(false, true));
                server.open();
                client = new Client("localhost", 25565);
                prepareLobby(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to start server. Make sure there are no other running server on your PC!", "Failed to start server", JOptionPane.WARNING_MESSAGE);
            }
        });
        addButton(frame, "Join game", 50, 150, 200, 50, actionEvent -> {
            String name;
            do {
                name = JOptionPane.showInputDialog(null, "Please enter your name:", "Join game", JOptionPane.QUESTION_MESSAGE);
                if(name == null) return;
            } while(name.isEmpty());
            String ip;
            do {
                ip = JOptionPane.showInputDialog(null, "Please enter the IP-Address:", "Join game", JOptionPane.QUESTION_MESSAGE);
                if(ip == null) return;
            } while(ip.isEmpty());
            try {
                client = new Client(ip, 25565);
                prepareLobby(false);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Server not found. Make sure the IP-Address is correct!", "Server not found", JOptionPane.WARNING_MESSAGE);
            }
        });
        addButton(frame, "Close", 50, 250, 200, 50, actionEvent -> {
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        });
        frame.repaint();
    }

    public void prepareLobby(boolean host) {
        Monopoly.INSTANCE.setState(GameState.LOBBY);
        frame.getContentPane().removeAll();
        frame.repaint();

        boolean ableToKick;
        boolean ableToAccessSettings;
        try {
            if (host) ableToKick = true;
            else ableToKick = client.serverMethod().getServerSettings().allPlayersCanKick;
            if (host) ableToAccessSettings = true;
            else ableToAccessSettings = client.serverMethod().getServerSettings().allPlayersCanAccessSettings;
        } catch (RemoteException e) {
            ableToKick = host;
            ableToAccessSettings = host;
        }

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

                    if(keyHandler.isKeyPressed(KeyEvent.VK_W)) JOptionPane.showMessageDialog(null, "You pressed W!", "W pressed", JOptionPane.PLAIN_MESSAGE);

                    if(client.closed()) {
                        interrupt();
                        prepareMenu();
                        return;
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
                        if(shouldUpdate) {
                            frame.getContentPane().removeAll();
                            int y = 50;
                            for (ServerPlayer serverPlayer : client.serverMethod().getServerPlayers()) {
                                addText(frame, serverPlayer.getName(), 50, y, 500, 25);
                                if(!serverPlayer.getName().equals(client.name)) {
                                    addButton(frame, "Kick", 600, y, 100, 25, actionEvent -> {
                                        try {
                                            client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.KICKED);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                } else {
                                    addButton(frame, "Verlassen", 600, y, 100, 25, actionEvent -> {
                                        try {
                                            client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.CLIENT_CLOSED);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                    addButton(frame, "Namen ändern", 750, y, 150, 25, actionEvent -> {
                                        try {
                                            String name = JOptionPane.showInputDialog(null, "Neuer Name:", "Namen ändern", JOptionPane.QUESTION_MESSAGE);
                                            if(client.serverMethod().changeName(client.name, name)) client.name = name; else JOptionPane.showMessageDialog(null, "Dieser Name ist schon vergeben oder nicht erlaubt!", "Namen ändern", JOptionPane.WARNING_MESSAGE);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                }
                                y += 50;
                            }
                            frame.repaint();
                            displayedServerPlayers = client.serverMethod().getServerPlayers();
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

    private static void addButton(JFrame frame, String display, int x, int y, int width, int height, ActionListener actionEvent){
        JButton button = new JButton(display);
        button.addActionListener(actionEvent);
        button.setBounds(x, y, width, height);
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
