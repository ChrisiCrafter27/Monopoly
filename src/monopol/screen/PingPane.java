package monopol.screen;

import monopol.utils.JUtils;
import monopol.utils.KeyHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class PingPane extends JLayeredPane {
    private long oldPing;

    private final JLabel label = addText("", (1920/2)-250, 70, 500, 30, true);

    public PingPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(label, DEFAULT_LAYER);
        reset();
    }

    public void reset() {
        oldPing = -1;
        setVisible(false);
    }

    public void update(long ping, KeyHandler keyHandler) {
        Color color;
        if(ping < 0) color = Color.BLUE;
        else if(ping < 250) color = Color.GREEN;
        else if(ping < 500) color = Color.YELLOW;
        else if(ping < 1000) color = Color.ORANGE;
        else color = Color.RED;
        label.setForeground(color);
        if(ping >= 0) label.setText("Ping: " + ping + "ms"); else label.setText("Ping: ?");
        setVisible(keyHandler.isKeyDown(KeyEvent.VK_TAB));
        if(oldPing != ping) repaint();
        oldPing = ping;
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

}
