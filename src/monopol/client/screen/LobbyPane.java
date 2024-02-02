package monopol.client.screen;

import monopol.client.Client;
import monopol.common.log.DebugLogger;
import monopol.common.message.DisconnectReason;
import monopol.common.data.Player;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyPane extends JLayeredPane {
    public static final Map<Color, String> COLORS = new HashMap<>();
    static {
        COLORS.putAll(Map.of(Color.YELLOW, "<html><font color=#ffff00>■</font>", Color.ORANGE, "<html><font color=#ffc800>■</font>", Color.RED, "<html><font color=#ff0000>■</font>", Color.MAGENTA, "<html><font color=#ff00ff>■</font>", Color.PINK, "<html><font color=#ffafaf>■</font>"));
        COLORS.putAll(Map.of(Color.CYAN, "<html><font color=#00ffff>■</font>", Color.BLUE, "<html><font color=#0000ff>■</font>", Color.GREEN, "<html><font color=#00ff00>■</font>", Color.WHITE, "<html><font color=#ffffff>■</font>", Color.LIGHT_GRAY, "<html><font color=#c0c0c0>■</font>"));
        COLORS.putAll(Map.of(Color.GRAY, "<html><font color=#808080>■</font>", Color.DARK_GRAY, "<html><font color=#404040>■</font>", Color.BLACK, "<html><font color=#000000>■</font>"));
    }
    
    private Client client;
    private boolean mustUpdate;
    private boolean requestUpdate = false;

    private ArrayList<Player> memory = new ArrayList<>();
    private boolean spaceDown = false;
    private String ip = "";

    private final JLabel connecting = addText("Verbinde zum Server...", (1920 / 2) - 250, 1080 / 2, 500, 25, true);
    private final JButton ipAddress = addButton((1920/2)-250, 1080-70, 500, 30, actionEvent -> {});
    private final JLayeredPane playerList = new JLayeredPane();
    private final JButton addPlayer = addButton("Spieler hinzufügen", 50, 1080 - 100, 200, 50, false, actionEvent -> {});
    private final JButton addBot = addButton("Bot hinzufügen", 50, 1080 - 200, 200, 50, false, actionEvent -> {});
    private final JButton leave = addButton("Verlassen", 1920 - 250, 1080 - 100, 200, 50, false, actionEvent -> {});
    private final JButton start = addButton("Starten", 1920 - 250, 1080 - 200, 200, 50, false, actionEvent -> {});
    private final  JLabel hintergrund = JUtils.addImage("images/Main_pictures/2.Menü.png",0,0,1920,1080);

    public LobbyPane() {
        super();

        DebugLogger.INSTANCE.log().info("[LobbyPane] initiating...");

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        playerList.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);


        add(connecting, POPUP_LAYER);
        add(ipAddress, MODAL_LAYER);
        add(playerList, MODAL_LAYER);
        add(addPlayer, POPUP_LAYER);
        add(addBot, POPUP_LAYER);
        add(leave, POPUP_LAYER);
        add(start, POPUP_LAYER);
        add(hintergrund, DEFAULT_LAYER);

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

    public void requestUpdate() {
        requestUpdate = true;
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
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
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
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
        leave.setVisible(true);

        start.setEnabled(client.player.isHost);
        removeActionListener(start);
        start.addActionListener(actionEvent -> {
            try {
                client.serverMethod().start();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
        start.setVisible(true);
    }

    private void updateList(ArrayList<Player> players) throws RemoteException {
        memory = players;

        playerList.removeAll();
        int y = 150;

        boolean ableToKick;
        try {
            if (client.player.isHost) ableToKick = true;
            else ableToKick = client.serverMethod().getServerSettings().allPlayersCanKick;
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
            ableToKick = false;
        }

        for (Player player : players) {
            playerList.add(addText(player.getName() + (client.serverMethod().isHost(player.getName()) ? " (Host)" : ""), 50, y, 500, 25, false));
            if(!player.getName().equals(client.player.getName())) {
                playerList.add(addButton("Kick", 600, y, 150, 25, ableToKick && !client.serverMethod().isHost(player.getName()), actionEvent -> {
                    try {
                        client.serverMethod().kick(player.getName(), DisconnectReason.KICKED);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }));
            } else {
                playerList.add(addButton("Namen ändern", 600, y, 150, 25, true, actionEvent -> {
                    try {
                        String name = JOptionPane.showInputDialog(null, "Neuer Name:", "Namen ändern", JOptionPane.QUESTION_MESSAGE);
                        if(client.serverMethod().changeName(client.player.getName(), name)) {
                            client.player.setName(name);
                            mustUpdate = true;
                        } else JOptionPane.showMessageDialog(null, "Dieser Name wird schon verwendet oder ist zu lang!", "Namen ändern", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }));
            }
            playerList.add(playerButton(player.getColor(), y, player.getName()));
            y += 50;
        }
    }

    private JButton playerButton(Color background, int y, String name) {
        JButton button = JUtils.addButton("", 775, y, 25, 25, name.equals(client.player.getName()), actionEvent ->  {
            List<Color> colors = COLORS.keySet().stream().filter(color -> {
                try {
                    return client.serverMethod().getPlayers().stream().map(Player::getColor).noneMatch(color::equals);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            int result = JOptionPane.showOptionDialog(null, "Wähle eine neue Farbe aus oder drücke ESC", "Farbe wählen", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, colors.stream().map(COLORS::get).toArray(), null);
            if(result != JOptionPane.CLOSED_OPTION) {
                try {
                    if(!client.serverMethod().changeColor(name, colors.get(result))) JOptionPane.showMessageDialog(null, "Farbe konnte nicht geändert werden.", "Farbe wählen", JOptionPane.WARNING_MESSAGE);
                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(null, "Farbe konnte nicht geändert werden.", "Farbe wählen", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setIcon(null);
        return button;
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

    public void update(ArrayList<Player> players, Client currentClient, ArrayList<Client> clients, String ip, KeyHandler keyHandler, boolean forceUpdate, RootPane root) throws RemoteException {
        connecting.setVisible(false);

        if(!ListUtils.equals(players, memory) || !currentClient.equals(client) || !ip.equals(this.ip) || forceUpdate || requestUpdate) {
            requestUpdate = false;
            DebugLogger.INSTANCE.log().info("[LobbyPane] updating list...");
            client = currentClient;
            this.ip = ip;
            updateList(players);
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
