package monopol.client.screen;

import monopol.common.utils.JUtils;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerDicePane extends JLayeredPane {

    private final JLabel NW1 = JUtils.addImage("images/Würfel/NW_1.png",480-70,540,60,60);
    private final JLabel NW2 = JUtils.addImage("images/Würfel/NW_1.png",480+70,540,60,60);
    private final JLabel SW1 = JUtils.addImage("images/Würfel/SW_Monop.png",480-5,540-5,70,70);
    private Thread thread1 = new Thread();
    private Thread thread2 = new Thread();
    private Thread thread3 = new Thread();
    private Thread animationTime = new Thread();
    private int animation_Time = 0;


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
        NW1.setIcon(new ImageIcon("images/Würfel/NW_"+Dice1+".png"));
        NW2.setIcon(new ImageIcon("images/Würfel/NW_"+Dice2+".png"));
        if(Dice3 <= 3){
            SW1.setIcon(new ImageIcon("images/Würfel/SW_"+Dice3+".png"));
        }
        if(Dice3 == 4){
            SW1.setIcon(new ImageIcon("images/Würfel/SW_Bus.png"));
        }
        if(Dice3 == 5 || Dice3 ==6){
            SW1.setIcon(new ImageIcon("images/Würfel/SW_Monop.png"));
        }
    }
    public void show(int Dice1, int Dice2,int Dice3){

        thread1.interrupt();
        thread2.interrupt();
        thread3.interrupt();

        setVisible(true);

        animation_Time = 0;

        animationTime = new Thread(() -> {
            while (!animationTime.isInterrupted() && animation_Time < 6000) {
                animation_Time += 10;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        animationTime.start();

        thread1 = new Thread(() -> {
            int breakTime1 = 10;
            int W1_animationTime = 1000;
            while (!thread1.isInterrupted()){
                try {
                    Thread.sleep(breakTime1);
                } catch (InterruptedException e) {
                    return;
                }
                Random random = new Random();
                if(animation_Time<W1_animationTime){
                    NW1.setIcon(new ImageIcon("images/Würfel/NW_"+(random.nextInt(6)+1)+".png"));
                    breakTime1 += (int) (500*(1-Math.exp(-0.0005 * animation_Time)));
                }
                else{
                    NW1.setIcon(new ImageIcon("images/Würfel/NW_"+Dice1+".png"));
                    thread1.interrupt();
                }
            }
        });
        thread1.start();

        thread2 = new Thread(() -> {
            int breakTime2 = 10;
            int W2_animationTime = 2000;
            while (!thread2.isInterrupted()){
                try {
                    Thread.sleep(breakTime2);
                } catch (InterruptedException e) {
                    return;
                }
                Random random = new Random();
                if(animation_Time<W2_animationTime){
                    NW2.setIcon(new ImageIcon("images/Würfel/NW_"+(random.nextInt(6)+1)+".png"));
                    breakTime2 += (int) (500*(1-Math.exp(-0.0005 * animation_Time)));
                }
                else{
                    NW2.setIcon(new ImageIcon("images/Würfel/NW_"+Dice2+".png"));
                    thread1.interrupt();
                }
            }
        });
        thread2.start();


        thread3 = new Thread(() -> {
            int breakTime3 = 10;
            int W3_animationTime = 3000;
            while (!thread3.isInterrupted()){
                try {
                    Thread.sleep(breakTime3);
                } catch (InterruptedException e) {
                    return;
                }
                Random random = new Random();
                if(animation_Time<W3_animationTime){
                    int ran = random.nextInt(6)+1;
                    if(ran <= 3){
                        SW1.setIcon(new ImageIcon("images/Würfel/SW_"+ran+".png"));
                    }
                    if(ran == 4){
                        SW1.setIcon(new ImageIcon("images/Würfel/SW_Bus.png"));
                    }
                    if(ran == 5 || ran ==6){
                        SW1.setIcon(new ImageIcon("images/Würfel/SW_Monop.png"));
                    }
                    breakTime3 += (int) (500*(1-Math.exp(-0.0005 * animation_Time)));
                }
                else{
                    if(Dice3 <= 3){
                        SW1.setIcon(new ImageIcon("images/Würfel/SW_"+Dice3+".png"));
                    }
                    if(Dice3 == 4){
                        SW1.setIcon(new ImageIcon("images/Würfel/SW_Bus.png"));
                    }
                    if(Dice3 == 5 || Dice3 ==6){
                        SW1.setIcon(new ImageIcon("images/Würfel/SW_Monop.png"));
                    }
                    thread3.interrupt();
                }
            }
        });
        thread3.start();
    }
}
