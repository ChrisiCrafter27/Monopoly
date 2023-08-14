package monopol.test;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class TestButton {
    JFrame frame;
    int x;
    int y;
    String normal;
    String presst;
    boolean clicked;
    JLabel normal_Label;
    public TestButton(JFrame frame, int x, int y, String normal, String presst){
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.normal = normal;
        this.presst = presst;
        clicked(normal,presst,frame);
    }
    public void clicked(String normal,String presst, JFrame frame){
        ImageIcon normal_Icon = new ImageIcon(normal);
        Image normal_Image = normal_Icon.getImage();
        Image normal_skaliert = normal_Image.getScaledInstance(122, 200, Image.SCALE_SMOOTH);

        if (normal_Label == null) {
            normal_Label = new JLabel(new ImageIcon(normal_skaliert));
            normal_Label.setBounds(x, y, 122, 200);
            frame.add(normal_Label);
        }
        normal_Label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!clicked){
                    normal_Label.setIcon(new ImageIcon(presst));
                    frame.repaint();
                    clicked=true;
                }
                else {
                    normal_Label.setIcon(new ImageIcon(normal));
                    frame.repaint();
                    clicked=false;
                }
            }
        });
    }
    public boolean getclicked(){
        return clicked;
    }
}
