package monopol.client.screen;

import monopol.common.utils.JUtils;
import monopol.client.Client;
import monopol.common.core.Monopoly;
import monopol.common.utils.ServerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.Consumer;

public class MenuPane extends JLayeredPane {
    private String ip;
    private Client client;

    public MenuPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        reset();
    }

    public void init(ArrayList<Client> clients, Consumer<Client> prepareLobby, RootPane root) {
        reset();
        setVisible(true);

        add(addButton("invisible", 0, 0, 0, 0, true, actionEvent -> {}));
        add(addButton("Host game", 50, 50, 200, 50, Monopoly.INSTANCE.serverEnabled(), actionEvent -> {
            int input;
            input = JOptionPane.showConfirmDialog(null, "Dürfen die sich Mitspieler gegenseitig kicken?", "Spiel erstellen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(input == JOptionPane.CLOSED_OPTION) return;
            boolean canKick = input == JOptionPane.YES_OPTION;
            input = JOptionPane.showConfirmDialog(null, "Dürfen die Mitspieler Einstellungen ändern?", "Spiel erstellen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(input == JOptionPane.CLOSED_OPTION) return;
            boolean canSet = input == JOptionPane.YES_OPTION;
            try {
                ip = "localhost";
                Monopoly.INSTANCE.openServer(new ServerSettings(canKick, canSet));
                client = new Client(ip, 25565, true, root);
                clients.add(client);
                reset();
                prepareLobby.accept(client);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                JOptionPane.showMessageDialog(null, "Server konnte nicht gestartet werden. Laufen andere Server auf deinem PC?", "Spiel erstellen", JOptionPane.WARNING_MESSAGE);
            }
        }), JLayeredPane.MODAL_LAYER);
        add(addButton("Join game", 50, 150, 200, 50, true, actionEvent -> {
            do {
                String clipboard = "";
                try {
                    clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
                } catch (Exception ignored) {}
                ip = (String) JOptionPane.showInputDialog(null, "IP-Adresse eingeben:", "Spiel beitreten", JOptionPane.QUESTION_MESSAGE, null, null, clipboard);
                if(ip == null) return;
            } while(ip.isEmpty());
            try {
                client = new Client(ip, 25565, (ip.equals("localhost")), root);
                clients.add(client);
                reset();
                prepareLobby.accept(client);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Server nicht gefunden. Ist die IP-Adresse korrekt?", "Spiel beitreten", JOptionPane.WARNING_MESSAGE);
            }
        }), JLayeredPane.MODAL_LAYER);
        add(addButton("Close", 50, 250, 200, 50, true, actionEvent -> {
            System.exit(0);
        }), JLayeredPane.MODAL_LAYER);
        add(JUtils.addImage("images/Monopoly_client1.png", 0, 0, 1920, 1080), JLayeredPane.DEFAULT_LAYER);

        repaint();
    }

    public void reset() {
        removeAll();
        setVisible(false);
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
        button.setIcon(new ImageIcon(JUtils.imageIcon("images/DO_NOT_CHANGE/plain_button_2.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }
}
