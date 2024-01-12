package monopol.screen;

import monopol.data.Street;
import monopol.utils.JUtils;

import javax.swing.*;

public class PlayerDisplayPane extends JLayeredPane {
    private final JLabel icon = JUtils.addImage("images/felder/" + Street.BADSTRASSE.colorGroup.image + "_cardcolor.png", 0, 0);
    private int pos = 0;

    public PlayerDisplayPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(icon);
        reset();
    }

    public void show(int pos) {
        this.pos = pos;
        icon.setBounds(JUtils.getX(x()), JUtils.getY(y()), icon.getWidth(), icon.getHeight());
        setVisible(true);
    }

    public void reset() {
        setVisible(false);
    }

    private int x() {
        if (pos < 14)
            return 35;
        else if(pos < 27)
            return 35 + 70 * (pos - 14);
        else if(pos < 40)
            return 35 + 70 * 13;
        else return 35 + 70 * (13 - (pos - 40));
    }

    private int y() {
        if (pos < 14)
            return 105 + 70 * (13 - (pos - 14));
        else if(pos < 27)
            return 105;
        else if(pos < 40)
            return 105 + 70 * (pos - 40);
        else return 105 + 70 * 13;
    }
}
