package monopol.utils;

import javax.swing.*;
import java.awt.*;

public class JUtils {
    public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    public static int getX(double x) {
        return (int) (x * (SCREEN_WIDTH / 1920d));
    }
    public static int getY(double y) {
        return (int) (y * (SCREEN_HEIGHT) / 1080d);
    }

    public static boolean hasComponent(JFrame frame, Component c)
    {
        Component[] components = frame.getContentPane().getComponents();
        for (Component component : components) {
            if (c == component) {
                return true;
            }
        }
        return false;
    }
}
