package monopol.screen;

import monopol.utils.JUtils;

import javax.swing.*;

public class RootPane extends JLayeredPane {
    public final PlayerPane playerPane = new PlayerPane();
    public final LobbyPane lobbyPane = new LobbyPane();
    public final MenuPane menuPane = new MenuPane();
    public final PingPane pingPane = new PingPane();

    public RootPane() {
        super();

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(true);

        add(playerPane, PALETTE_LAYER);
        add(lobbyPane, DEFAULT_LAYER);
        add(menuPane, DEFAULT_LAYER);
        add(pingPane, POPUP_LAYER);

        repaint();
    }
}
