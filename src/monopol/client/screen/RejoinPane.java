package monopol.client.screen;

import monopol.client.Client;
import monopol.common.core.Monopoly;
import monopol.common.utils.JUtils;
import monopol.server.DisconnectReason;

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Supplier;

public class RejoinPane extends JLayeredPane {
    private final JLayeredPane players = new JLayeredPane();
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};

    public RejoinPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        add(JUtils.addButton(new JButton(), null, "images/global/gray_background.png", 0, 60, 1920, 1020, true, false, actionEvent -> {}), DEFAULT_LAYER);
        add(JUtils.addText("Warte auf andere Spieler:", 1920 / 2 - 300, 200, 600, 40, SwingConstants.CENTER), PALETTE_LAYER);

        players.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(players, PALETTE_LAYER);

        reset();
    }

    public void reset() {
        players.removeAll();
        setVisible(false);
    }

    public void init(Supplier<Client> clientSup) {
        this.clientSup = clientSup;
    }

    public void setList(List<String> names) {
        reset();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            players.add(JUtils.addButton(name, 1920 / 2 - 100, 300 + i * 50, 200, 30, true, actionEvent -> {
                if(clientSup.get() != null && clientSup.get().player.isHost) {
                    if(JOptionPane.showConfirmDialog(null, name + " entfernen?", "Spieler entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) Monopoly.INSTANCE.server().remove(name);
                }
            }));
        }
        setVisible(true);
    }
}
