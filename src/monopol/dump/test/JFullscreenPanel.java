package monopol.dump.test;

import javax.swing.*;
import java.awt.*;

public class JFullscreenPanel extends JPanel {
    public JFullscreenPanel() {
        super();
        setLayout(null);
    }
    @Override
    public void paintComponent(Graphics g) {
        double originalWidth = getWidth();
        double originalHeight = getHeight();
        double targetWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        double targetHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        double widthMultiplier = originalWidth / targetWidth;
        double heightMultiplier = originalHeight / targetHeight;
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(widthMultiplier, heightMultiplier);
        g2d.translate(-1*(getX()-(getX() * widthMultiplier)), -1*(getY()-(getY() * heightMultiplier)));
        super.paintComponent(g2d);
    }
}
