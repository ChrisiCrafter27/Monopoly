package monopol.screen;

import monopol.client.Client;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PrototypeMenu {
    private final JFrame frame = new JFrame("Monopoly - PrototypeWindow");

    public PrototypeMenu() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setSize(new Dimension(1920, 1080));
        frame.setPreferredSize(new Dimension(1920, 1080));
        frame.setResizable(true);
        frame.setVisible(true);
    }

    public void prepareMenu() {
        addButton(frame, "Host game", 50, 50, 200, 50, actionEvent -> {
            String name;
            do {
                name = JOptionPane.showInputDialog(null, "Please enter your name:", "Host game", JOptionPane.QUESTION_MESSAGE);
            } while(name == null || name.isEmpty());
            try {
                Server server = new Server(25565);
                server.open();
                Client client = new Client("localhost", 25565);
                Monopoly.INSTANCE.setState(GameState.LOBBY);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to start server. Make sure there are no other running server on your PC!", "Failed to start server", JOptionPane.WARNING_MESSAGE);
            }
        });
        addButton(frame, "Join game", 50, 150, 200, 50, actionEvent -> {
            String name;
            do {
                name = JOptionPane.showInputDialog(null, "Please enter your name:", "Join game", JOptionPane.QUESTION_MESSAGE);
            } while(name == null || name.isEmpty());
            String ip;
            do {
                ip = JOptionPane.showInputDialog(null, "Please enter the IP-Address:", "Join game", JOptionPane.QUESTION_MESSAGE);
            } while(ip == null || ip.isEmpty());
            try {
                Client client = new Client(ip, 25565);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Server not found. Make sure the IP-Address is correct!", "Server not found", JOptionPane.WARNING_MESSAGE);
                Monopoly.INSTANCE.setState(GameState.LOBBY);

            }
        });
    }

    private static void addButton(JFrame frame, String display, int x, int y, int width, int height, ActionListener actionListener){
        JButton button = new JButton(display);
        button.addActionListener(actionListener);
        button.setBounds(x, y, width, height);
        frame.add(button);
    }

    public static void main(String[] args) {
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }
}
