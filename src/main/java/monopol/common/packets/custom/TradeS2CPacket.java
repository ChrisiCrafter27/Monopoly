package monopol.common.packets.custom;

import monopol.client.Client;
import monopol.client.screen.RootPane;
import monopol.common.data.DataReader;
import monopol.common.data.DataWriter;
import monopol.common.message.DisconnectReason;
import monopol.common.packets.S2CPacket;

import javax.swing.*;

public class TradeS2CPacket extends S2CPacket<TradeS2CPacket> {

    public TradeS2CPacket() {

    }

    @Override
    public void serialize(DataWriter writer) {}

    @SuppressWarnings("unused")
    public static TradeS2CPacket deserialize(DataReader reader) {
        return new TradeS2CPacket();
    }

    @Override
    public void handleOnClient(Client client, RootPane display) {
        /*
        TradeState state = TradeState.valueOf((String) message.getMessage()[0]);
        switch (state) {
            case ABORT -> {
                if(tradeData.tradeState != TradeState.NULL && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.tradeState = TradeState.ABORT;
                }
            }
            case ACCEPT -> {
                if(tradeData.tradeState == TradeState.WAIT_FOR_ACCEPT && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.tradeState = TradeState.CHANGE_OFFER;
                }
            }
            case CHANGE_OFFER -> {
                if(tradeData.tradeState != TradeState.NULL && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.counterOfferSend = false;
                }
            }
            case CONFIRM -> {
                if((tradeData.tradeState == TradeState.CONFIRM || tradeData.tradeState == TradeState.WAIT_FOR_CONFIRM) && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.tradePlayerConfirmed = true;
                    tradeData.tradeState = TradeState.CONFIRMED;
                }
            }
            case DECLINE -> {
                if(tradeData.tradeState != TradeState.NULL && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.tradeState = TradeState.DECLINE;
                }
            }
            case FINISH -> {
                if(tradeData.tradeState == TradeState.WAIT_FOR_CONFIRM && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.tradeState = TradeState.FINISH;
                }
            }
            case IN_PROGRESS -> {
                if(tradeData.tradeState != TradeState.NULL && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                    tradeData.tradeState = TradeState.IN_PROGRESS;
                }
            }
            case NULL -> {
                if(tradeData.tradeState != TradeState.NULL) {
                    tradeData.tradeState = TradeState.NULL;
                    tradeData.tradePlayer = null;
                }
            }
            case SEND_OFFER -> {
                if((tradeData.tradeState == TradeState.CHANGE_OFFER || tradeData.tradeState == TradeState.SEND_OFFER || tradeData.tradeState == TradeState.CONFIRM) && tradeData.tradePlayer.equals(message.getMessage()[1])) {

                    tradeData.counterofferCards.removeAll(tradeData.counterofferCards);
                    for(String string : (ArrayList<String>) message.getMessage()[2]) {
                        try {
                            tradeData.counterofferCards.add(Street.valueOf(string));
                        } catch (Exception ignored) {}
                        try {
                            tradeData.counterofferCards.add(TrainStation.valueOf(string));
                        } catch (Exception ignored) {}
                        try {
                            tradeData.counterofferCards.add(Plant.valueOf(string));
                        } catch (Exception ignored) {}
                    }

                    tradeData.counterOfferMoney = (int) message.getMessage()[3];

                    tradeData.counterOfferSend = true;
                    if(tradeData.tradeState != TradeState.CONFIRM) tradeData.tradeState = TradeState.SEND_OFFER;
                }
            }
            case SERVER_FAIL -> {
                if(tradeData.tradeState != TradeState.NULL TODO other conditions) {
                    tradeData.tradeState = TradeState.SERVER_FAIL;
                }
            }
            case WAIT_FOR_CONFIRM -> {
                tradeData.tradePlayerConfirmed = true;
            }
            case WAIT_FOR_ACCEPT -> {
                if(tradeData.tradeState == TradeState.NULL) {
                    tradeData.tradeState = TradeState.ACCEPT;
                    tradeData.tradePlayer = (String) message.getMessage()[1];
                } else {
                    Object[] array = new Object[2];
                    array[0] = TradeState.IN_PROGRESS;
                    array[1] = player.getName();
                    serverMethod().sendMessage((String) message.getMessage()[1], MessageType.TRADE, array);
                }
            }
        }
        */
    }
}
