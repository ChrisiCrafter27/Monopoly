package monopol.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class RotateJLabel extends JLabel {
    private final double rotation;
    private final int maxLength;
    public RotateJLabel(String text, int size, int font, double rotation, int x, int y, int maxLength) {
        super(text);
        this.rotation = rotation;
        this.maxLength = maxLength;
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
        //Graphics2D gx = (Graphics2D) g;
        //gx.rotate(Math.toRadians(rotation), getX() + getWidth()/2, getY() + getHeight()/2);
        //gx.translate(0, getHeight()/2);
        //gx.rotate(0.6, getX() + getWidth()/2, getY() + getHeight()/2);
        //gx.translate(getHeight(), getHeight());
        //super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.rotate(Math.toRadians(rotation), width / 2f, height / 2f); // Rotate around the center

        //g2d.setColor(Color.BLACK);
        //g2d.drawString("Rotated Text", 10, height / 2);

        super.paintComponent(g2d);
    }
}
