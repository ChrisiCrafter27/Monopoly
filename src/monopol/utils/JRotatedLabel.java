package monopol.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class JRotatedLabel extends JLabel {
    private final double rotation;

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
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        g2d.rotate(Math.toRadians(rotation), width / 2f, height / 2f);

        super.paintComponent(g2d);
    }
}
