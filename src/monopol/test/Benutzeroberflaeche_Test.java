package monopol.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class Benutzeroberflaeche_Test {

    public static void main(String[] args) {
        JFrame frame = frame();
        bild(frame);
        Text(frame);
        Knopf(frame);
        frame.setVisible(true);
    }

    private static JFrame frame(){
        JFrame frame = new JFrame("Monopoly");
        frame.setSize(800, 600);
        return frame;
    }

    private static void bild(JFrame frame) {

        String pfad = "Bilder/Monopoly.png";

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
        knopf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "YAY!");
            }
        });
        knopf.setBounds(300, 400,200,80);
        frame.add(knopf);
    }
    public void hi(){
        System.out.println("hi");
    }
}


