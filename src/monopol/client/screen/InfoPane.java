package monopol.client.screen;

import monopol.client.Client;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class InfoPane extends JLayeredPane {
    private final HashMap<Client, List<String>> texts = new HashMap<>();
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};

    private final JLabel text1 = JUtils.addText("", 0, 0, 350, 15, SwingConstants.LEFT);
    private final JLabel text2 = JUtils.addText("", 0, 20, 350, 15, SwingConstants.LEFT);
    private final JLabel text3 = JUtils.addText("", 0, 40, 350, 15, SwingConstants.LEFT);
    private final JLabel text4 = JUtils.addText("", 0, 60, 350, 15, SwingConstants.LEFT);
    private final JLabel text5 = JUtils.addText("", 0, 80, 350, 15, SwingConstants.LEFT);

    private final Runnable task = () -> {
        Client client = clientSup.get();
        while (!Thread.interrupted()) {
            if (clientSup.get() != client) {
                client = clientSup.get();
                if (!texts.containsKey(client)) texts.put(client, new ArrayList<>());
                updateTexts();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    };

    private Thread thread = new Thread(task);

    public InfoPane() {
        super();
        setBounds(1080/2 - 175, 400, 300, 100);
        add(text1);
        add(text2);
        add(text3);
        add(text4);
        add(text5);
        reset();
    }

    public void init(Supplier<Client> clientSup) {
        this.clientSup = clientSup;
        setVisible(true);
        thread.interrupt();
        thread = new Thread(task);
        thread.start();
    }

    public void reset() {
        setVisible(false);
        texts.clear();
        text1.setText("");
        text2.setText("");
        text3.setText("");
        text4.setText("");
        text5.setText("");
        thread.interrupt();
    }

    public void show(Client client, String text) {
        if (!texts.containsKey(client)) texts.put(client, new ArrayList<>());
        List<String> list = texts.get(client);
        list.add(text);
        if(list.size() > 5) list.remove(0);
        updateTexts();
    }

    private void updateTexts() {
        List<String> list = texts.get(clientSup.get());
        if(list == null) return;
        if(list.size() > 0) text1.setText(list.get(0));
        else text1.setText("");
        if(list.size() > 1) text2.setText(list.get(1));
        else text2.setText("");
        if(list.size() > 2) text3.setText(list.get(2));
        else text3.setText("");
        if(list.size() > 3) text4.setText(list.get(3));
        else text4.setText("");
        if(list.size() > 4) text5.setText(list.get(4));
        else text5.setText("");
    }
}
