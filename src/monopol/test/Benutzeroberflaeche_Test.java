package monopol.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




public class Benutzeroberflaeche_Test {


    static int value2 = 5;
    public static void main(String[] args) {
        JFrame frame = frame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        TestButton button = new TestButton(frame, 500 , 300,"images/Monopoly.png", "images/walk1.png" );
        bild(frame);
        Text(frame);
        Knopf(frame);
        Regler(frame);




        frame.setVisible(true);
    }

    private static JFrame frame(){
        JFrame frame = new JFrame("Monopoly");
        frame.setSize(800, 600);
        return frame;
    }

    private static void bild(JFrame frame) {

        String pfad = "images/Monopoly.png";

        ImageIcon imageIcon = new ImageIcon(pfad);
        Image normalImage = imageIcon.getImage();
        int breite = 250;
        int hoehe = 60;
        Image skaliertesImage = normalImage.getScaledInstance(breite, hoehe, Image.SCALE_SMOOTH);
        JLabel imageLabel2 = new JLabel(new ImageIcon(skaliertesImage));
        imageLabel2.setBounds(300, 10, breite, hoehe);
        frame.add(imageLabel2);
        frame.setLayout(null);
    }

    private static void Text(JFrame frame){
        String text = "Mein test Text!";
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setBounds(100, 200, 300, 50);
        frame.add(label);
    }

    private static void Knopf(JFrame frame){
        JButton knopf = new JButton("Klick mich!");
        knopf.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "YAY!");
            walking(frame);
        });
        knopf.setBounds(300, 200,200,80);
        frame.add(knopf);
    }

    private static void Regler(JFrame frame){
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBounds(100, 300, 400, 50);

        String text = "hi";
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setBounds(100, 250, 300, 50);
        frame.add(label);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int value = slider.getValue();
                System.out.println("abstand zwischen animation_frames: " + value+ "%");
                String labeltext = String.valueOf(value);
                label.setText(labeltext);
                value2 = value / 10;
            }
        });
        frame.add(slider);
    }
    private static void walking(JFrame frame) {
        final int xpos = 200;
        final int ypos = 400;
        int xzielpos = 500;
        int yzielpos = 400;
        int hoehe = 122;
        int breite = 122;

        String pfad = "images/walk1.png";
        ImageIcon imageIcon = new ImageIcon(pfad);
        Image normalImage = imageIcon.getImage();
        Image skaliertesImage = normalImage.getScaledInstance(breite, hoehe, Image.SCALE_SMOOTH);
        JLabel imageLabel2 = new JLabel(new ImageIcon(skaliertesImage));
        imageLabel2.setBounds(xpos, ypos, breite, hoehe);
        frame.add(imageLabel2);
        frame.setLayout(null);

        Timer timer = new Timer(20, new ActionListener() {
            int xposs = xpos;
            int yposs = ypos;
            int animation = 0;
            int pfadzaehler = 0;
            int pfadzaehler2 = 7;
            String animationpfad;
            boolean animationpfad2 = false;
            boolean drehen = false;
            public void actionPerformed(ActionEvent e) {

                if (xposs < xzielpos) {
                    xposs++;
                }
                if (yposs < yzielpos) {
                    yposs++;
                }
                if (yposs > yzielpos) {
                    yposs--;
                }
                if (xposs > xzielpos) {
                    xposs--;
                    drehen = true;
                }
                imageLabel2.setBounds(xposs, yposs, 122, 122);
                if (xposs == xzielpos && yposs == yzielpos) {
                    ((Timer) e.getSource()).stop();
                    frame.remove(imageLabel2);
                }
                animation++;
                if (animation >= value2) {
                    if (!drehen && !animationpfad2) {
                        pfadzaehler++;
                        animationpfad = "images/walk" + pfadzaehler + ".png";
                        ImageIcon imageIcon = new ImageIcon(animationpfad);
                        imageLabel2.setIcon(imageIcon);
                    }
                    if (!drehen && animationpfad2) {
                        pfadzaehler2--;
                        animationpfad = "images/walk" + pfadzaehler2 + ".png";
                        ImageIcon imageIcon = new ImageIcon(animationpfad);
                        imageLabel2.setIcon(imageIcon);
                    }
                    if (drehen && !animationpfad2) {
                        pfadzaehler++;
                        animationpfad = "images/walk" + pfadzaehler + ".png";
                        drehen = true;
                        ImageIcon imageIcon = new ImageIcon(animationpfad);
                        imageLabel2.setIcon(imageIcon);
                    }
                    if (drehen && animationpfad2) {
                        pfadzaehler2--;
                        animationpfad = "images/walk" + pfadzaehler2 + ".png";
                        drehen = true;
                        ImageIcon imageIcon = new ImageIcon(animationpfad);
                        imageLabel2.setIcon(imageIcon);
                    }

                    if (pfadzaehler >= 6) {
                        pfadzaehler = 1;
                        animationpfad2 = true;
                    }
                    if (pfadzaehler2 <= 1) {
                        pfadzaehler2 = 6;
                        animationpfad2 = false;
                    }
                    animation = 0;
                }
            }
    });
    timer.start();
    }
}


