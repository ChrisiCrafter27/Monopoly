package monopol.screen;

import monopol.client.Client;
import monopol.core.Monopoly;
import monopol.server.ServerSettings;
import monopol.utils.JUtils;

import javax.swing.*;
import java.awt.*;
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

    public void init(ArrayList<Client> clients, Consumer<Client> prepareLobby) {
        reset();
        setVisible(true);

        add(addButton("invisible", 0, 0, 0, 0, true, actionEvent -> {}));
        add(addButton("Host game", 50, 50, 200, 50, true, actionEvent -> {
            int input;
            input = JOptionPane.showConfirmDialog(null, "Allow all players to kick each other?", "Host game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(input == JOptionPane.CLOSED_OPTION) return;
            boolean canKick = input == JOptionPane.YES_OPTION;
            input = JOptionPane.showConfirmDialog(null, "Allow all players to access settings?", "Host game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(input == JOptionPane.CLOSED_OPTION) return;
            boolean canSet = input == JOptionPane.YES_OPTION;
            try {
                ip = "localhost";
                Monopoly.INSTANCE.openServer(new ServerSettings(canKick, canSet));
                client = new Client(ip, 25565, true);
                clients.add(client);
                reset();
                prepareLobby.accept(client);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start server. Make sure there are no other running server on your PC!", "Failed to start server", JOptionPane.WARNING_MESSAGE);
            }
        }), JLayeredPane.MODAL_LAYER);
        add(addButton("Join game", 50, 150, 200, 50, true, actionEvent -> {
            do {
                ip = JOptionPane.showInputDialog(null, "Please enter the IP-Address:", "Join game", JOptionPane.QUESTION_MESSAGE);
                if(ip == null) return;
            } while(ip.isEmpty());
            try {
                client = new Client(ip, 25565, (ip.equals("localhost")));
                clients.add(client);
                reset();
                prepareLobby.accept(client);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Server not found. Make sure the IP-Address is correct!", "Server not found", JOptionPane.WARNING_MESSAGE);
            }
        }), JLayeredPane.MODAL_LAYER);
        add(addButton("Close", 50, 250, 200, 50, true, actionEvent -> {
            System.exit(0);
        }), JLayeredPane.MODAL_LAYER);
        add(addImage("images/Monopoly_client1.png", 0, 0, 1920, 1080), JLayeredPane.DEFAULT_LAYER);

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
        button.setIcon(new ImageIcon(new ImageIcon("images/DO_NOT_CHANGE/plain_button_2.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    public JLabel addImage(String src, int x, int y, int width, int height) {
        JLabel label = addImage(src, x, y);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        label.setIcon(new ImageIcon(((ImageIcon) label.getIcon()).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT)));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public JLabel addImage(String src, int x, int y) {
        ImageIcon icon = new ImageIcon(src);
        icon = new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT));
        JLabel label = new JLabel(icon);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), icon.getIconWidth(), icon.getIconHeight());
        return label;
    }
}
