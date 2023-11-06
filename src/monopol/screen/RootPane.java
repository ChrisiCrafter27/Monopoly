package monopol.screen;

import monopol.utils.JUtils;

import javax.swing.*;

public class RootPane extends JLayeredPane {
    public final PlayerPane playerPane = new PlayerPane();
    public final LobbyPane lobbyPane = new LobbyPane();
    public final MenuPane menuPane = new MenuPane();

    public RootPane() {
        super();

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(true);

        add(playerPane, POPUP_LAYER);
        add(lobbyPane, PALETTE_LAYER);
        add(menuPane, PALETTE_LAYER);

        repaint();
    }
}
