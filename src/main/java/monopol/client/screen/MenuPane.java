package monopol.client.screen;

import monopol.common.utils.JUtils;
import monopol.client.Client;
import monopol.common.core.Monopoly;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.function.Consumer;

public class MenuPane extends JLayeredPane {
    private Client client;

    public MenuPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        reset();
    }

    public void init(ArrayList<Client> clients, Consumer<Client> prepareLobby, RootPane root) {
        SwingUtilities.invokeLater(() -> {
            reset();
            setVisible(true);

            add(JUtils.addButton("Spiel erstellen", 90, 325, 800, 200, 100, true, actionEvent -> {
                try {
                    Monopoly.INSTANCE.startServer();
                    Monopoly.INSTANCE.openServer();
                    client = new Client(Monopoly.INSTANCE.serverProperties().ip, Monopoly.INSTANCE.serverProperties(), true, root);
                    clients.add(client);
                    reset();
                    prepareLobby.accept(client);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    JOptionPane.showMessageDialog(Monopoly.INSTANCE.parentComponent, "Server konnte nicht gestartet werden. Laufen andere Server auf deinem PC?", "Spiel erstellen", JOptionPane.WARNING_MESSAGE);
                }
            }), JLayeredPane.MODAL_LAYER);
            add(JUtils.addButton("Spiel beitreten", 90, 550, 800, 200, 100, true, actionEvent -> {
                String ip;
                do {
                    String clipboard = "";
                    try {
                        clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
                    } catch (Exception ignored) {}
                    ip = (String) JOptionPane.showInputDialog(Monopoly.INSTANCE.parentComponent, "IP-Adresse eingeben:", "Spiel beitreten", JOptionPane.QUESTION_MESSAGE, null, null, clipboard);
                    if(ip == null) return;
                } while(ip.isEmpty());
                try {
                    client = new Client((Inet4Address) Inet4Address.getByName(ip), Monopoly.INSTANCE.serverProperties(), false, root);
                    clients.add(client);
                    reset();
                    prepareLobby.accept(client);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(Monopoly.INSTANCE.parentComponent, "Server nicht gefunden. Ist die IP-Adresse korrekt?", "Spiel beitreten", JOptionPane.WARNING_MESSAGE);
                }
            }), JLayeredPane.MODAL_LAYER);
            if(Monopoly.INSTANCE.serverEnabled()) {
                add(JUtils.addButton("Schließen", 90, 900, 400, 100, 50, true, actionEvent -> System.exit(0)), JLayeredPane.MODAL_LAYER);
            } else {
                add(JUtils.addButton("Netzwerk", 90, 800, 400, 100, 50, true, actionEvent -> new ServerScreen(Monopoly.INSTANCE.serverProperties()).show()), JLayeredPane.MODAL_LAYER);
                add(JUtils.addButton("Schließen", 90, 950, 400, 100, 50, true, actionEvent -> System.exit(0)), JLayeredPane.MODAL_LAYER);
            }
            add(JUtils.addImage("images/Monopoly_client1.png", 0, 0, 1920, 1080), JLayeredPane.DEFAULT_LAYER);
        });
    }

    public void reset() {
        removeAll();
        setVisible(false);
    }
}
