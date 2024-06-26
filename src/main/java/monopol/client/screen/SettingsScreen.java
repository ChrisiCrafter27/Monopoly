package monopol.client.screen;

import monopol.common.settings.BooleanSetting;
import monopol.common.settings.EnumSetting;
import monopol.common.settings.IntSetting;
import monopol.common.settings.Setting;
import monopol.common.utils.JUtils;
import monopol.server.events.BuildRule;
import monopol.server.events.Events;
import monopol.server.events.MegaEditionEvents;
import monopol.server.events.OwnedCardsOfColorGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsScreen<T extends Events> {
    public static SettingsScreen<?> current;

    private final JFrame frame = new JFrame("Einstellungen");
    private final Events.Factory<T> factory;
    private final Consumer<T> set;
    private List<Setting<?, ?>> settingsList;
    private int i;

    public SettingsScreen(Events.Factory<T> factory, Events prev, Consumer<T> set) {
        if(current != null) current.close();
        current = this;
        this.factory = factory;
        this.set = set;
        this.settingsList = settings(prev != null ? prev : defaultEvents());
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.repaint();

            ToolTipManager.sharedInstance().setDismissDelay(30000);
            ToolTipManager.sharedInstance().setInitialDelay(100);
            ToolTipManager.sharedInstance().setReshowDelay(100);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel panel1 = new JPanel(new GridLayout(0, 2, 10, 10));
            JPanel panel2 = new JPanel(new GridLayout(0, 1, 10, 10));
            JPanel panel3 = new JPanel(new GridLayout(0, 2, 10, 10));

            Map<String, JComponent> componentMap = new HashMap<>();

            for (Setting<?, ?> setting : settingsList) {
                JLabel label = new JLabel(setting.name() + ": ");
                if(setting.tooltip() != null) label.setToolTipText(setting.tooltip());
                JComponent component = null;
                String name = setting.name();

                if(setting instanceof IntSetting intSetting) {
                    label.setText(label.getText() + " [" + intSetting.min() + "; " + intSetting.max() + "]");
                    JTextField textField = new JTextField(String.valueOf(intSetting.defaultValue()));
                    textField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            char c = e.getKeyChar();
                            if (!(Character.isDigit(c) || c == KeyEvent.VK_MINUS)) {
                                e.consume();
                            }
                        }
                    });
                    textField.addFocusListener(new FocusListener() {
                        @Override
                        public void focusGained(FocusEvent e) {}
                        @Override
                        public void focusLost(FocusEvent e) {
                            try {
                                int value = Integer.parseInt(((JTextField) e.getComponent()).getText());
                                if(value > intSetting.max() || value < intSetting.min()) throw new NumberFormatException();
                            } catch (NumberFormatException ignored) {
                                ((JTextField) componentMap.get(intSetting.name())).setText(String.valueOf(intSetting.defaultValue()));
                            }
                        }
                    });
                    intSetting.setComponent(textField);
                    component = textField;
                } else if(setting instanceof EnumSetting<?> enumSetting) {
                    Enum<?>[] enumConstants = enumSetting.values();
                    JComboBox comboBox = new JComboBox<>(enumConstants);
                    comboBox.setSelectedItem(enumSetting.defaultValue());
                    enumSetting.setComponent(comboBox);
                    component = comboBox;
                } else if(setting instanceof BooleanSetting booleanSetting) {
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setSelected(booleanSetting.defaultValue());
                    booleanSetting.setComponent(checkBox);
                    component = checkBox;
                }

                panel1.add(label);
                panel1.add(component);

                componentMap.put(name, component);
            }

            JButton presetButton = new JButton("Voreinstellungen");
            presetButton.addActionListener(e -> {
                Map<String, Events> presets = Map.of(
                        "Basisspiel", new MegaEditionEvents(
                                false,
                                16,
                                true,
                                false,
                                false,
                                false,
                                false,
                                2500,
                                200,
                                true,
                                true,
                                true,
                                false,
                                false,
                                BuildRule.ANYWHERE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL
                        ),
                        "Mega-Edition", new MegaEditionEvents(
                                false,
                                16,
                                true,
                                true,
                                true,
                                true,
                                true,
                                2500,
                                200,
                                true,
                                true,
                                true,
                                false,
                                false,
                                BuildRule.ANYWHERE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                                OwnedCardsOfColorGroup.ALL
                        ),
                        "Schnelles Spiel", new MegaEditionEvents(
                                false,
                                16,
                                true,
                                true,
                                false,
                                true,
                                true,
                                2500,
                                200,
                                true,
                                true,
                                true,
                                false,
                                false,
                                BuildRule.ANYWHERE,
                                OwnedCardsOfColorGroup.ONE,
                                OwnedCardsOfColorGroup.ONE,
                                OwnedCardsOfColorGroup.ONE,
                                OwnedCardsOfColorGroup.ONE,
                                OwnedCardsOfColorGroup.TWO,
                                OwnedCardsOfColorGroup.TWO
                        )
                );
                List<String> options = new ArrayList<>(presets.keySet());
                int result = JOptionPane.showOptionDialog(frame, "Wähle eine Vorlage:", "Vorlage wählen", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options.toArray(), null);
                if(result != JOptionPane.CLOSED_OPTION) {
                    settingsList = settings(presets.get(options.get(result)));
                    show();
                }
            });

            JButton abortButton = new JButton("Abbrechen");
            abortButton.addActionListener(e -> close());

            JButton saveButton = new JButton("Speichern");
            saveButton.addActionListener(e -> {
                set.accept(events());
                close();
            });

            panel2.add(presetButton);
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

    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }

    private T defaultEvents() {
        return factory.create(
                false,
                16,
                true,
                true,
                false,
                true,
                true,
                2500,
                200,
                true,
                true,
                true,
                false,
                false,
                BuildRule.ANYWHERE,
                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                OwnedCardsOfColorGroup.ALL_BUT_ONE,
                OwnedCardsOfColorGroup.ALL
        );
    }

    private List<Setting<?, ?>> settings(Events defaultValues) {
        return List.of(
                new BooleanSetting("Busfahrkarten limitieren", defaultValues.limitBusTickets),
                new IntSetting("Anzahl an Busfahrkarten", 0, 100, defaultValues.maxBusTickets),
                //new BooleanSetting("Gebäude limitieren", defaultValues.limitBuildings),
                new BooleanSetting("Tempowürfel", "Teil der Monopoly Mega-Edition", defaultValues.tempoDice),
                new BooleanSetting("Mr. Monopoly", "Wird Mr. Monopoly gewürfelt, bewegst du dich nach dem Wurf auf das nächste zu verkaufende Feld oder, falls es keins mehr gibt, dass mit der höchsten Miete", defaultValues.mrMonopoly),
                new BooleanSetting("Wolkenkratzer und Zugdepots", "Teil der Monopoly Mega-Edition", defaultValues.megaBuildings),
                new BooleanSetting("Teleport bei Dreierpasch", "Wenn du einen Dreierpasch würfelst, kannst du dich auf ein beliebiges Feld bewegen. Teil der Monopoly Mega-Edition", defaultValues.tripleTeleport),
                new IntSetting("Start-Geld", 0, 10000, defaultValues.startMoney),
                new IntSetting("Los-Geld", 0, 10000, defaultValues.losMoney),
                new BooleanSetting("Doppeltes Los-Geld", "Wenn du auf Los landest, anstatt es zu überqueren, bekommst du doppelt soviel Geld", defaultValues.doubleLosMoney),
                new BooleanSetting("Frei Parken", "Steuereinnahmen und Einnahmen aus Ereignis-/Gemeinschaftskarten landen auf dem Spielbrett. Wer auf Frei-Parken kommt, bekommt alles davon.", defaultValues.freeParking),
                new BooleanSetting("Miete im Gefängnis", defaultValues.rentInPrison),
                new BooleanSetting("Gleichmäßiges Bauen", "In einer Farbgruppe müssen alle Straßen auf dem gleichen Level (Anzahl Häuser/Hotel/Wolkenkratzer) sein. Es darf nur eine Abweichung von einem Level nach oben geben.", defaultValues.buildEquable),
                new BooleanSetting("Karten nach Ziehen mischen", "Wenn eine Ereignis-/Gemeinschaftskarte gezogen wird, wird diese sofort wieder untergemischt", defaultValues.reRollEventCardsAfterUse),
                new EnumSetting<>("Bauregel", "Legt fest, wo du Gebäude (Häuser/Hotels/Wolkenkratzer) bauen und abreißen kannst, wenn du am Zug bist", BuildRule.values(), defaultValues.buildRule),
                new EnumSetting<>("Karten für ein Haus", "Legt fest, wie viele Karten du von der jeweiligen Farbgruppe brauchst", OwnedCardsOfColorGroup.values(), defaultValues.requiredCards.cardsRequiredForOneHouse()),
                new EnumSetting<>("Karten für zwei Häuser", "Legt fest, wie viele Karten du von der jeweiligen Farbgruppe brauchst", OwnedCardsOfColorGroup.values(), defaultValues.requiredCards.cardsRequiredForTwoHouses()),
                new EnumSetting<>("Karten für drei Häuser", "Legt fest, wie viele Karten du von der jeweiligen Farbgruppe brauchst", OwnedCardsOfColorGroup.values(), defaultValues.requiredCards.cardsRequiredForThreeHouses()),
                new EnumSetting<>("Karten für vier Häuser", "Legt fest, wie viele Karten du von der jeweiligen Farbgruppe brauchst", OwnedCardsOfColorGroup.values(), defaultValues.requiredCards.cardsRequiredForFourHouses()),
                new EnumSetting<>("Karten für ein Hotel", "Legt fest, wie viele Karten du von der jeweiligen Farbgruppe brauchst", OwnedCardsOfColorGroup.values(), defaultValues.requiredCards.cardsRequiredForHotel()),
                new EnumSetting<>("Karten für einen Wolkenkratzer", "Legt fest, wie viele Karten du von der jeweiligen Farbgruppe brauchst", OwnedCardsOfColorGroup.values(), defaultValues.requiredCards.cardsRequiredForSkyscraper())
        );
    }

    private T events() {
        i = 0;
        return factory.create(
                getBoolean(),
                getInt(),
                true,
                getBoolean(),
                getBoolean(),
                getBoolean(),
                getBoolean(),
                getInt(),
                getInt(),
                getBoolean(),
                getBoolean(),
                getBoolean(),
                getBoolean(),
                getBoolean(),
                getEnum(),
                getEnum(),
                getEnum(),
                getEnum(),
                getEnum(),
                getEnum(),
                getEnum());
    }

    private boolean getBoolean() {
        i++;
        if(settingsList.get(i-1) instanceof BooleanSetting setting) {
            return setting.getValue();
        } else throw new IllegalStateException();
    }

    private <E extends Enum<E>> E getEnum() {
        i++;
        if(settingsList.get(i-1) instanceof EnumSetting<?> setting) {
            try {
                return (E) setting.getValue();
            } catch (ClassCastException e) {
                throw new IllegalStateException(e);
            }
        } else throw new IllegalStateException();
    }

    private int getInt() {
        i++;
        if(settingsList.get(i-1) instanceof IntSetting setting) {
            return setting.getValue();
        } else throw new IllegalStateException();
    }
}
