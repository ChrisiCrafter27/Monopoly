package monopol.client.screen;

import monopol.client.Client;
import monopol.common.log.DebugLogger;
import monopol.server.DisconnectReason;
import monopol.server.ServerPlayer;
import monopol.common.utils.JUtils;
import monopol.common.utils.KeyHandler;
import monopol.common.utils.ListUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class LobbyPane extends JLayeredPane {
    private Client client;
    private boolean mustUpdate;

    private ArrayList<ServerPlayer> memory = new ArrayList<>();
    private boolean spaceDown = false;
    private String ip = "";

    private final JLabel connecting = addText("Verbinde zum Server...", (1920 / 2) - 250, 1080 / 2, 500, 25, true);
    private final JButton ipAddress = addButton((1920/2)-250, 1080-70, 500, 30, actionEvent -> {});
    private final JPanel playerList = new JPanel();
    private final JButton addPlayer = addButton("Spieler hinzufügen", 50, 1080 - 100, 200, 50, false, actionEvent -> {});
    private final JButton addBot = addButton("Bot hinzufügen", 50, 1080 - 200, 200, 50, false, actionEvent -> {});
    private final JButton leave = addButton("Verlassen", 1920 - 250, 1080 - 100, 200, 50, false, actionEvent -> {});
    private final JButton start = addButton("Starten", 1920 - 250, 1080 - 200, 200, 50, false, actionEvent -> {});

    public LobbyPane() {
        super();

        DebugLogger.INSTANCE.log().info("[LobbyPane] initiating...");

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        playerList.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        playerList.setLayout(null);

        add(connecting, POPUP_LAYER);
        add(ipAddress, MODAL_LAYER);
        add(playerList, MODAL_LAYER);
        add(addPlayer, POPUP_LAYER);
        add(addBot, POPUP_LAYER);
        add(leave, POPUP_LAYER);
        add(start, POPUP_LAYER);

        reset();
    }

    public void reset() {
        DebugLogger.INSTANCE.log().info("[LobbyPane] resetting...");

        client = null;
        memory = new ArrayList<>();

        setVisible(false);
        connecting.setVisible(false);
        ipAddress.setVisible(false);
        ipAddress.setFont(new Font("Arial", Font.PLAIN, 30));
        playerList.setVisible(false);
        addPlayer.setVisible(false);
        addBot.setVisible(false);
        leave.setVisible(false);
        start.setVisible(false);
        repaint();
    }

    public void init() {
        DebugLogger.INSTANCE.log().info("[LobbyPane] waiting for server...");

        setVisible(true);
        connecting.setVisible(true);
        repaint();
    }

    private void updateButtons(ArrayList<Client> clients, RootPane root) {
        addPlayer.setEnabled(true);
        removeActionListener(addPlayer);
        addPlayer.addActionListener(actionEvent -> {
            try {
                if(client.serverMethod().acceptsNewClient()) {
                    client = new Client(ip, 25565, false, root);
                    clients.add(clients.size(), client);
                }
            } catch (Exception ignored) {}
        });
        addPlayer.setVisible(true);

        addBot.setEnabled(client.player.isHost);
        removeActionListener(addBot);
        addBot.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(null, "Diese Aktion ist noch nicht implementiert", "Bot hinzufügen", JOptionPane.WARNING_MESSAGE);
        });
        addBot.setVisible(true);

        leave.setEnabled(true);
        removeActionListener(leave);
        leave.addActionListener(actionEvent -> {
            try {
                client.serverMethod().kick(client.player.getName(), DisconnectReason.CLIENT_CLOSED);
            } catch (Exception ignored) {}
        });
        leave.setVisible(true);

        start.setEnabled(client.player.isHost);
        removeActionListener(start);
        start.addActionListener(actionEvent -> {
            try {
                client.serverMethod().start();
            } catch (Exception ignored) {}
        });
        start.setVisible(true);
    }

    private void updateList(ArrayList<ServerPlayer> serverPlayers) throws RemoteException {
        memory = serverPlayers;

        playerList.removeAll();
        int y = 150;

        boolean ableToKick;
        try {
            if (client.player.isHost) ableToKick = true;
            else ableToKick = client.serverMethod().getServerSettings().allPlayersCanKick;
        } catch (RemoteException e) {
            ableToKick = false;
        }

        for (ServerPlayer serverPlayer : serverPlayers) {
            playerList.add(addText(serverPlayer.getName() + (client.serverMethod().isHost(serverPlayer.getName()) ? " (Host)" : ""), 50, y, 500, 25, false));
            if(!serverPlayer.getName().equals(client.player.getName())) {
                playerList.add(addButton("Kick", 600, y, 150, 25, ableToKick && !client.serverMethod().isHost(serverPlayer.getName()), actionEvent -> {
                    try {
                        client.serverMethod().kick(serverPlayer.getName(), DisconnectReason.KICKED);
                    } catch (Exception ignored) {}
                }));
            } else {
                playerList.add(addButton("Namen ändern", 600, y, 150, 25, true, actionEvent -> {
                    try {
                        String name = JOptionPane.showInputDialog(null, "Neuer Name:", "Namen ändern", JOptionPane.QUESTION_MESSAGE);
                        if(client.serverMethod().changeName(client.player.getName(), name)) {
                            client.player.setName(name);
                            mustUpdate = true;
                        } else JOptionPane.showMessageDialog(null, "Dieser Name wird schon verwendet oder ist zu lang!", "Namen ändern", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ignored) {
                    }
                }));
            }
            y += 50;
        }
    }

    private void updateIp() {
        ipAddress.setText(spaceDown ? "IP-Adresse: " + ip : "Klicken um IP zu kopieren");
        removeActionListener(ipAddress);
        ipAddress.addActionListener(actionEvent -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(ip), null);
        });
        ipAddress.setVisible(true);
    }

    public void update(ArrayList<ServerPlayer> serverPlayers, Client currentClient, ArrayList<Client> clients, String ip, KeyHandler keyHandler, boolean forceUpdate, RootPane root) throws RemoteException {
        connecting.setVisible(false);

        if(!ListUtils.equals(serverPlayers, memory) || !currentClient.equals(client) || !ip.equals(this.ip) || forceUpdate) {
            DebugLogger.INSTANCE.log().info("[LobbyPane] updating list...");
            client = currentClient;
            this.ip = ip;
            updateList(serverPlayers);
            updateButtons(clients, root);
            updateIp();
            repaint();
        }
        if(keyHandler.isKeyDown(KeyEvent.VK_SPACE) != spaceDown) {
            DebugLogger.INSTANCE.log().info("[LobbyPane] updating ip...");
            spaceDown = keyHandler.isKeyDown(KeyEvent.VK_SPACE);
            this.ip = ip;
            updateIp();
            repaint();
        }

        playerList.setVisible(true);
    }

    public Client getClient() {
        return client;
    }

    public boolean mustUpdate() {
        if(mustUpdate) {
            mustUpdate = false;
            return true;
        }
        return false;
    }

    private JLabel addText(String display, int x, int y, int width, int height, boolean centered) {
        JLabel label;
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        if(centered) label = new JLabel(display, SwingConstants.CENTER); else label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, (int) ( height*1.2));
        return label;
    }

    private JButton addButton(String display, int x, int y, int width, int height, boolean enabled, ActionListener actionEvent) {
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

    public JButton addButton(int x, int y, int width, int height, ActionListener actionEvent) {
        JButton button = new JButton("");
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        button.addActionListener(actionEvent);
        button.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    private void removeActionListener(JButton button) {
        for(ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }
}
