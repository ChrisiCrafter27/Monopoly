package monopol.client.screen;

import monopol.common.utils.JUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class InfoPane extends JLayeredPane {
    private final List<String> texts = new ArrayList<>();

    private final JLabel text1 = JUtils.addText("", 0, 0, 350, 15, SwingConstants.LEFT);
    private final JLabel text2 = JUtils.addText("", 0, 20, 350, 15, SwingConstants.LEFT);
    private final JLabel text3 = JUtils.addText("", 0, 40, 350, 15, SwingConstants.LEFT);
    private final JLabel text4 = JUtils.addText("", 0, 60, 350, 15, SwingConstants.LEFT);
    private final JLabel text5 = JUtils.addText("", 0, 80, 350, 15, SwingConstants.LEFT);

    public InfoPane() {
        super();
        setBounds(1080/2 - 175, 400, 300, 100);
        add(text1);
        add(text2);
        add(text3);
        add(text4);
        add(text5);
        reset();
    }

    public void init() {
        setVisible(true);
    }

    public void reset() {
        setVisible(false);
        texts.clear();
    }

    public void show(String text) {
        texts.add(text);
        if(texts.size() > 5) texts.remove(0);
        if(texts.size() > 0) text1.setText(texts.get(0));
        else text1.setText("");
        if(texts.size() > 1) text2.setText(texts.get(1));
        else text2.setText("");
        if(texts.size() > 2) text3.setText(texts.get(2));
        else text3.setText("");
        if(texts.size() > 3) text4.setText(texts.get(3));
        else text4.setText("");
        if(texts.size() > 4) text5.setText(texts.get(4));
        else text5.setText("");
    }
}
