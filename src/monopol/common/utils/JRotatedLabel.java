package monopol.common.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class JRotatedLabel extends JLabel {
    private final double rotation;
    private int rotX;
    private int rotY;
    private boolean useRotXY = false;

    public JRotatedLabel(String text, int size, int font, double rotation, int x, int y, int maxLength) {
        super(text);
        this.rotation = rotation;
        Font newFont = new Font("Arial", font, size);
        setFont(newFont);
        FontMetrics metrics = new FontMetrics(newFont) {
        };
        Rectangle2D bounds = metrics.getStringBounds(text, null);
        boolean modified = false;
        while(bounds.getWidth() > maxLength && maxLength > 0 && text.length() > 1) {
            text = text.substring(0, text.length() - 1);
            bounds = metrics.getStringBounds(text, null);
            modified = true;
        }
        if(modified) {
            text += ".";
            bounds = metrics.getStringBounds(text, null);
            setText(text);
        }
        setBounds((int) (x + (maxLength- bounds.getWidth()) / 2), (int) (y + (maxLength- bounds.getWidth()) / 2), (int) bounds.getWidth(), (int) bounds.getWidth());
        rotX = (int) (getWidth() / 2f);
        rotY = (int) (getHeight() / 2f);
    }

    public JRotatedLabel(ImageIcon icon, double rotation, int x, int y, int maxLength, int rotX, int rotY) {
        this("", 0, Font.PLAIN, rotation, x, y, maxLength);
        this.rotX = rotX;
        this.rotY = rotY;
        setIcon(icon);
        setBounds(x, y, Math.max(getIcon().getIconWidth(), getIcon().getIconHeight()) * 2, Math.max(getIcon().getIconWidth(), getIcon().getIconHeight()) * 2);
        useRotXY = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if(!useRotXY) rotX = (int) (getWidth() / 2f);
        if(!useRotXY) rotY = (int) (getHeight() / 2f);

        if (useRotXY) g2d.rotate(Math.toRadians(rotation), (getWidth() - getIcon().getIconWidth()) / 2f + rotX, (getHeight() - getIcon().getIconHeight()) / 2f + rotY);
        //if (useRotXY) g2d.rotate(Math.toRadians(rotation), rotX, rotY);
        else g2d.rotate(Math.toRadians(rotation), rotX, rotY);

        super.paintComponent(g2d);
    }
}
