package monopol.client;

import monopol.constants.IPurchasable;

import java.util.ArrayList;

public class TradeData {
    public TradeState tradeState = TradeState.NULL;
    public String tradePlayer;
    public boolean counterOfferSend;
    public boolean tradePlayerConfirmed;
    public final ArrayList<IPurchasable> offerCards = new ArrayList<>();
    public final ArrayList<IPurchasable> counterofferCards = new ArrayList<>();
    public int offerMoney = 0;
    public int counterOfferMoney = 0;
}
