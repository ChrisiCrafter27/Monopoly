package monopol.client.screen;

import monopol.client.Client;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PlayerSelectPane extends JLayeredPane {
    private Client client;
    private ArrayList<Client> shownClients = new ArrayList<>();
    private boolean requestUpdate = false;

    public PlayerSelectPane() {
        super();

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        reset();
    }

    public void requestUpdate() {
        this.requestUpdate = true;
    }

    public void update(Client currentClient, ArrayList<Client> clients, boolean forceUpdate) {
        if(!clients.equals(shownClients) || !currentClient.equals(client) || forceUpdate || requestUpdate) {

            requestUpdate = false;

            removeAll();
            client = currentClient;
            shownClients.clear();
            shownClients.addAll(clients);

            for(int i = 0; i < clients.size(); i++) {
                int step = 1920 / clients.size();
                final int value = i;
                JButton button = JUtils.addButton(clients.get(i).player().getName(), i * step, 0, step, 60, true, (client == clients.get(value)), actionEvent -> {
                    client = clients.get(value);
                    update(client, clients, true);
                });
                step = Math.max(step, 1);
                button.setIcon(new ImageIcon(JUtils.imageIcon("images/playerselect/playerselect_" + clients.size() + ".png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
                button.setSelectedIcon(new ImageIcon(JUtils.imageIcon("images/playerselect/playerselect_" + clients.size() + "_selected.png").getImage().getScaledInstance(step, 60, Image.SCALE_SMOOTH)));
                add(button, JLayeredPane.DEFAULT_LAYER);
            }
        }

    }

    public void reset() {
        client = null;
        shownClients = new ArrayList<>();
        removeAll();
        setVisible(false);
    }

    public void init() {
        setVisible(true);
    }

    public Client getClient() {
        return client;
    }
}
