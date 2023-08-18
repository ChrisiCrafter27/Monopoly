package monopol.screen;

import com.sun.tools.javac.Main;
import monopol.test.TestButton;
import monopol.utils.KeyHandler;
import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.sql.SQLOutput;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import java.awt.event.MouseListener;


public class GameWindow {

    JFrame frame = new JFrame("Monopoly");
    JLayeredPane MENUPanel = new JLayeredPane();
    JLayeredPane HostGame = new JLayeredPane();
    JLayeredPane JoinGame = new JLayeredPane();


    private ArrayList clickedButtonsMap = new ArrayList();
    private Point mousePosition;
    private boolean animated = false;
    public GameWindow(){
        frame.setUndecorated(true);
        frame.setSize(new Dimension( 1920,1080));
        frame.setResizable(true);
        frame.setFocusable(true);
        frame.requestFocus();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        MENUPanel.setSize(frame.getSize());
        frame.setVisible(true);
    }
    public static void main(String[] args){
        GameWindow window = new GameWindow();
        window.Mainmenu();
    }
    public JFrame getframe(){
        return frame;
    }
    public JLayeredPane getMENUpanel(){
        return MENUPanel;
    }
    public void Mainmenu(){
        frame.add(MENUPanel);
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();

            }
        });

        setframebild("images/Monopoly_client1.png",MENUPanel);

        addText_panel("Hello i am Mr Monopoly and i will teach you ",MENUPanel,"Arial",40,1000,310,1000,50);
        addText_panel("everything you need to know",MENUPanel,"Arial",40,1000,360,1000,50);

        addbutton_panel("butt1","images/Join_Server_0_0.png","images/Join_Server_0_1.png",true,"images/Join_Server_1_0.png","images/Join_Server_1_1.png",MENUPanel,120,350,450,100, new MouseAdapter()  {
            public void mouseClicked(MouseEvent e){
                if(clickedButtonsMap.contains("butt2")){
                    System.out.println("Button2 allredy presst: " + clickedButtonsMap.contains("butt2"));
                    System.out.println("error");
                    MENUPanel.removeAll();
                    MENUPanel.repaint();
                }
                else{
                    if(!clickedButtonsMap.contains("butt1")){
                        System.out.println("button1 wurde schon gecklickt: "+ clickedButtonsMap.contains("butt1"));
                        Joingame(MENUPanel);
                        System.out.println("joinGame wird ausgeführt");
                        MENUPanel.repaint();
                    }
                    else {
                        System.out.println("button1 wurde schon gecklickt: "+ clickedButtonsMap.contains("butt1"));
                        JoinGame.removeAll();
                        System.out.println("removing pannels auf Menüpanel");
                        MENUPanel.repaint();
                    }
                }

            }
        });

        addbutton_panel("butt2","images/Host_Server_0_0.png","images/Host_Server_0_1.png.png",true,"images/Host_Server_1_0.png","images/Host_Server_1_1.png",MENUPanel,120,480,450,100, new MouseAdapter()  {
            public void mouseClicked(MouseEvent e){
                if(clickedButtonsMap.contains("butt1")){
                    System.out.println("error2");
                    JoinGame.removeAll();
                }
                else{
                    if(!clickedButtonsMap.contains("butt2")){
                        hostgame(MENUPanel);
                        System.out.println("hostgame");
                    }
                    else {
                        System.out.println("allredy in hostgame");
                        MENUPanel.removeAll();
                    }
                }
            }
        });
    }
    public void Joingame(JLayeredPane panel){
        System.out.println("layer of joingame:"+ MENUPanel.getLayer(JoinGame));
        JoinGame.setBounds(0, 0, 800, 600);
        panel.add(JoinGame, 0);
        setPannelBild("images/Host_Server_0_0.png", JoinGame);
        JoinGame.setVisible(true);
        MENUPanel.repaint();
    }

    public void hostgame(JLayeredPane panel){
        panel.add(HostGame,JLayeredPane.POPUP_LAYER);
    }

    public void addText_panel(String text, JLayeredPane panel,String font,int size,int x, int y,int Width, int Height){
        String Text = text;
        JLabel label = new JLabel(Text);
        label.setFont(new Font(font, Font.PLAIN, size));
        label.setBounds(x, y, Width, Height);
        panel.add(label,JLayeredPane.POPUP_LAYER);
        panel.repaint();
        panel.revalidate();
    }

    public void addbutton_panel(String name, String Bild1, String Bild2,boolean mouseanimation,String Bild3_1,String Bild3_2, JLayeredPane panel, int x, int y, int Width, int Height, MouseListener actionEvent){

        ImageIcon button_Icon = new ImageIcon(Bild1);
        Image Button_Image = button_Icon.getImage();
        Image Button_skaliert = Button_Image.getScaledInstance(Width, Height, Image.SCALE_SMOOTH);

        ImageIcon button2_Icon = new ImageIcon(Bild2);
        Image Button2_Image = button2_Icon.getImage();
        Image Button2_skaliert = Button2_Image.getScaledInstance(Width, Height, Image.SCALE_SMOOTH);



        JButton button = new JButton(new ImageIcon(Button_skaliert));
        button.setBounds(x , y, Width, Height);
        button.addMouseListener(actionEvent);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setIcon(new ImageIcon(new ImageIcon(Bild1).getImage().getScaledInstance(Width, Height, Image.SCALE_SMOOTH)));
        button.setDisabledIcon(new ImageIcon(new ImageIcon(Bild1).getImage().getScaledInstance(Width, Height, Image.SCALE_SMOOTH)));
        button.setPressedIcon(new ImageIcon(new ImageIcon(Bild2).getImage().getScaledInstance(Width, Height, Image.SCALE_SMOOTH)));
        button.setSelectedIcon(new ImageIcon(new ImageIcon(Bild2).getImage().getScaledInstance(Width, Height, Image.SCALE_SMOOTH)));
        if (mouseanimation) {
            button.setRolloverSelectedIcon(new ImageIcon(new ImageIcon(Bild3_2).getImage().getScaledInstance(Width, Height, Image.SCALE_SMOOTH)));
            button.setRolloverIcon(new ImageIcon(new ImageIcon(Bild3_1).getImage().getScaledInstance(Width, Height, Image.SCALE_SMOOTH)));
        }


        panel.add(button, JLayeredPane.POPUP_LAYER);
        panel.repaint();
        panel.revalidate();

        MouseListener actionEvent2 = new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if (!clickedButtonsMap.contains(name)){
                    clickedButtonsMap.add(name);
                    button.setSelected(true);
                }
                else if (clickedButtonsMap.contains(name)){
                    clickedButtonsMap.remove(name);
                    button.setSelected(false);
                }
            }
        };
        button.addMouseListener(actionEvent2);
    }
    public void setframebild(String bild,JLayeredPane panel){
        panel.setLayout(null);
        ImageIcon imageIcon = new ImageIcon(bild);
        Image image = imageIcon.getImage();
        Image skaliertesImage = image.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
        JLabel BILD = new JLabel(new ImageIcon(skaliertesImage));
        BILD.setBounds(0,0, frame.getWidth(), frame.getHeight());
        panel.add(BILD,JLayeredPane.DEFAULT_LAYER);
        panel.repaint();
        panel.revalidate();
    }
    public void setPannelBild(String Bild,JLayeredPane panel){


        //System.out.println("Panel size: " + panel.getWidth() + "x" + panel.getHeight());
        //System.out.println("Image path: " + Bild);

        ImageIcon imageIcon = new ImageIcon(Bild);
        //System.out.println("Image loaded: " + (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE));

        Image image = imageIcon.getImage();
        //System.out.println("Image size: " + image.getWidth(null) + "x" + image.getHeight(null));

        Image skaliertesImage = image.getScaledInstance(55, 20, Image.SCALE_SMOOTH);
        //System.out.println("panel size: " + panel.getWidth() + "x" + panel.getHeight());
        //System.out.println("skaliertesImage size: " + skaliertesImage.getWidth(null) + "x" + skaliertesImage.getHeight(null));
        JLabel BILD = new JLabel(new ImageIcon(skaliertesImage));
        BILD.setBounds(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight());
        //System.out.println("bild bounds: " + BILD.getBounds());
        panel.add(BILD, 3);
        panel.repaint();
        panel.revalidate();
    }
    public void addEingabeFeld(){

    }

}
