package monopol.client.screen;

import monopol.common.data.IPurchasable;
import monopol.common.data.Plant;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;

public class SelectedCardPane extends JLayeredPane {
    private final JLabel color = JUtils.addImage("", 5, 5, 334, 60);
    private final JLabel rahmen = JUtils.addImage("images/Main_pictures/rahmensuppe.png", 5, 5, 334, 60);
    private final JLabel name = JUtils.addText("", 5, 21, 334, 26, SwingConstants.CENTER);
    private final JLabel textKey0 = JUtils.addText("", 5, 65, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey1 = JUtils.addText("", 5, 90, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey2 = JUtils.addText("", 5, 115, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey3 = JUtils.addText("", 5, 140, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey4 = JUtils.addText("", 5, 165, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey5 = JUtils.addText("", 5, 190, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey6 = JUtils.addText("", 5, 215, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey7 = JUtils.addText("", 5, 240, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey8 = JUtils.addText("", 5, 260, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey9 = JUtils.addText("", 5, 280, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey10 = JUtils.addText("", 5, 300, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey11 = JUtils.addText("", 5, 320, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey12 = JUtils.addText("", 5, 340, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey13 = JUtils.addText("", 5, 360, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey14 = JUtils.addText("", 5, 380, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey15 = JUtils.addText("", 5, 400, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey16 = JUtils.addText("", 5, 420, 334, 15, SwingConstants.CENTER);
    private final JLabel textKey17 = JUtils.addText("", 5, 430, 334, 20, SwingConstants.LEFT);
    private final JLabel textKey18 = JUtils.addText("", 5, 465, 334, 15, SwingConstants.CENTER);
    private final JLabel textValue0 = JUtils.addText("", 5, 65, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue1 = JUtils.addText("", 5, 90, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue2 = JUtils.addText("", 5, 115, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue3 = JUtils.addText("", 5, 140, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue4 = JUtils.addText("", 5, 165, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue5 = JUtils.addText("", 5, 190, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue6 = JUtils.addText("", 5, 215, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue7 = JUtils.addText("", 5, 240, 334, 20, SwingConstants.RIGHT);
    private final JLabel textValue17 = JUtils.addText("", 5, 430, 334, 20, SwingConstants.RIGHT);

    private IPurchasable purchasable;

    public SelectedCardPane() {
        super();
        setBounds(JUtils.getX(JUtils.SCREEN_HEIGHT + 530 - 60), JUtils.getY(561), JUtils.getX(344), JUtils.getY(485));
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
        update();
        setVisible(true);
    }

    private void update() {
        if(purchasable == null) throw new IllegalStateException("init() was not called");
        if(purchasable instanceof Street street) color.setIcon(new ImageIcon(new ImageIcon("images/felder/" + street.colorGroup.image + "_cardcolor.png").getImage().getScaledInstance(334, 60, Image.SCALE_SMOOTH)));
        else color.setIcon(new ImageIcon(""));
        name.setText(purchasable.getName().toUpperCase());
        textKey0.setText(purchasable.keyText(0));
        textKey1.setText(purchasable.keyText(1));
        textKey2.setText(purchasable.keyText(2));
        textKey3.setText(purchasable.keyText(3));
        textKey4.setText(purchasable.keyText(4));
        textKey5.setText(purchasable.keyText(5));
        textKey6.setText(purchasable.keyText(6));
        textKey7.setText(purchasable.keyText(7));
        textKey8.setText(purchasable.keyText(8));
        textKey9.setText(purchasable.keyText(9));
        textKey10.setText(purchasable.keyText(10));
        textKey11.setText(purchasable.keyText(11));
        textKey12.setText(purchasable.keyText(12));
        textKey13.setText(purchasable.keyText(13));
        textKey14.setText(purchasable.keyText(14));
        textKey15.setText(purchasable.keyText(15));
        textKey16.setText(purchasable.keyText(16));
        textKey17.setText(purchasable.keyText(17));
        textKey18.setText(purchasable.keyText(18));
        textValue0.setText(purchasable.valueText(0));
        textValue1.setText(purchasable.valueText(1));
        textValue2.setText(purchasable.valueText(2));
        textValue3.setText(purchasable.valueText(3));
        textValue4.setText(purchasable.valueText(4));
        textValue5.setText(purchasable.valueText(5));
        textValue6.setText(purchasable.valueText(6));
        textValue7.setText(purchasable.valueText(7));
        textValue17.setText(purchasable.valueText(17));
        for (Component component : getComponents()) {
            if (component.getForeground() == Color.GREEN) component.setForeground(Color.BLACK);
        }
        if (purchasable instanceof Street street) {
            if(street.getOwner() == null || street.getOwner().isEmpty()) {
                textKey0.setForeground(Color.GREEN);
                textValue0.setForeground(Color.GREEN);
            } else {
                switch (street.getLevel()) {
                    case 0 -> {
                        textKey1.setForeground(Color.GREEN);
                        textValue1.setForeground(Color.GREEN);
                    }
                    case 1 -> {
                        textKey2.setForeground(Color.GREEN);
                        textValue2.setForeground(Color.GREEN);
                    }
                    case 2 -> {
                        textKey3.setForeground(Color.GREEN);
                        textValue3.setForeground(Color.GREEN);
                    }
                    case 3 -> {
                        textKey4.setForeground(Color.GREEN);
                        textValue4.setForeground(Color.GREEN);
                    }
                    case 4 -> {
                        textKey5.setForeground(Color.GREEN);
                        textValue5.setForeground(Color.GREEN);
                    }
                    case 5 -> {
                        textKey6.setForeground(Color.GREEN);
                        textValue6.setForeground(Color.GREEN);
                    }
                    case 6 -> {
                        textKey7.setForeground(Color.GREEN);
                        textValue7.setForeground(Color.GREEN);
                    }
                }
            }
        } else if (purchasable instanceof TrainStation trainStation) {
            if(trainStation.getOwner() == null || trainStation.getOwner().isEmpty()) {
                textKey1.setForeground(Color.GREEN);
                textValue1.setForeground(Color.GREEN);
            } else {
                switch (trainStation.getRent(1)) {
                    case 25 -> {
                        textKey3.setForeground(Color.GREEN);
                        textValue3.setForeground(Color.GREEN);
                    }
                    case 50 -> {
                        if (trainStation.getLevel() == 1) {
                            textKey3.setForeground(Color.GREEN);
                            textValue3.setForeground(Color.GREEN);
                        } else {
                            textKey4.setForeground(Color.GREEN);
                            textValue4.setForeground(Color.GREEN);
                        }
                    }
                    case 100 -> {
                        if (trainStation.getLevel() == 1) {
                            textKey4.setForeground(Color.GREEN);
                            textValue4.setForeground(Color.GREEN);
                        } else {
                            textKey5.setForeground(Color.GREEN);
                            textValue5.setForeground(Color.GREEN);
                        }
                    }
                    case 200 -> {
                        if (trainStation.getLevel() == 1) {
                            textKey5.setForeground(Color.GREEN);
                            textValue5.setForeground(Color.GREEN);
                        } else {
                            textKey6.setForeground(Color.GREEN);
                            textValue6.setForeground(Color.GREEN);
                        }
                    }
                    case 400 -> {
                        textKey6.setForeground(Color.GREEN);
                        textValue6.setForeground(Color.GREEN);
                    }
                }
            }
        } else if (purchasable instanceof Plant plant) {
            if(plant.getOwner() == null || plant.getOwner().isEmpty()) {
                textKey1.setForeground(Color.GREEN);
                textValue1.setForeground(Color.GREEN);
            } else {
                switch (plant.getRent(1)) {
                    case 4 -> {
                        textKey3.setForeground(Color.GREEN);
                        textValue3.setForeground(Color.GREEN);
                    }
                    case 10 -> {
                        textKey4.setForeground(Color.GREEN);
                        textValue4.setForeground(Color.GREEN);
                    }
                    case 20 -> {
                        textKey5.setForeground(Color.GREEN);
                        textValue5.setForeground(Color.GREEN);
                    }
                }
            }
        }
    }

    public void reset() {
        setVisible(false);
    }
}
