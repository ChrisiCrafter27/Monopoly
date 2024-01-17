package monopol.client.screen;

import monopol.common.utils.JUtils;
import monopol.common.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerDisplayPane extends JLayeredPane {
    private final Map<String, Pair<JButton, Integer>> players = new HashMap<>();

    public PlayerDisplayPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        reset();
    }

    public void init(Map<String, Color> map) {
        for (Map.Entry<String, Color> entry : map.entrySet()) {
            String name = entry.getKey();
            players.put(name, new Pair<>(playerButton(entry.getValue()), 0));
            setPos(name, 0);
        }
        setVisible(true);
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
                players.put(name, new Pair<>(playerButton(color), 0));
                setPos(name, 0);
                i++;
            }
        }
    }

    private JButton playerButton(Color color) {
        JButton button = JUtils.addButton("", 0, 0, 0, 0, true, actionEvent ->  {
            System.out.println("Spectating Player1");
        });
        button.setBackground(color);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setIcon(null);
        add(button);
        return button;
    }

    public void setPos(String name, int pos) {
        if (players.containsKey(name)) {
            Pair<JButton, Integer> pair = players.get(name);
            int oldPos = pair.getRight();
            pair.setRight(pos);
            List<JButton> buttons1 = playersOn(pos);
            for (int i = 0; i < buttons1.size(); i++) {
                JButton button = buttons1.get(i);
                button.setBounds(JUtils.getX(x(pos, buttons1.size(), i)-10), JUtils.getY(y(pos, buttons1.size(), i)-10), 20, 20);
            }
            List<JButton> buttons2 = playersOn(oldPos);
            for (int i = 0; i < buttons2.size(); i++) {
                JButton button = buttons2.get(i);
                button.setBounds(JUtils.getX(x(oldPos, buttons2.size(), i)-10), JUtils.getY(y(oldPos, buttons2.size(), i)-10), 20, 20);
            }
        }
    }

    public int getPos(String name) {
        return players.get(name).getRight();
    }

    private List<JButton> playersOn(int pos) {
        return players.values().stream().filter(pair -> pair.getRight() == pos).map(Pair::getLeft).toList();
    }

    public void reset() {
        removeAll();
        setVisible(false);
    }

    private int x(int pos, int players, int i) {
        return simpleX(pos) + switch (players) {
            case 2, 4, 6 -> switch (i) {
                case 0, 2, 4 -> -15;
                default -> 15;
            };
            case 3 -> switch (i) {
                case 0 -> -15;
                case 1 -> 15;
                default -> 0;
            };
            case 5 -> switch (i) {
                case 0, 2 -> -15;
                case 1, 3 -> 15;
                default -> 0;
            };
            default -> 0;
        };
    }

    private int y(int pos, int players, int i) {
        return simpleY(pos) + switch (players) {
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
        };
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
