package monopol.client.screen;

import monopol.common.utils.JUtils;
import monopol.common.utils.KeyHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class PingPane extends JLayeredPane {
    private boolean keyDown = false;

    private final JLabel label = JUtils.addText("", (1920/2)-250, 70, 500, 30, true);

    public PingPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(label, DEFAULT_LAYER);
        reset();
    }

    public void reset() {
        keyDown = false;
        setVisible(false);
    }

    public void update(long ping, KeyHandler keyHandler, Component root, Runnable kick) {
        Color color;
        if(ping < 0) color = Color.BLUE;
        else if(ping < 250) color = Color.GREEN;
        else if(ping < 500) color = Color.YELLOW;
        else if(ping < 1000) color = Color.ORANGE;
        else color = Color.RED;
        label.setForeground(color);
        if(ping >= 0) label.setText("Ping: " + ping + "ms"); else label.setText("Ping: ?");
        setVisible(keyHandler.isKeyDown(KeyEvent.VK_TAB));

        if (keyDown && !keyHandler.isKeyDown(KeyEvent.VK_ESCAPE)) {
            if(JOptionPane.showConfirmDialog(root, "Möchtest du den Server wirklich verlassen?", "Server verlassen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) kick.run();
        }
        keyDown = keyHandler.isKeyDown(KeyEvent.VK_ESCAPE);
    }
}
