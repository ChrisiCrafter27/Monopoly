package monopol.screen;

import monopol.data.IPurchasable;
import monopol.data.Street;
import monopol.utils.JUtils;

import javax.swing.*;

public class SelectedCardPane extends JLayeredPane {
    private final JLabel color = JUtils.addImage("images/felder/" + Street.BADSTRASSE.colorGroup.image + "_cardcolor.png", 5, 5, 334, 60);
    private final JLabel rahmen = JUtils.addImage("images/Main_pictures/rahmensuppe.png", 5, 5, 334, 60);
    private final JLabel name = JUtils.addText(Street.BADSTRASSE.name(), 5, 21, 334, 26, SwingConstants.CENTER);
    private final JLabel textKey0 = JUtils.addText(Street.BADSTRASSE.keyText(0), 5, 65, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey1 = JUtils.addText(Street.BADSTRASSE.keyText(1), 5, 90, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey2 = JUtils.addText(Street.BADSTRASSE.keyText(2), 5, 115, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey3 = JUtils.addText(Street.BADSTRASSE.keyText(3), 5, 140, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey4 = JUtils.addText(Street.BADSTRASSE.keyText(4), 5, 165, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey5 = JUtils.addText(Street.BADSTRASSE.keyText(5), 5, 190, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey6 = JUtils.addText(Street.BADSTRASSE.keyText(6), 5, 215, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey7 = JUtils.addText(Street.BADSTRASSE.keyText(7), 5, 240, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey8 = JUtils.addText(Street.BADSTRASSE.keyText(8), 5, 265, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey9 = JUtils.addText(Street.BADSTRASSE.keyText(9), 5, 280, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey10 = JUtils.addText(Street.BADSTRASSE.keyText(10), 5, 300, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey11 = JUtils.addText(Street.BADSTRASSE.keyText(11), 5, 320, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey12 = JUtils.addText(Street.BADSTRASSE.keyText(12), 5, 340, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey13 = JUtils.addText(Street.BADSTRASSE.keyText(13), 5, 360, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey14 = JUtils.addText(Street.BADSTRASSE.keyText(14), 5, 380, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey15 = JUtils.addText(Street.BADSTRASSE.keyText(15), 5, 400, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey16 = JUtils.addText(Street.BADSTRASSE.keyText(16), 5, 420, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey17 = JUtils.addText(Street.BADSTRASSE.keyText(17), 5, 430, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey18 = JUtils.addText(Street.BADSTRASSE.keyText(18), 5, 465, 334, 15, SwingConstants.CENTER);
    private final JLabel textValue0 = JUtils.addText(Street.BADSTRASSE.valueText(0), 5, 65, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue1 = JUtils.addText(Street.BADSTRASSE.valueText(1), 5, 90, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue2 = JUtils.addText(Street.BADSTRASSE.valueText(2), 5, 115, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue3 = JUtils.addText(Street.BADSTRASSE.valueText(3), 5, 140, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue4 = JUtils.addText(Street.BADSTRASSE.valueText(4), 5, 165, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue5 = JUtils.addText(Street.BADSTRASSE.valueText(5), 5, 190, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue6 = JUtils.addText(Street.BADSTRASSE.valueText(6), 5, 215, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue7 = JUtils.addText(Street.BADSTRASSE.valueText(7), 5, 240, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue17 = JUtils.addText(Street.BADSTRASSE.valueText(17), 5, 430, 334, 20, SwingConstants.RIGHT);

    private IPurchasable purchasable;

    public SelectedCardPane() {
        super();
        setBounds((int) (JUtils.SCREEN_HEIGHT + 530 - 60), 561, 344, 485);
        setVisible(false);

        add(color, PALETTE_LAYER);
        add(name, POPUP_LAYER);
        add(rahmen, MODAL_LAYER);
        add(textKey0, POPUP_LAYER);
        add(textKey1, POPUP_LAYER);
        add(textKey2, POPUP_LAYER);
        add(textKey3, POPUP_LAYER);
        add(textKey4, POPUP_LAYER);
        add(textKey5, POPUP_LAYER);
        add(textKey6, POPUP_LAYER);
        add(textKey7, POPUP_LAYER);
        add(textKey8, POPUP_LAYER);
        add(textKey9, POPUP_LAYER);
        add(textKey10, POPUP_LAYER);
        add(textKey11, POPUP_LAYER);
        add(textKey12, POPUP_LAYER);
        add(textKey13, POPUP_LAYER);
        add(textKey14, POPUP_LAYER);
        add(textKey15, POPUP_LAYER);
        add(textKey16, POPUP_LAYER);
        add(textKey17, POPUP_LAYER);
        add(textKey18, POPUP_LAYER);
        add(textValue0, POPUP_LAYER);
        add(textValue1, POPUP_LAYER);
        add(textValue2, POPUP_LAYER);
        add(textValue3, POPUP_LAYER);
        add(textValue4, POPUP_LAYER);
        add(textValue5, POPUP_LAYER);
        add(textValue6, POPUP_LAYER);
        add(textValue7, POPUP_LAYER);
        add(textValue17, POPUP_LAYER);

        repaint();
    }

    public void init(IPurchasable purchasable) {
        this.purchasable = purchasable;
        updateTexts();
        setVisible(true);
    }

    private void updateTexts() {
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey1.setText(Street.BADSTRASSE.keyText(0));
        textKey2.setText(Street.BADSTRASSE.keyText(0));
        textKey3.setText(Street.BADSTRASSE.keyText(0));
        textKey4.setText(Street.BADSTRASSE.keyText(0));
        textKey5.setText(Street.BADSTRASSE.keyText(0));
        textKey6.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
        textKey0.setText(Street.BADSTRASSE.keyText(0));
    }

    public void reset() {
        setVisible(false);
    }
}
