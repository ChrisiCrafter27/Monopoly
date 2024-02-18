package monopol.common.utils;

import com.sun.tools.javac.Main;
import monopol.common.log.ServerLogger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JUtils {
    public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    public static int getX(double x) {
        return (int) (x * (SCREEN_WIDTH / 1920d));
    }
    public static int getY(double y) {
        return (int) (y * (SCREEN_HEIGHT) / 1080d);
    }

    public static JButton addButton(String display, int x, int y, int width, int height, boolean enabled, ActionListener actionEvent) {
        JButton button = new JButton(display);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        button.addActionListener(actionEvent);
        button.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        button.setEnabled(enabled);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        button.setIcon(new ImageIcon(imageIcon("images/DO_NOT_CHANGE/plain_button_2.png").getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    public static JButton addButton(JButton button, String display, int x, int y, int width, int height, boolean enabled, String icon, ActionListener actionEvent) {
        button.setText(display);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        button.addActionListener(actionEvent);
        button.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        button.setEnabled(enabled);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        button.setIcon(new ImageIcon(imageIcon(icon).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    public static JButton addButton(JButton button, String display, int x, int y, int width, int height, boolean enabled, String icon,String disabled_icon, ActionListener actionEvent) {
        button = addButton(button,display,x,y,width,height,enabled,icon,actionEvent);
        button.setDisabledIcon(new ImageIcon(imageIcon(disabled_icon).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        return button;
    }

    public static JButton addButton(String display, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        JButton button = addButton(display, x, y, width, height, enabled, actionEvent);
        button.setSelected(selected);
        return button;
    }

    public static JButton addButton(JButton button, String display, String icon, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        button = addButton(button, display, x, y, width, height, enabled, icon, actionEvent);
        button.setSelected(selected);
        return button;
    }

    public static JButton addButton(String display, String icon, int x, int y, int width, int height, boolean enabled, boolean selected, ActionListener actionEvent) {
        JButton button = addButton(new JButton(), display, x, y, width, height, enabled, icon, actionEvent);
        button.setSelected(selected);
        return button;
    }

    public static JLabel addText(String display, int x, int y, int width, int height, boolean centered) {
        JLabel label;
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        if(centered) label = new JLabel(display, SwingConstants.CENTER); else label = new JLabel(display);
        label.setFont(new Font("Arial", Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, (int) ( height*1.2));
        return label;
    }

    public static JLabel addText(String display, int x, int y, int width, int height, int position) {
        JLabel label;
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        label = new JLabel(display, position);
        label.setFont(new Font("Arial", Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, (int) ( height*1.2));
        label.setForeground(Color.BLACK);
        return label;
    }

    public static JLabel addText(JLabel label, String display,String font, int x, int y, int width, int height, boolean centered) {
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        if(centered) label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText(display);
        label.setFont(new Font(font, Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public static JLabel addImage(String src, int x, int y) {
        ImageIcon icon = imageIcon(src);
        if(icon.getIconHeight() > 0) icon = new ImageIcon(icon.getImage().getScaledInstance(JUtils.getX(icon.getIconWidth()), JUtils.getY(icon.getIconHeight()), Image.SCALE_DEFAULT));
        JLabel label = new JLabel(icon);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), icon.getIconWidth(), icon.getIconHeight());
        return label;
    }

    public static JLabel addImage(String src, int x, int y, int rotation, int rotX, int rotY) {
        ImageIcon icon = imageIcon(src);
        return new JRotatedLabel(icon, rotation, JUtils.getX(x), JUtils.getY(y), 0, rotX, rotY);
    }

    public static JLabel addImage(String src, int x, int y, int width, int height) {
        JLabel label = addImage(src, x, y);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        label.setIcon(new ImageIcon(((ImageIcon) label.getIcon()).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT)));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public static JLabel addText(String display, int x, int y, int width, int height) {
        JLabel label = new JLabel(display);
        width = JUtils.getX(width);
        height = JUtils.getY(height);
        label.setFont(new Font("Arial", Font.PLAIN, height));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), width, height);
        return label;
    }

    public static JLabel addRotatedText(String display, int font, int x, int y, int size, double angle, int maxLength) {
        x = JUtils.getX(x);
        y = JUtils.getY(y);
        if(angle == 0 || angle == 180) {
            maxLength = JUtils.getX(maxLength);
            size = JUtils.getX(size);
        } else if(angle == 90 || angle == -90) {
            maxLength = JUtils.getY(maxLength);
            size = JUtils.getY(size);
        }
        return new JRotatedLabel(display, size, font, angle, x, y, maxLength);
    }

    public static ImageIcon imageIcon(String path) {
        if(path.isEmpty()) return new ImageIcon("");
        try {
            return new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream("assets/" + path)));
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException(new FileNotFoundException("Unknown image: " + path));
        }
    }
}
