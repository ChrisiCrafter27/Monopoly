package monopol.client.screen;

import monopol.client.Client;
import monopol.common.data.Field;
import monopol.common.data.IField;
import monopol.common.data.IPurchasable;
import monopol.common.data.Player;
import monopol.common.utils.JUtils;
import monopol.common.utils.MapUtils;
import monopol.common.utils.Triplet;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlayerDisplayPane extends JLayeredPane {
    private final Map<String, Triplet<JButton, Integer, Integer>> players = new HashMap<>();
    private Thread animThread = new Thread(() -> {});
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};
    private Supplier<RootPane> displaySup = () -> {throw new IllegalStateException("init() was not called");};

    public PlayerDisplayPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        reset();
    }

    public void init(Supplier<Client> clientSup, Supplier<RootPane> displaySup) {
        setVisible(true);
        this.clientSup = clientSup;
        this.displaySup = displaySup;
        animThread.interrupt();
        animThread = new Thread(() -> {
            while(!Thread.interrupted()) {
                for(String name : players.keySet()) {
                    Triplet<JButton, Integer, Integer> triplet = players.get(name);
                    if(!triplet.getMiddle().equals(triplet.getRight())) {
                        int oldPos = triplet.getRight();
                        int pos = oldPos;
                        pos++;
                        if(pos >= 52) pos = 0;
                        triplet.setRight(pos);
                        Map<String, Triplet<JButton, Integer, Integer>> map1 = playersOn(pos);
                        List<Triplet<JButton, Integer, Integer>> list11 = map1.values().stream().filter(v -> !inPrison(MapUtils.key(map1, v))).toList();
                        List<Triplet<JButton, Integer, Integer>> list12 = map1.values().stream().filter(v -> inPrison(MapUtils.key(map1, v))).toList();
                        for (int i = 0; i < list11.size(); i++) {
                            JButton button = list11.get(i).getLeft();
                            if(Field.fields().indexOf(Field.GEFAENGNIS) == list11.get(i).getRight()) {
                                button.setBounds(JUtils.getX(prisonX(pos, list11.size(), i, false)-10), JUtils.getY(prisonY(pos, list11.size(), i, false)-10), 20, 20);
                            } else button.setBounds(JUtils.getX(x(pos, list11.size(), i)-10), JUtils.getY(y(pos, list11.size(), i)-10), 20, 20);
                        }
                        for (int i = 0; i < list12.size(); i++) {
                            JButton button = list12.get(i).getLeft();
                            if(Field.fields().indexOf(Field.GEFAENGNIS) == list12.get(i).getRight()) {
                                button.setBounds(JUtils.getX(prisonX(pos, list12.size(), i, true)-10), JUtils.getY(prisonY(pos, list12.size(), i, true)-10), 20, 20);
                            } else button.setBounds(JUtils.getX(x(pos, list12.size(), i)-10), JUtils.getY(y(pos, list12.size(), i)-10), 20, 20);
                        }
                        Map<String, Triplet<JButton, Integer, Integer>> map2 = playersOn(oldPos);
                        List<Triplet<JButton, Integer, Integer>> list21 = map2.values().stream().filter(v -> !inPrison(MapUtils.key(map2, v))).toList();
                        List<Triplet<JButton, Integer, Integer>> list22 = map2.values().stream().filter(v -> inPrison(MapUtils.key(map2, v))).toList();
                        for (int i = 0; i < list21.size(); i++) {
                            JButton button = list21.get(i).getLeft();
                            if(Field.fields().indexOf(Field.GEFAENGNIS) == list21.get(i).getRight()) {
                                button.setBounds(JUtils.getX(prisonX(pos, list21.size(), i, false)-10), JUtils.getY(prisonY(pos, list21.size(), i, false)-10), 20, 20);
                            } else button.setBounds(JUtils.getX(x(pos, list21.size(), i)-10), JUtils.getY(y(pos, list21.size(), i)-10), 20, 20);
                        }
                        for (int i = 0; i < list22.size(); i++) {
                            JButton button = list22.get(i).getLeft();
                            if(Field.fields().indexOf(Field.GEFAENGNIS) == list22.get(i).getRight()) {
                                button.setBounds(JUtils.getX(prisonX(pos, list22.size(), i, true)-10), JUtils.getY(prisonY(pos, list22.size(), i, true)-10), 20, 20);
                            } else button.setBounds(JUtils.getX(x(pos, list22.size(), i)-10), JUtils.getY(y(pos, list22.size(), i)-10), 20, 20);
                        }
                        List<IField> list = Field.fields();
                        if(pos == triplet.getMiddle() && list.size() > pos && list.get(pos) instanceof IPurchasable purchasable) displaySup.get().selectedCardPane.select(purchasable);
                    }
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        animThread.start();
    }

    private boolean inPrison(String name) {
        try {
            Player player = clientSup.get().serverMethod().getPlayer(name);
            return player.inPrison() && players.get(name).getRight() == Field.fields().indexOf(Field.GEFAENGNIS);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void check(Set<String> names) {
        if(!players.keySet().containsAll(names) || !names.containsAll(players.keySet())) {
            players.clear();
            removeAll();
            int i = 0;
            for(String name : names) {
                Color color = switch (i) {
                    case 0 -> Color.YELLOW;
                    case 1 -> Color.RED;
                    case 2 -> Color.BLUE;
                    case 3 -> Color.GREEN;
                    case 4 -> Color.ORANGE;
                    case 5 -> Color.MAGENTA;
                    default -> Color.WHITE;
                };
                players.put(name, new Triplet<>(playerButton(color, name), 0, 0));
                setPos(name, 0, Color.WHITE);
                i++;
            }
        }
    }

    private JButton playerButton(Color color, String name) {
        JButton button = JUtils.addButton("", 0, 0, 0, 0, true, actionEvent ->  {
            displaySup.get().playerInfoPane.setCurrentAndUpdate(name);
        });
        button.setBackground(color);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setIcon(null);
        add(button);
        return button;
    }

    public synchronized void setPosWithAnim(String name, int pos, Color color) {
        if (players.containsKey(name)) {
            Triplet<JButton, Integer, Integer> triplet = players.get(name);
            if(color != null) triplet.getLeft().setBackground(color);
            triplet.setMiddle(pos);
        }
    }

    public synchronized void setPos(String name, int pos, Color color) {
        if (players.containsKey(name)) {
            Triplet<JButton, Integer, Integer> triplet = players.get(name);
            int oldPos = triplet.getRight();
            triplet.setMiddle(pos);
            triplet.setRight(pos);
            if(color != null) triplet.getLeft().setBackground(color);
            Map<String, Triplet<JButton, Integer, Integer>> map1 = playersOn(pos);
            List<Triplet<JButton, Integer, Integer>> list11 = map1.values().stream().filter(v -> !inPrison(MapUtils.key(map1, v))).toList();
            List<Triplet<JButton, Integer, Integer>> list12 = map1.values().stream().filter(v -> inPrison(MapUtils.key(map1, v))).toList();
            for (int i = 0; i < list11.size(); i++) {
                JButton button = list11.get(i).getLeft();
                if(Field.fields().indexOf(Field.GEFAENGNIS) == list11.get(i).getRight()) {
                    button.setBounds(JUtils.getX(prisonX(pos, list11.size(), i, false)-10), JUtils.getY(prisonY(pos, list11.size(), i, false)-10), 20, 20);
                } else button.setBounds(JUtils.getX(x(pos, list11.size(), i)-10), JUtils.getY(y(pos, list11.size(), i)-10), 20, 20);
            }
            for (int i = 0; i < list12.size(); i++) {
                JButton button = list12.get(i).getLeft();
                if(Field.fields().indexOf(Field.GEFAENGNIS) == list12.get(i).getRight()) {
                    button.setBounds(JUtils.getX(prisonX(pos, list12.size(), i, true)-10), JUtils.getY(prisonY(pos, list12.size(), i, true)-10), 20, 20);
                } else button.setBounds(JUtils.getX(x(pos, list12.size(), i)-10), JUtils.getY(y(pos, list12.size(), i)-10), 20, 20);
            }
            Map<String, Triplet<JButton, Integer, Integer>> map2 = playersOn(oldPos);
            List<Triplet<JButton, Integer, Integer>> list21 = map2.values().stream().filter(v -> !inPrison(MapUtils.key(map2, v))).toList();
            List<Triplet<JButton, Integer, Integer>> list22 = map2.values().stream().filter(v -> inPrison(MapUtils.key(map2, v))).toList();
            for (int i = 0; i < list21.size(); i++) {
                JButton button = list21.get(i).getLeft();
                if(Field.fields().indexOf(Field.GEFAENGNIS) == list21.get(i).getRight()) {
                    button.setBounds(JUtils.getX(prisonX(pos, list21.size(), i, false)-10), JUtils.getY(prisonY(pos, list21.size(), i, false)-10), 20, 20);
                } else button.setBounds(JUtils.getX(x(pos, list21.size(), i)-10), JUtils.getY(y(pos, list21.size(), i)-10), 20, 20);
            }
            for (int i = 0; i < list22.size(); i++) {
                JButton button = list22.get(i).getLeft();
                if(Field.fields().indexOf(Field.GEFAENGNIS) == list22.get(i).getRight()) {
                    button.setBounds(JUtils.getX(prisonX(pos, list22.size(), i, true)-10), JUtils.getY(prisonY(pos, list22.size(), i, true)-10), 20, 20);
                } else button.setBounds(JUtils.getX(x(pos, list22.size(), i)-10), JUtils.getY(y(pos, list22.size(), i)-10), 20, 20);
            }
            List<IField> list = Field.fields();
            if(list.size() > pos && list.get(pos) instanceof IPurchasable purchasable) displaySup.get().selectedCardPane.select(purchasable);
        }
    }

    private Map<String, Triplet<JButton, Integer, Integer>> playersOn(int pos) {
        return players.entrySet().stream().filter(entry -> entry.getValue().getRight() == pos).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void reset() {
        removeAll();
        setVisible(false);
        animThread.interrupt();
    }

    private int prisonX(int pos, int players, int i, boolean inPrison) {
        return simpleX(pos) + (inPrison ? switch (players) {
            case 1, 3, 4 -> switch (i) {
                case 0, 1 -> 0;
                default -> 30;
            };
            case 2 -> switch (i) {
                case 0 -> 0;
                case 1 -> 30;
                default -> -30;
            };
            default -> 0;
        } : switch (players) {
            case 1, 3, 5 -> switch (i) {
                case 0, 1, 3 -> -30;
                case 2 -> 0;
                default -> 30;
            };
            case 2, 4 -> switch (i) {
                case 0, 2 -> -30;
                case 1 -> 0;
                default -> 30;
            };
            default -> 0;
        });
    }

    private int prisonY(int pos, int players, int i, boolean inPrison) {
        return simpleY(pos) + (inPrison ? switch (players) {
            case 1, 3, 4 -> switch (i) {
                case 0, 2 -> 0;
                default -> 30;
            };
            case 2 -> switch (i) {
                case 0 -> 30;
                case 1 -> 0;
                default -> -30;
            };
            default -> 0;
        } : switch (players) {
            case 1, 3, 5 -> switch (i) {
                case 0, 2, 4 -> -30;
                case 1 -> 0;
                default -> 30;
            };
            case 2, 4 -> switch (i) {
                case 1, 3 -> -30;
                case 0 -> 0;
                default -> 30;
            };
            default -> 0;
        });
    }

    private boolean otherSide(int pos) {
        return (pos < Field.fields().indexOf(Field.GEFAENGNIS) && pos > Field.fields().indexOf(Field.LOS)) || (pos < Field.fields().indexOf(Field.INSGEFAENGNIS) && pos > Field.fields().indexOf(Field.FREIPARKEN));
    }

    private int x(int pos, int players, int i) {
        return simpleX(pos) + (otherSide(pos) ? switch (players) {
            case 1, 2 -> 0;
            case 3, 4 -> switch (i) {
                case 0, 1 -> -15;
                default -> 15;
            };
            default -> switch (i) {
                case 0, 1 -> -30;
                case 2, 3 -> -0;
                default -> 30;
            };
        } : switch (players) {
            case 2, 4, 6 -> switch (i) {
                case 0, 2, 4 -> -15;
                default -> 15;
            };
            case 3 -> switch (i) {
                case 0 -> -15;
                case 1 -> 15;
                default -> 0;
            };
            default -> switch (i) {
                case 0, 2 -> -15;
                case 1, 3 -> 15;
                default -> 0;
            };
        });
    }

    private int y(int pos, int players, int i) {
        return simpleY(pos) + (otherSide(pos) ? switch (players) {
            case 2, 4, 6 -> switch (i) {
                case 0, 2, 4 -> -15;
                default -> 15;
            };
            case 3 -> switch (i) {
                case 0 -> -15;
                case 1 -> 15;
                default -> 0;
            };
            default -> switch (i) {
                case 0, 2 -> -15;
                case 1, 3 -> 15;
                default -> 0;
            };
        } : switch (players) {
            case 1, 2 -> 0;
            case 3, 4 -> switch (i) {
                case 0, 1 -> -15;
                default -> 15;
            };
            default -> switch (i) {
                case 0, 1 -> -30;
                case 2, 3 -> -0;
                default -> 30;
            };
        });
    }

    private int simpleX(int pos) {
        if (pos < 14)
            return 45;
        else if(pos < 26)
            return 125 + 70 * (pos - 14);
        else if(pos < 40)
            return 135 + 70 * 12;
        else return 125 + 70 * (12 - (pos - 39));
    }

    private int simpleY(int pos) {
        if (pos < 1)
            return 195 + 70 * 12;
        else if (pos < 13)
            return 185 + 70 * (12 - pos);
        else if(pos < 27)
            return 105;
        else if(pos < 39)
            return 185 + 70 * (pos - 27);
        else return 195 + 70 * 12;
    }
}
