package monopol.client.screen;

import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerDicePane extends JLayeredPane {

    private final JLabel NW1 = JUtils.addImage("images/Würfel/NW_1.png",480-85-3,540-3,66,66);
    private final JLabel NW2 = JUtils.addImage("images/Würfel/NW_1.png",480+85-3,540-3,66,66);
    private final JLabel SW1 = JUtils.addImage("images/Würfel/SW_Monop.png",480-5,540-5,70,70);
    private Thread thread1 = new Thread();
    private Thread thread2 = new Thread();
    private Thread thread3 = new Thread();
    private Thread animationTime1 = new Thread();
    private Thread animationTime2 = new Thread();
    private Thread animationTime3 = new Thread();
    private int animation_Time1 = 0;
    private int animation_Time2 = 0;
    private int animation_Time3 = 0;
    private int breakTime1 = 10;
    private int breakTime2 = 10;
    private int breakTime3 = 10;


    public PlayerDicePane(){
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);
        add(NW1, DEFAULT_LAYER);
        add(NW2, DEFAULT_LAYER);
        add(SW1, DEFAULT_LAYER);
    }
    public void reset() {
        setVisible(false);
        thread1.interrupt();
        thread2.interrupt();
        thread3.interrupt();
    }
    public void showWithoutAnim(int Dice1, int Dice2,int Dice3) {
        setVisible(true);
        NW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/NW_"+Dice1+".png").getImage().getScaledInstance(66,66, Image.SCALE_SMOOTH)));
        NW2.setIcon(new ImageIcon(new ImageIcon("images/Würfel/NW_"+Dice2+".png").getImage().getScaledInstance(66,66,Image.SCALE_SMOOTH)));
        if(Dice3 <= 3){
            SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_"+Dice3+".png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
        }
        if(Dice3 == 4){
            SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_Bus.png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
        }
        if(Dice3 == 5 || Dice3 ==6){
            SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_Monop.png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
        }
    }
    public void show(int Dice1, int Dice2,int Dice3){

        thread1.interrupt();
        thread2.interrupt();
        thread3.interrupt();

        animationTime1.interrupt();
        animationTime2.interrupt();
        animationTime3.interrupt();

        setVisible(true);

        animation_Time1 = 10;
        animation_Time2 = 10;
        animation_Time3 = 10;

        animationTime1 = new Thread(() -> {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {return;}
            while (!animationTime1.isInterrupted() && animation_Time1 <= 3000) {animation_Time1 += 10;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {return;}
            }
        });
        animationTime1.start();

        animationTime3 = new Thread(() -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {return;}
            while (!animationTime3.isInterrupted() && animation_Time3 <= 3000) {animation_Time3 += 10;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {return;}
            }
        });
        animationTime3.start();

        animationTime2 = new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {return;}
            while (!animationTime2.isInterrupted() && animation_Time2 <= 3000) {animation_Time2 += 10;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {return;}
            }
        });
        animationTime2.start();



        thread1 = new Thread(() -> {
            int ran = Dice1 -1;
            if(ran == 0){ran = 6;}
            while (!thread1.isInterrupted()){
                try {
                    Thread.sleep(breakTime1);
                } catch (InterruptedException e) {
                    return;
                }
                if(ran <6){ran +=1;}else{ran =1;}
                if(animation_Time1<3000){
                    NW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/NW_"+ran+".png").getImage().getScaledInstance(66,66,Image.SCALE_SMOOTH)));
                    breakTime1 = (int) (500*(1-Math.exp(-0.0005 * animation_Time1)));
                    System.out.println(animation_Time1);
                }
                else{
                    NW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/NW_"+Dice1+".png").getImage().getScaledInstance(66,66,Image.SCALE_SMOOTH)));
                    thread1.interrupt();
                }
            }
        });
        thread1.start();

        thread2 = new Thread(() -> {
            int ran = Dice2 -1;
            if(ran == 0){ran = 6;}
            while (!thread2.isInterrupted()){
                try {
                    Thread.sleep(breakTime2);
                } catch (InterruptedException e) {
                    return;
                }
                if(ran <6){ran +=1;}else{ran =1;}
                if(animation_Time2<3000){
                    NW2.setIcon(new ImageIcon(new ImageIcon("images/Würfel/NW_"+ran+".png").getImage().getScaledInstance(66,66,Image.SCALE_SMOOTH)));
                    breakTime2 = (int) (500*(1-Math.exp(-0.0005 * animation_Time2)));
                    System.out.println(animation_Time2);
                }
                else{
                    NW2.setIcon(new ImageIcon(new ImageIcon("images/Würfel/NW_"+Dice2+".png").getImage().getScaledInstance(66,66,Image.SCALE_SMOOTH)));
                    thread2.interrupt();
                }
            }
        });
        thread2.start();


        thread3 = new Thread(() -> {
            int ran = Dice3 -1;
            if(ran == 0){ran = 6;}
            while (!thread3.isInterrupted()){
                try {
                    Thread.sleep(breakTime3);
                } catch (InterruptedException e) {
                    return;
                }
                if(ran <6){ran +=1;}else{ran =1;}
                if(animation_Time3<3000){
                    if(ran <= 3){
                        SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_"+ran+".png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
                    }
                    if(ran == 5){
                        SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_Bus.png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
                    }
                    if(ran == 4 || ran ==6){
                        SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_Monop.png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
                    }
                    breakTime3 = (int) (500*(1-Math.exp(-0.0005 * animation_Time3)));
                }
                else{
                    if(Dice3 <= 3){
                        SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_"+Dice3+".png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
                    }
                    if(Dice3 == 5){
                        SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_Bus.png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
                    }
                    if(Dice3 == 4 || Dice3 ==6){
                        SW1.setIcon(new ImageIcon(new ImageIcon("images/Würfel/SW_Monop.png").getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH)));
                    }
                    thread3.interrupt();
                }
            }
        });
        thread3.start();
    }
}
