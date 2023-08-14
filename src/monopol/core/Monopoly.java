package monopol.core;

import monopol.screen.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Monopoly {

    public static final Monopoly INSTANCE = new Monopoly();

    GameState state;

    public static void main(String[] args) {
        INSTANCE.state = GameState.MAIN_MENU;
        Thread menuThread = new Thread() {
            @Override
            public void run() {
                GameWindow Monopoly = new GameWindow();
                JFrame frame = Monopoly.getframe();
                Monopoly.Mainmenu();
                frame.setVisible(true);



                while(!interrupted()) {


                    try {
                        sleep(10);
                    } catch (InterruptedException ignored) {}
                }
            }
        };
        menuThread.start();
    }

    public void setState(GameState gameState) {
        state = gameState;
    }

    public GameState getState() {
        return state;
    }
}