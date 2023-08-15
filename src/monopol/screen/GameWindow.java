package monopol.screen;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

public class GameWindow {

    JFrame frame = new JFrame("Monopoly");
    JLayeredPane MENUPanel = new JLayeredPane();
    JLayeredPane HostGame = new JLayeredPane();
    JLayeredPane JoinGame = new JLayeredPane();

    private Map<String, Boolean> clickedButtonsMap = new HashMap<>();
    private Point mousePosition;
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

        addText_panel("Hello i am Mr Monopoly and i will teach you ",MENUPanel,"Arial",30,800,260,1000,50);
        addText_panel("everything you need to know",MENUPanel,"Arial",30,800,300,1000,50);

        addbutton_panel("butt1","images/Join_Server_0_0.png","images/Join_Server_0_1.png",false,"images/Join_Server_1_0.png","images/Join_Server_1_1.png",MENUPanel,100,270,450,100, new MouseAdapter()  {
            public void mouseClicked(MouseEvent e){
                if(clickedButtonsMap.containsKey("butt2")){
                    System.out.println("error");
                    MENUPanel.remove(HostGame);
                }
                else{
                    if(!clickedButtonsMap.containsKey("butt1")){
                        Joingame(MENUPanel);
                        System.out.println("joinGame");
                    }
                    else System.out.println("allredy in joingame");
                    MENUPanel.remove(JoinGame);
                }
            }
        });
        addbutton_panel("butt2","images/Host_Server_0_0.png","images/Host_Server_0_1.png.png",false,"images/Host_Server_1_0.png","images/Host_Server_1_1.png",MENUPanel,100,380,450,100, new MouseAdapter()  {
            public void mouseClicked(MouseEvent e){
                if(clickedButtonsMap.containsKey("butt1")){
                    System.out.println("error2");
                    MENUPanel.remove(JoinGame);
                }
                else{
                    if(!clickedButtonsMap.containsKey("butt2")){
                        hostgame(MENUPanel);
                        System.out.println("hostgame");
                    }
                    else System.out.println("allredy in hostgame");
                    MENUPanel.remove(HostGame);
                }

            }
        });
    }
    public void Joingame(JLayeredPane panel){

        JoinGame.setBounds(0, 0, 800, 600);
        panel.add(JoinGame, Integer.valueOf(6));
        System.out.println("panel wurde zu Menupanel hinzugefügt: "+panel.isAncestorOf(JoinGame));
        System.out.println("layer of Joingame: "+ panel.getLayer(JoinGame));
        System.out.println(panel.getBounds());
        setPannelBild("images/Host_Server_0_0.png", JoinGame);
        JoinGame.setVisible(true);
    }

    public void hostgame(JLayeredPane panel){

        panel.add(HostGame,Integer.valueOf(6));
    }

    public void addText_panel(String text, JLayeredPane panel,String font,int size,int x, int y,int Width, int Height){
        String Text = text;
        JLabel label = new JLabel(Text);
        label.setFont(new Font(font, Font.PLAIN, size));
        label.setBounds(x, y, Width, Height);
        panel.add(label,Integer.valueOf(3));
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

        ImageIcon button3_Icon = new ImageIcon(Bild3_1);
        Image Button3_Image = button3_Icon.getImage();
        Image Button3_1_skaliert = Button3_Image.getScaledInstance(Width, Height, Image.SCALE_SMOOTH);

        ImageIcon button3_2_Icon = new ImageIcon(Bild3_2);
        Image Button3_2_Image = button3_2_Icon.getImage();
        Image Button3_2_skaliert = Button3_2_Image.getScaledInstance(Width, Height, Image.SCALE_SMOOTH);

        JLabel button = new JLabel(new ImageIcon(Button_skaliert));
        button.setBounds(x , y, Width, Height);
        button.addMouseListener(actionEvent);
        panel.add(button,Integer.valueOf(3));
        panel.repaint();
        panel.revalidate();

        MouseListener actionEvent2 = new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if (!clickedButtonsMap.containsKey(name)){
                    clickedButtonsMap.put(name,true);
                    button.setIcon(new ImageIcon(Button2_skaliert));
                }
                else if (clickedButtonsMap.containsKey(name)){
                    clickedButtonsMap.remove(name,true);
                    button.setIcon(new ImageIcon(Button_skaliert));
                }
            }
        };
        button.addMouseListener(actionEvent2);
        if (mouseanimation){
            frame.addMouseMotionListener(new MouseMotionAdapter() {

                @Override
                public void mouseMoved(MouseEvent e) {
                    if(mousePosition.getX() >= x &&  mousePosition.getX() <= x + Width && mousePosition.getY() >= y && mousePosition.getY() <= y + Height ){

                        if (clickedButtonsMap.containsKey(name)){
                            button.setIcon(new ImageIcon(Button3_2_skaliert));
                            System.out.println("butt3_2 angezeigt");
                        }
                        if(!clickedButtonsMap.containsKey(name)){
                            button.setIcon(new ImageIcon(Button3_1_skaliert));
                            System.out.println("butt3_1 angezeigt");
                        }

                    }
                    else {
                        if (clickedButtonsMap.containsKey(name) ) {
                            button.setIcon(new ImageIcon(Button2_skaliert));
                            System.out.println("presst button anggezeigt");
                        }
                        if (!clickedButtonsMap.containsKey(name) ) {
                            button.setIcon(new ImageIcon(Button_skaliert));
                            System.out.println("noraml button anggezeigt");
                        }
                    }
                }
            });
        }
    }
    public void setframebild(String bild,JLayeredPane panel){

        //System.out.println("Bildpfad: " + bild);
        ImageIcon imageIcon = new ImageIcon(bild);
        //System.out.println("Bild geladen: " + (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE));
        Image image = imageIcon.getImage();
        //System.out.println("Bildgröße: " + image.getWidth(null) + "x" + image.getHeight(null));
        Image skaliertesImage = image.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
        //System.out.println("skaliertesImage: " + skaliertesImage.getWidth(null) + "x" + skaliertesImage.getHeight(null));
        JLabel BILD = new JLabel(new ImageIcon(skaliertesImage));
        BILD.setBounds(0,0, frame.getWidth(), frame.getHeight());
        panel.add(BILD,Integer.valueOf(2));
        panel.repaint();
        panel.revalidate();
    }
    public void setPannelBild(String Bild,JLayeredPane panel){


        System.out.println("Panel size: " + panel.getWidth() + "x" + panel.getHeight());
        System.out.println("Image path: " + Bild);
        ImageIcon imageIcon = new ImageIcon(Bild);
        System.out.println("Image loaded: " + (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE));
        Image image = imageIcon.getImage();
        System.out.println("Image size: " + image.getWidth(null) + "x" + image.getHeight(null));
        Image skaliertesImage = image.getScaledInstance(55, 20, Image.SCALE_SMOOTH);
        System.out.println("panel size: " + panel.getWidth() + "x" + panel.getHeight());
        System.out.println("skaliertesImage size: " + skaliertesImage.getWidth(null) + "x" + skaliertesImage.getHeight(null));
        JLabel BILD = new JLabel(new ImageIcon(skaliertesImage));
        BILD.setBackground(Color.BLACK);
        BILD.setBounds(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight());
        System.out.println("bild bounds: " + BILD.getBounds());
        panel.add(BILD, Integer.valueOf(7));
        System.out.println("panelBild hinzugefügt: "+ panel.isAncestorOf(BILD));
        System.out.println("layer of Bild in Joingame: "+ JoinGame.getLayer(BILD));

        panel.setVisible(true);
        MENUPanel.setVisible(true);
        frame.setVisible(true);
        panel.repaint();
        panel.revalidate();
    }
    public void addEingabeFeld(){

    }

}
