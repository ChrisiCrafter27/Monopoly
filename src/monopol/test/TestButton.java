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
    public TestButton(JFrame frame, int x, int y, String normal, String presst){


        this.frame = frame;
        this.x = x;
        this.y = y;
        this.normal = normal;
        this.presst = presst;
        image(frame);
    }

    public void image(JFrame frame){
        ImageIcon normal_Icon = new ImageIcon(normal);
        Image normal_Image = normal_Icon.getImage();
        Image normal_skaliert = normal_Image.getScaledInstance(122, 200, Image.SCALE_SMOOTH);
        JLabel normal_Label = new JLabel(new ImageIcon(normal_skaliert));
        normal_Label.setBounds(x, y, 122, 200);
        frame.add(normal_Label);

        ImageIcon presst_Icon = new ImageIcon(presst);
        Image presst_Image = presst_Icon.getImage();
        Image presst_skaliert = presst_Image.getScaledInstance(122, 200, Image.SCALE_SMOOTH);
        JLabel presst_Label = new JLabel(new ImageIcon(presst_skaliert));
        presst_Label.setBounds(x, y, 122, 200);

        clicked(normal_Label,presst_Label);
    }

    public void clicked(JLabel normal, JLabel presst){
        normal.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                System.out.println("hi");
                frame.add(presst);
                frame.remove(normal);
            }
        });
        presst.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                System.out.println("jo");
                frame.add(normal);
                frame.remove(presst);
            }
        });

    }

}
