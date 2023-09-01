package monopol.core;

import monopol.screen.GameWindow;
import monopol.screen.PrototypeMenu;
import monopol.server.Server;
import monopol.server.ServerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Monopoly {

    public static final Monopoly INSTANCE = new Monopoly();
    private final Server server;
    GameState state;

    private Monopoly() {
        try {
            server = new Server(25565);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setState(GameState gameState) {
        state = gameState;
    }
    public GameState getState() {
        return state;
    }
    public void openServer(ServerSettings settings) {
        server.open(settings);
    }
    public void closeServer() {
        server.close();
    }

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
        //menuThread.start();
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }
}