package monopol.client.screen;

import monopol.client.Client;
import monopol.common.core.Monopoly;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RejoinPane extends JLayeredPane {
    private final JLayeredPane players = new JLayeredPane();
    private Supplier<Client> clientSup = () -> {throw new IllegalStateException("init() was not called");};
    private Consumer<Client> rejoin = client -> {};

    public RejoinPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        add(JUtils.addButton(new JButton(), null, "images/global/gray_background.png", 0, 60, 1920, 1020, true, false, actionEvent -> {}), JLayeredPane.DEFAULT_LAYER);
        add(JUtils.addText("Warte auf andere Spieler:", 1920 / 2 - 300, 200, 600, 40, SwingConstants.CENTER), JLayeredPane.PALETTE_LAYER);

        players.setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        add(players, JLayeredPane.PALETTE_LAYER);

        reset();
    }

    public void reset() {
        players.removeAll();
        setVisible(false);
    }

    public void init(Supplier<Client> clientSup, Consumer<Client> rejoin) {
        this.clientSup = clientSup;
        this.rejoin = rejoin;
    }

    public void setList(List<String> names, RootPane display) {
        reset();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            players.add(JUtils.addButton(name, 1920 / 2 - 100, 300 + i * 50, 200, 30, true, actionEvent -> {
                Object[] options;
                if(clientSup.get() != null && clientSup.get().player().isHost) {
                    options = new Object[]{"Abbrechen", "Beitreten", "Entfernen"};
                } else options = new Object[]{"Abbrechen", "Beitreten"};
                int result = JOptionPane.showOptionDialog(Monopoly.INSTANCE.parentComponent, "Als " + name + " erneut beitreten?", "Erneut beitreten", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
                if(result == 1) {
                    try {
                        Client client = new Client((Inet4Address) Inet4Address.getByName(clientSup.get().serverMethod().getIp()), Monopoly.INSTANCE.serverProperties(), false, display, name);
                        rejoin.accept(client);
                    } catch (NotBoundException | RemoteException | UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if(result == 2) Monopoly.INSTANCE.server().remove(name);
            }));
        }
        setVisible(true);
    }
}
