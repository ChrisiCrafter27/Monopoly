package monopol.screen;

import monopol.data.Street;
import monopol.utils.JUtils;

import javax.swing.*;
import java.awt.*;

public class SelectedCardPane extends JLayeredPane {
    private final JLabel color = JUtils.addImage("images/felder/" + Street.BADSTRASSE.colorGroup.image + "_cardcolor.png", 5, 5, 334, 60);
    private final JLabel rahmen = JUtils.addImage("images/Main_pictures/rahmensuppe.png", 5, 5, 334, 60);
    private final JLabel name = JUtils.addText(Street.BADSTRASSE.name(), 5, 21, 334, 26, true);
    private final JLabel rent1Key = JUtils.addText(, 5, 21, 334, 26, true);

    public SelectedCardPane() {
        super();
        setBounds((int) (JUtils.SCREEN_HEIGHT + 530 - 60), 561, 344, 485);
        setVisible(false);

        add(color, PALETTE_LAYER);
        name.setForeground(Color.BLACK);
        add(name, POPUP_LAYER);
        add(rahmen, MODAL_LAYER);

        setVisible(true);
        repaint();
    }
}
