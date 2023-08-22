package monopol.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.Key;
import java.util.ArrayList;

public class KeyHandler implements KeyListener {

    private final ArrayList<Integer> pressedKeys = new ArrayList<>();
    private String string = "";

    @Override
    public void keyTyped(KeyEvent e) {
        string += e.getKeyChar();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!pressedKeys.contains(e.getKeyCode())) {
            pressedKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove((Object) e.getKeyCode());
    }

    public boolean isKeyPressed(int code) {
        return pressedKeys.contains(code);
    }

    public void resetString() {
        string = "";
    }

    public String getString() {
        return string;
    }
}
