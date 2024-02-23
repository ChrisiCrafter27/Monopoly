package monopol.client.screen;

import monopol.common.utils.JUtils;
import monopol.common.utils.KeyHandler;
import monopol.common.utils.ListUtils;
import monopol.client.Client;
import monopol.common.message.DisconnectReason;
import monopol.common.data.Player;

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

    private final JLabel connecting = JUtils.addText("Verbinde zum Server...", (1920 / 2) - 250, 1080 / 2, 500, 25, true);
    private final JButton ipAddress = JUtils.addButton("", null, 200, 590, 800, 30, true, false, actionEvent -> {});
    private final JLayeredPane playerList = new JLayeredPane();
    private final JButton addPlayer = JUtils.addButton("<html><div style='text-align: center;'>Spieler<br/>hinzufügen</div></html>", new Font(null, Font.PLAIN, 50), Color.BLACK, 450, 680, 270, 210, false, actionEvent -> {});
    private final JButton addBot = JUtils.addButton("<html><div style='text-align: center;'>Bot<br/>hinzufügen</div></html>", new Font(null, Font.PLAIN, 50), Color.BLACK, 790, 680, 270, 210, false, actionEvent -> {});
    private final JButton leave = JUtils.addButton("Verlassen", new Font(null, Font.PLAIN, 50), Color.BLACK, 0, 680, 380, 130, false, actionEvent -> {});
    private final JButton start = JUtils.addButton("Starten", new Font(null, Font.PLAIN, 50), Color.BLACK, 1540, 290, 380, 130, false, actionEvent -> {});
    private final JLabel hintergrund = JUtils.addImage("images/Main_pictures/2.Menü.png",0,60,1920,1020);

    public LobbyPane() {
        super();

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        playerList.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);


        add(connecting, JLayeredPane.POPUP_LAYER);
        add(ipAddress, JLayeredPane.MODAL_LAYER);
        add(playerList, JLayeredPane.MODAL_LAYER);
        add(addPlayer, JLayeredPane.POPUP_LAYER);
        add(addBot, JLayeredPane.POPUP_LAYER);
        add(leave, JLayeredPane.POPUP_LAYER);
        add(start, JLayeredPane.POPUP_LAYER);
        add(hintergrund, JLayeredPane.DEFAULT_LAYER);

        reset();
    }

    public void reset() {
        client = null;
        memory = new ArrayList<>();

        setVisible(false);
        connecting.setVisible(false);
        ipAddress.setVisible(false);
        ipAddress.setFont(new Font("Arial", Font.PLAIN, 20));
        ipAddress.setForeground(Color.BLACK);
        playerList.setVisible(false);
        addPlayer.setVisible(false);
        addBot.setVisible(false);
        leave.setVisible(false);
        start.setVisible(false);
        repaint();
    }

    public void init() {
        setVisible(true);
        connecting.setVisible(true);
        repaint();
    }

    public void requestUpdate() {
        requestUpdate = true;
    }

    private void updateButtons(ArrayList<Client> clients, RootPane root, boolean mayAddPlayer) {
        addPlayer.setEnabled(true);
        removeActionListener(addPlayer);
        addPlayer.addActionListener(actionEvent -> {
            try {
                if(mayAddPlayer && client.serverMethod().acceptsNewClient()) {
                    client = new Client(ip, 25565, false, root);
                    clients.add(clients.size(), client);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
        addPlayer.setVisible(true);

        addBot.setEnabled(true);
        removeActionListener(addBot);
        addBot.addActionListener(actionEvent -> {
            if(client.player.isHost) JOptionPane.showMessageDialog(null, "Diese Aktion ist noch nicht implementiert", "Bot hinzufügen", JOptionPane.INFORMATION_MESSAGE);
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
        int y = 310;

        boolean ableToKick;
        try {
            if (client.player.isHost) ableToKick = true;
            else ableToKick = client.serverMethod().getServerSettings().allPlayersCanKick;
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
            ableToKick = false;
        }

        Player host = players.stream().filter(player -> {
            try {
                return client.serverMethod().isHost(player.getName());
            } catch (RemoteException e) {
                return false;
            }
        }).findFirst().orElse(null);
        if(host != null) {
            players.remove(host);
            players.add(0, host);
        }

        for (Player player : players) {
            playerList.add(JUtils.addText(player.getName() + (client.serverMethod().isHost(player.getName()) ? " (Host)" : ""), 200, y, 500, 25, SwingConstants.LEFT, Color.BLACK));
            if(!player.getName().equals(client.player.getName())) {
                playerList.add(JUtils.addButton("Kick", 750, y, 150, 25, ableToKick && !client.serverMethod().isHost(player.getName()), actionEvent -> {
                    try {
                        client.serverMethod().kick(player.getName(), DisconnectReason.KICKED);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }));
            } else {
                playerList.add(JUtils.addButton("Namen ändern", 750, y, 150, 25, true, actionEvent -> {
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
        JButton button = JUtils.addButton("", 925, y, 25, 25, name.equals(client.player.getName()), actionEvent ->  {
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
            client = currentClient;
            this.ip = ip;
            updateList(players);
            updateButtons(clients, root, players.size() < 6);
            updateIp();
            repaint();
        }
        if(keyHandler.isKeyDown(KeyEvent.VK_SPACE) != spaceDown) {
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

    private void removeActionListener(JButton button) {
        for(ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }
}
