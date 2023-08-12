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
import java.rmi.RemoteException;

public class PrototypeMenu {
    private final JFrame frame = new JFrame("Monopoly - PrototypeWindow");
    private Client client;
    private final KeyHandler keyHandler = new KeyHandler();

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
        });
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

                while(!isInterrupted()) {

                    if(keyHandler.isKeyPressed(10)) JOptionPane.showMessageDialog(null, "You pressed Enter!", "Enter pressed", JOptionPane.PLAIN_MESSAGE);

                    try {
                        frame.getContentPane().removeAll();
                        int y = 50;
                        for (ServerPlayer serverPlayer : client.serverMethod().getServerPlayers()) {
                            addText(frame, serverPlayer.getName(), 50, y, 500, 25);
                            addButton(frame, "kick", 600, y, 100, 25, actionEvent -> {
                                try {
                                    client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.KICKED);
                                } catch (RemoteException e) {
                                    client.close();
                                    interrupt();
                                    prepareMenu();
                                }
                            });
                            y += 50;
                        }
                    } catch (RemoteException e) {
                        System.err.println(e.getMessage());
                        client.close();
                        interrupt();
                        prepareMenu();
                    }
                    frame.repaint();
                    try {
                        sleep(1000);
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

    private static void addText(JFrame frame, String display, int x, int y, int width, int height) {
        JLabel label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, 25));
        label.setBounds(x, y, width, height);
        frame.add(label);
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }
}
