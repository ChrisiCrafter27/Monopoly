package monopol.client.screen;

import monopol.common.utils.JUtils;

import javax.swing.*;

public class RootPane extends JLayeredPane {
    public final PlayerPane playerPane = new PlayerPane();
    public final LobbyPane lobbyPane = new LobbyPane();
    public final MenuPane menuPane = new MenuPane();
    public final PingPane pingPane = new PingPane();
    public final BoardPane boardPane = new BoardPane();
    public final TradePane tradePane = new TradePane();
    public final SelectedCardPane selectedCardPane = new SelectedCardPane();
    public final PlayerDisplayPane playerDisplayPane = new PlayerDisplayPane();
    public final InfoPane infoPane = new InfoPane();
    public final RejoinPane rejoinPane = new RejoinPane();
    public final FreeParkingPane freeParkingPane = new FreeParkingPane();
    public final PlayerInfoPane playerInfoPane = new PlayerInfoPane();
    public final DicePane dicePane = new DicePane();

    public RootPane() {
        super();

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(true);

        add(playerPane, PALETTE_LAYER);
        add(lobbyPane, DEFAULT_LAYER);
        add(menuPane, DEFAULT_LAYER);
        add(pingPane, POPUP_LAYER);
        add(boardPane, DEFAULT_LAYER);
        add(tradePane, MODAL_LAYER);
        add(selectedCardPane, PALETTE_LAYER);
        add(playerDisplayPane, PALETTE_LAYER);
        add(infoPane, PALETTE_LAYER);
        add(rejoinPane, DRAG_LAYER);
        add(freeParkingPane, PALETTE_LAYER);
        add(playerInfoPane, PALETTE_LAYER);
        add(dicePane, PALETTE_LAYER);

        repaint();
    }
}
