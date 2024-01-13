package monopol.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class KeyHandler implements KeyListener {

    private final ArrayList<Integer> pressedKeys = new ArrayList<>();
    private String string = "";

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if(!pressedKeys.contains(e.getKeyCode())) pressedKeys.add(e.getKeyCode());
        if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!string.isEmpty()) {
                string = string.substring(0, string.length() - 1);
            }
        } else if(e.getKeyChar() != '￿') string += e.getKeyChar();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove((Integer) e.getKeyCode());
    }

    public boolean isKeyDown(int code) {
        return pressedKeys.contains(code);
    }

    public void resetString() {
        string = "";
    }

    public String getString() {
        return string;
    }
}
