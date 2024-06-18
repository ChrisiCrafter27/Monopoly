package monopol.client.screen;

import monopol.common.core.Monopoly;
import monopol.common.utils.JUtils;
import monopol.common.utils.ServerProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.NetworkInterface;
import java.util.Map;
import java.util.Optional;

public class ServerScreen {
    public static ServerScreen current;

    private final JFrame frame = new JFrame("Server Optionen");
    private ServerProperties serverProperties;
    private Map<String, NetworkInterface> networkInterfaces = ServerProperties.networkInterfaces();

    public ServerScreen(ServerProperties serverProperties) {
        if(current != null) current.close();
        current = this;
        this.serverProperties = serverProperties;
    }

    public void show() {
        if(Monopoly.INSTANCE.serverEnabled()) return;

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.repaint();

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel panel1 = new JPanel(new GridLayout(0, 2, 10, 10));
            JPanel panel2 = new JPanel(new GridLayout(0, 1, 10, 10));
            JPanel panel3 = new JPanel(new GridLayout(0, 2, 10, 10));

            panel1.add(new JLabel("Jeder darf kicken:"));
            JCheckBox everyoneCanKick = new JCheckBox();
            everyoneCanKick.setSelected(serverProperties.serverSettings.allPlayersCanKick);
            panel1.add(everyoneCanKick);

            panel1.add(new JLabel(" "));
            panel1.add(new JLabel(" "));

            panel1.add(new JLabel("Main Port:"));
            JTextField mainPort = new JTextField("" + serverProperties.port1);
            mainPort.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) {
                        e.consume();
                    }
                }
            });
            mainPort.addFocusListener(new FocusListener() {
                private int value;
                @Override
                public void focusGained(FocusEvent e) {
                    try {
                        if(mainPort.getText().length() > 5) throw new NumberFormatException();
                        value = Integer.parseInt(mainPort.getText());
                        if(value < 1) throw new NumberFormatException();
                    } catch (NumberFormatException ignored) {
                        value = 25565;
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        if(mainPort.getText().length() > 5) throw new NumberFormatException();
                        int value = Integer.parseInt(mainPort.getText());
                        if(value < 1) throw new NumberFormatException();
                    } catch (NumberFormatException ignored) {
                        mainPort.setText("" + value);
                    }
                }
            });
            panel1.add(mainPort);

            panel1.add(new JLabel("Unicast Port:"));
            JTextField unicastPort = new JTextField("" + serverProperties.port2);
            unicastPort.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) {
                        e.consume();
                    }
                }
            });
            unicastPort.addFocusListener(new FocusListener() {
                private int value;
                @Override
                public void focusGained(FocusEvent e) {
                    try {
                        if(unicastPort.getText().length() > 5) throw new NumberFormatException();
                        value = Integer.parseInt(unicastPort.getText());
                        if(value < 1) throw new NumberFormatException();
                    } catch (NumberFormatException ignored) {
                        value = 1199;
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        if(unicastPort.getText().length() > 5) throw new NumberFormatException();
                        int value = Integer.parseInt(unicastPort.getText());
                        if(value < 1) throw new NumberFormatException();
                    } catch (NumberFormatException ignored) {
                        unicastPort.setText("" + value);
                    }
                }
            });
            panel1.add(unicastPort);

            panel1.add(new JLabel(" "));
            panel1.add(new JLabel(" "));

            panel1.add(new JLabel("Adapter:"));
            JComboBox<String> adapterNames = new JComboBox<>(networkInterfaces.keySet().toArray(String[]::new));
            adapterNames.setSelectedItem(ServerProperties.defaultNetworkInterface());
            panel1.add(adapterNames);

            JButton resetButton = new JButton("Reset");
            resetButton.addActionListener(e -> {
                serverProperties = new ServerProperties();
                networkInterfaces = ServerProperties.networkInterfaces();
                everyoneCanKick.setSelected(serverProperties.serverSettings.allPlayersCanKick);
                mainPort.setText("" + serverProperties.port1);
                unicastPort.setText("" + serverProperties.port2);
                adapterNames.removeAllItems();
                networkInterfaces.keySet().forEach(adapterNames::addItem);
            });

            JButton abortButton = new JButton("Abbrechen");
            abortButton.addActionListener(e -> close());

            JButton saveButton = new JButton("Speichern");
            saveButton.addActionListener(e -> {
                serverProperties.serverSettings.allPlayersCanKick = everyoneCanKick.isSelected();
                serverProperties.port1 = tryParse(mainPort.getText()).orElse(25565);
                serverProperties.port2 = tryParse(unicastPort.getText()).orElse(1199);
                serverProperties.ip = ServerProperties.ip(networkInterfaces.get(adapterNames.getItemAt(adapterNames.getSelectedIndex())));
                close();
            });

            panel2.add(new JLabel(" "));
            panel2.add(resetButton);
            panel3.add(abortButton);
            panel3.add(saveButton);

            panel.add(panel1);
            panel.add(Box.createVerticalStrut(10));
            panel.add(panel2);
            panel.add(Box.createVerticalStrut(10));
            panel.add(panel3);

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.add(panel);
            frame.pack();
            frame.setLocation((int) (JUtils.SCREEN_WIDTH / 2d - frame.getWidth() / 2d), (int) (JUtils.SCREEN_HEIGHT / 2d - frame.getHeight() / 2d));
            frame.setVisible(true);
        });
    }

    private Optional<Integer> tryParse(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }
}
