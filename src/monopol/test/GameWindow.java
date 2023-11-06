package monopol.test;

import monopol.utils.KeyHandler;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.awt.event.MouseEvent;


public class GameWindow {

    JFrame frame = new JFrame("Monopoly");
    JLayeredPane MENUPanel = new JLayeredPane();
    JLayeredPane HostGame = new JLayeredPane();
    JLayeredPane JoinGame = new JLayeredPane();
    private final ArrayList<String> clickedButtonsMap = new ArrayList();
    int normalWidth = 1920;
    int normalHeight= 1080;
    double multiplikator_Width;
    double multiplikator_Height;

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

        multiplikator_Width = (double) frame.getWidth() / normalWidth;
        multiplikator_Height = (double) frame.getHeight() / normalHeight;
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
        setframebild("images/Monopoly_client1.png",MENUPanel);
        addText_panel("Hello i am Mr Monopoly and i will teach you ",MENUPanel,"Arial",30, 1000 * multiplikator_Width, 310 * multiplikator_Height,1000,50);
        addText_panel("everything you need to know",MENUPanel,"Arial",30,1000 * multiplikator_Width, 360 * multiplikator_Height,1000,50);

        addbutton_panel("butt1","images/Join_Server_0_0.png","images/Join_Server_0_1.png",true,"images/Join_Server_1_0.png","images/Join_Server_1_1.png",MENUPanel,120 * multiplikator_Width,350 * multiplikator_Height,450,100, actionevent ->  {
            if(clickedButtonsMap.contains("butt2")){
                System.out.println("Button2 allredy presst: " + clickedButtonsMap.contains("butt2"));
                HostGame.removeAll();
                clickedButtonsMap.remove("butt2");
                clickedButtonsMap.add("butt1");
                Joingame(MENUPanel);
                MENUPanel.repaint();
            }
            else{
                if(!clickedButtonsMap.contains("butt1")){
                    System.out.println("button1 wurde schon gecklickt: "+ clickedButtonsMap.contains("butt1"));
                    Joingame(MENUPanel);
                    System.out.println("joinGame wird ausgeführt");
                    MENUPanel.repaint();
                    clickedButtonsMap.add("butt1");
                }
                else {
                    System.out.println("button1 wurde schon gecklickt: "+ clickedButtonsMap.contains("butt1"));
                    JoinGame.removeAll();
                    clickedButtonsMap.remove("butt1");
                    System.out.println("removing pannels auf Menüpanel");
                    MENUPanel.repaint();
                }
            }
        });

        addbutton_panel("butt2","images/Host_Server_0_0.png","images/Host_Server_0_1.png.png",true,"images/Host_Server_1_0.png.png","images/Host_Server_1_1.png.png",MENUPanel,120* multiplikator_Width,480* multiplikator_Height,450,100, actionEvent -> {
            if(clickedButtonsMap.contains("butt1")){
                System.out.println("Button1 allredy presst: " + clickedButtonsMap.contains("butt1"));
                JoinGame.removeAll();
                clickedButtonsMap.remove("butt1");
                clickedButtonsMap.add("butt2");
                hostgame(MENUPanel);
                MENUPanel.repaint();
            }
            else{
                if(!clickedButtonsMap.contains("butt2")){
                    System.out.println("Button1 allredy presst: " + clickedButtonsMap.contains("butt1"));
                    hostgame(MENUPanel);
                    System.out.println("hostgame wird ausgeführt");
                    clickedButtonsMap.add("butt2");
                }
                else {
                    System.out.println("Button1 allredy presst: " + clickedButtonsMap.contains("butt1"));
                    System.out.println("removing pannels auf Menüpanel");
                    clickedButtonsMap.remove("butt2");
                    HostGame.removeAll();
                    MENUPanel.repaint();
                }
            }
        });
    }
    public void Joingame(JLayeredPane panel){



        /*
        System.out.println("layer of joingame:"+ MENUPanel.getLayer(JoinGame));
        JoinGame.setBounds(100, 400,200, 300);
        panel.add(JoinGame, 0);
        setPannelBild("images/Host_Server_0_0.png", JoinGame);
        JoinGame.setVisible(true);
        MENUPanel.repaint();
        addEingabeFeld(JoinGame, 20, 20,200, 700, 500, 100);
         */
    }

    public void hostgame(JLayeredPane panel){

        panel.add(HostGame,JLayeredPane.POPUP_LAYER);
        System.out.println("layer of hostgame:"+ MENUPanel.getLayer(HostGame));
        HostGame.setBounds(100, 200, 800, 600);
        panel.add(HostGame, 0);
        setPannelBild("images/DO_NOT_CHANGE/plain_button_0.png", HostGame);
        HostGame.setVisible(true);
        MENUPanel.repaint();
        //addEingabeFeld(HostGame, 20, 20, 20, 20, 500, 100);
    }

    public void addText_panel(String text, JLayeredPane panel,String font,int size,double x, double y,int Width, int Height){
        String Text = text;
        JLabel label = new JLabel(Text);
        label.setFont(new Font(font, Font.PLAIN, size));
        int x2 = (int) x;
        int y2 = (int) y;
        label.setBounds(x2, y2, Width, Height);
        panel.add(label,JLayeredPane.POPUP_LAYER);
        panel.repaint();
        //panel.revalidate();
    }

    public void addbutton_panel(String name, String Bild1, String Bild2,boolean mouseanimation,String Bild3_1,String Bild3_2, JLayeredPane panel, double x1, double y1, int Width, int Height, ActionListener actionEvent){

        ImageIcon button_Icon = new ImageIcon(Bild1);
        Image Button_Image = button_Icon.getImage();
        Image Button_skaliert = Button_Image.getScaledInstance(Width, Height, Image.SCALE_SMOOTH);

        int x = (int) x1;
        int y = (int) y1;

        JButton button = new JButton(new ImageIcon(Button_skaliert));
        button.setBounds(x , y, Width, Height);
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
        //panel.revalidate();

        button.addActionListener(actionEvent);

        new Thread() {
            @Override
            public void run() {
                while(!interrupted()) {
                    button.setSelected(clickedButtonsMap.contains(name));
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();
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
        //panel.revalidate();
    }

    public void setPannelBild(String Bild,JLayeredPane panel){


        //System.out.println("Panel size: " + panel.getWidth() + "x" + panel.getHeight());
        //System.out.println("Image path: " + Bild);

        ImageIcon imageIcon = new ImageIcon(Bild);
        //System.out.println("Image loaded: " + (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE));

        Image image = imageIcon.getImage();
        //System.out.println("Image size: " + image.getWidth(null) + "x" + image.getHeight(null));

        Image skaliertesImage = image.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_SMOOTH);
        //System.out.println("panel size: " + panel.getWidth() + "x" + panel.getHeight());
        //System.out.println("skaliertesImage size: " + skaliertesImage.getWidth(null) + "x" + skaliertesImage.getHeight(null));
        JLabel BILD = new JLabel(new ImageIcon(skaliertesImage));
        BILD.setBounds(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight());
        //System.out.println("bild bounds: " + BILD.getBounds());
        panel.add(BILD, 3);
        panel.repaint();
        //panel.revalidate();
    }


    public void addEingabeFeld(JLayeredPane panel, int fontSize, int maxLength, double x, double y, int width, int height) {

        int labelX = (int) x;
        int labelY = (int) y;

        Thread Eingabe = new Thread() {
            @Override
            public void run() {

                KeyHandler keyHandler = new KeyHandler();
                frame.addKeyListener(keyHandler);
                frame.requestFocus();
                JLabel label = new JLabel("");
                label.setBounds(labelX, labelY, width, height);
                label.setFont(label.getFont().deriveFont(fontSize));
                panel.add(label);
                while (!keyHandler.isKeyPressed(KeyEvent.VK_ENTER) && !keyHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {
                    //System.out.println(keyHandler.getString());
                    label.setText(keyHandler.getString());
                    panel.repaint();
                    MENUPanel.repaint();
                    frame.repaint();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                System.out.println(keyHandler.getString());
                if(keyHandler.getString().length() > maxLength) {

                } else if(keyHandler.getString().equals("")) {

                } else if(keyHandler.isKeyPressed(KeyEvent.VK_ESCAPE)) {

                } else if(keyHandler.isKeyPressed(KeyEvent.VK_ENTER)) {

                }
            }
        };
        Eingabe.start();
        System.out.println("Thread started");
    }

}
