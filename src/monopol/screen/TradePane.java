package monopol.screen;

import monopol.utils.JUtils;

import javax.swing.*;

public class TradePane extends JLayeredPane {

    public TradePane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        //add(label, DEFAULT_LAYER);
        reset();
    }

    public void reset() {
        setVisible(false);
    }

}
