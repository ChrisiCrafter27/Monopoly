package monopol.utils;

import javax.swing.*;
import java.awt.*;

public class RotateJLabel extends JLabel {
    private final int rotation;
    public RotateJLabel(String text, int rotation) {
        super(text);
        this.rotation = rotation;
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gx = (Graphics2D) g;
        gx.rotate(Math.toRadians(rotation), getX() + getWidth()/2f, getY() + getHeight()/2f);
        super.paintComponent(g);
    }
}
