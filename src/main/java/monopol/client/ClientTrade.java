package monopol.client;

import monopol.client.screen.TradePane;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.function.Supplier;

public class ClientTrade {
    public static void trade(Supplier<Client> clientSup, TradePane display) {
        try {
            trade(clientSup, clientSup.get().tradeData.tradePlayer, clientSup.get().tradeData.tradeState, display);
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
            clientSup.get().close();
        }
    }

    public static void trade(Supplier<Client> clientSup, String player2, TradeState state, TradePane display) throws RemoteException {
        /*

        Client client = clientSup.get();
        ClientPlayer clientPlayer = client.player;
        String player1 = clientPlayer.getName();

        display.reset();
        display.init(player1, player2, () -> client);

        client.tradeData.tradeState = state;

        if(state != TradeState.NULL) display.darken();

        switch (state) {
            case CHOOSE_PLAYER -> {
                //Print a list of all other players with a button to send a trade invite
                client.tradeData.offerCards.clear();
                client.tradeData.counterofferCards.clear();
                client.tradeData.offerMoney = 0;
                client.tradeData.counterOfferMoney = 0;
                client.tradeData.counterOfferSend = false;
                client.tradeData.tradePlayerConfirmed = false;
                client.tradeData.tradePlayer = null;
                display.enablePlayerButtons();
                display.enableAbortButton(1920/2-100, 1080-100);
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case WAIT_FOR_ACCEPT -> {
                //Print a waiting screen and an interrupt button for accepting
                client.tradeData.tradePlayer = player2;
                if(player2 == null) return;
                display.enableWaitText(1920/2-500, 1020/2-50);
                display.enableAbortButton(1920/2-100, 1020/2+50);
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            if(client.tradeData.tradeState != TradeState.WAIT_FOR_ACCEPT) {
                                trade(clientSup, display);
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case ACCEPT -> {
                //Print buttons to accept or decline invite
                if(player2 == null) return;
                client.tradeData.offerCards.clear();
                client.tradeData.counterofferCards.clear();
                client.tradeData.offerMoney = 0;
                client.tradeData.counterOfferMoney = 0;
                client.tradeData.counterOfferSend = false;
                client.tradeData.tradePlayerConfirmed = false;
                client.tradeData.tradePlayer = player2;
                display.enableTradeRequest();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            if(client.tradeData.tradeState != TradeState.ACCEPT) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case DECLINE -> {
                //Print the info that the other player declined your trade invite
                if(player2 == null) return;
                display.enableDeclinedInvitation();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case ABORT -> {
                //Print a screen that says that the trade was aborted
                if(player2 == null) return;
                display.enableInvitationExpired();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case IN_PROGRESS -> {
                //Print a screen that says that the target player is already trading
                if(player2 == null) return;
                display.enableAlreadyTrading();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case CHANGE_OFFER -> {
                //Print the trade offers and buttons to change offer
                if(player2 == null) return;
                display.enableOfferTexts();
                display.enableChangeOfferButtons();
                display.enableAbortButton(1920/2-100+150, 1080-150);

                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            if(client.tradeData.tradeState != TradeState.CHANGE_OFFER) {
                                if(client.tradeData.tradeState == TradeState.SEND_OFFER) client.tradeData.tradeState = TradeState.CHANGE_OFFER;
                                trade(clientSup, display);
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case SEND_OFFER -> {
                //Set the trade state to change offer
                if(player2 == null) return;
                client.tradeData.tradeState = TradeState.CHANGE_OFFER;
                trade(clientSup, display);
            }
            case CONFIRM -> {
                //Print the trade offers with an accept button
                if(player2 == null) return;
                display.enableOfferTexts();
                display.enableTradeInfo();
                display.enableAbortButton(920/2-100-300, 1080-150);
                display.enableConfirmButtons();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            if(client.tradeData.tradeState != TradeState.CONFIRM) {
                                if(client.tradeData.tradeState == TradeState.CONFIRMED) client.tradeData.tradeState = TradeState.CONFIRM;
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case WAIT_FOR_CONFIRM -> {
                //Print a waiting screen and an interrupt button for confirmation
                if(player2 == null) return;
                display.enableOfferTexts();
                display.enableTradeInfo();
                display.enableAbortButton(1920/2-100, 1020/2+50);
                display.enableWaitText(1920/2-500, 1020/2-50);

                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            if(client.tradeData.tradePlayerConfirmed) client.tradeData.tradeState = TradeState.PERFORM;
                            if(client.tradeData.tradeState != TradeState.WAIT_FOR_CONFIRM) {
                                return;
                            }
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case PERFORM -> {
                //Perform the trade
                if(player2 == null) return;
                if(client.serverMethod().trade(player1, player2, client.tradeData.offerCards, client.tradeData.counterofferCards, client.tradeData.offerMoney, client.tradeData.counterOfferMoney)) {
                    client.tradeData.tradeState = TradeState.FINISH;
                    Object[] array = new Object[2];
                    array[0] = TradeState.FINISH;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        client.close();
                    }
                } else {
                    client.tradeData.tradeState = TradeState.SERVER_FAIL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.SERVER_FAIL;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        client.close();
                    }
                }
                display.reset();
            }
            case FINISH -> {
                //Send a message to the server and print a success screen
                display.enableTradeComplete();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case SERVER_FAIL -> {
                //Print a screen that says that the trade failed because of an error on the server
                display.enableShowTradeFailed();
                display.enableOfferTexts();
                display.enableTradeInfo();
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(clientSup.get() != client) {
                                return;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
        }
        */
    }
}
