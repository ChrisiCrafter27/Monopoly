package monopol.client.screen;

import monopol.common.utils.JUtils;

import javax.swing.*;

public class RootPane extends JLayeredPane {
    public final PlayerPane playerPane = new PlayerPane();
    public final LobbyPane lobbyPane = new LobbyPane();
    public final MenuPane menuPane = new MenuPane();
    public final PingPane pingPane = new PingPane();
    public final BoardPane boardPane = new BoardPane();
    //public final TradePane tradePane = new TradePane();
    public final SelectedCardPane selectedCardPane = new SelectedCardPane();
    public final PlayerDisplayPane playerDisplayPane = new PlayerDisplayPane();
    public final InfoPane infoPane = new InfoPane();
    public final RejoinPane rejoinPane = new RejoinPane();
    public final FreeParkingPane freeParkingPane = new FreeParkingPane();
    public final PlayerInfoPane playerInfoPane = new PlayerInfoPane();
    public final DicePane dicePane = new DicePane();
    public final HousePane housePane = new HousePane();

    public RootPane() {
        super();

        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(true);

        add(playerPane, JLayeredPane.PALETTE_LAYER);
        add(lobbyPane, JLayeredPane.DEFAULT_LAYER);
        add(menuPane, JLayeredPane.DEFAULT_LAYER);
        add(pingPane, JLayeredPane.POPUP_LAYER);
        add(boardPane, JLayeredPane.DEFAULT_LAYER);
        //add(tradePane, JLayeredPane.MODAL_LAYER);
        add(selectedCardPane, JLayeredPane.PALETTE_LAYER);
        add(playerDisplayPane, JLayeredPane.PALETTE_LAYER);
        add(infoPane, JLayeredPane.PALETTE_LAYER);
        add(rejoinPane, JLayeredPane.DRAG_LAYER);
        add(freeParkingPane, JLayeredPane.PALETTE_LAYER);
        add(playerInfoPane, JLayeredPane.PALETTE_LAYER);
        add(dicePane, JLayeredPane.PALETTE_LAYER);
        add(housePane, JLayeredPane.MODAL_LAYER);
    }
}
