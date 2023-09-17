package monopol.client;

import monopol.constants.IPurchasable;
import monopol.constants.Plant;
import monopol.constants.Street;
import monopol.constants.TrainStation;
import monopol.message.MessageType;
import monopol.screen.PrototypeMenu;
import monopol.server.ServerPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public class ClientEvents {
    public static void trade(PrototypeMenu menu, String player2, TradeState state) throws RemoteException {
        JFrame frame = menu.frame;
        Client client = menu.client;
        ClientPlayer clientPlayer = client.player;
        String player1 = clientPlayer.getName();

        client.tradeData.tradeState = state;

        ArrayList<JButton> buttonsToDisable = new ArrayList<>();
        for(Component component : frame.getContentPane().getComponents()) {
            if(component instanceof JButton button) {
                if(button.getY() != 0) buttonsToDisable.add(button);
            }
        }

        frame.add(menu.addImage("images/global/gray_background.png", 0, 60), 0);

        switch (state) {
            case CHOOSE_PLAYER -> {
                //Print a list of all other players with a button to send a trade invite
                client.tradeData.offerCards.removeAll(client.tradeData.offerCards);
                client.tradeData.counterofferCards.removeAll(client.tradeData.counterofferCards);
                client.tradeData.offerMoney = 0;
                client.tradeData.counterOfferMoney = 0;
                client.tradeData.counterOfferSend = false;
                client.tradeData.tradePlayerConfirmed = false;
                int i = 0;
                for(ServerPlayer serverPlayer : client.serverMethod().getServerPlayers()) {
                    if(!player1.equals(serverPlayer.getName())) {
                        frame.add(menu.addButton(serverPlayer.getName(), 1920 / 2 - 250, 200 + (75 * i), 500, 50, true, actionEvent -> {
                            try {
                                client.tradeData.tradePlayer = serverPlayer.getName();
                                client.tradeData.tradeState = TradeState.WAIT_FOR_ACCEPT;
                                Object[] array = new Object[2];
                                array[0] = TradeState.WAIT_FOR_ACCEPT;
                                array[1] = player1;
                                client.serverMethod().sendMessage(serverPlayer.getName(), MessageType.TRADE, array);
                                menu.prepareGame();
                            } catch (IOException e) {
                                client.close();
                            }
                        }), 0);
                        i += 1;
                    }
                }
                frame.add(menu.addButton("Handel abbrechen", 1920/2-100, 1080-100, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case WAIT_FOR_ACCEPT -> {
                //Print a waiting screen and an interrupt button for accepting
                client.tradeData.tradePlayer = player2;
                if(player2 == null) return;
                frame.add(menu.addText("Warte, bis " + player2 + " deine Einladungs akzeptiert", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Handel abbrechen", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client) return;
                            if(client.tradeData.tradeState != TradeState.WAIT_FOR_ACCEPT) {
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case ACCEPT -> {
                //Print buttons to accept or deny invite
                if(player2 == null) return;
                client.tradeData.offerCards.removeAll(client.tradeData.offerCards);
                client.tradeData.counterofferCards.removeAll(client.tradeData.counterofferCards);
                client.tradeData.offerMoney = 0;
                client.tradeData.counterOfferMoney = 0;
                client.tradeData.counterOfferSend = false;
                client.tradeData.tradePlayerConfirmed = false;
                client.tradeData.tradePlayer = player2;
                frame.add(menu.addText(player2 + " möchte mit dir handeln", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Angebot ablehnen", 1920/2-100-150, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.DENY;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Angebot annehmen", 1920/2-100+150, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradeState = TradeState.CHANGE_OFFER;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ACCEPT;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client) return;
                            if(client.tradeData.tradeState != TradeState.ACCEPT) {
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                }.start();
            }
            case DENY -> {
                //Print the info that the other player denied your trade invite
                if(player2 == null) return;
                frame.add(menu.addText(player2 + " hat deine Einladung abgelehnt", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case ABORT -> {
                //Print a screen that says that the trade was aborted
                if(player2 == null) return;
                frame.add(menu.addText(player2 + " hat den Handel abgebrochen", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case IN_PROGRESS -> {
                //Print a screen that says that the target player is already trading
                if(player2 == null) return;
                frame.add(menu.addText(player2 + " handelt schon", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case CHANGE_OFFER -> {
                //Print the trade offers and buttons to change offer
                updateOwner(client);

                if(player2 == null) return;
                frame.add(menu.addText("Angebot von dir", 0, 100, 1920/2, 20, true), 0);
                frame.add(menu.addText("Angebot von " + player2, 1920/2, 100, 1920/2, 20, true), 0);

                addTradeInfo(menu, client.tradeData.counterofferCards, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeButtons(menu, client, player1, 1920/4-40-40-40-15-15-15-10-10, 200);
                menu.frame.add(menu.addText(client.tradeData.counterOfferMoney + "€", 1920/4+1920/4+1920/4-100, 460, 200, 20, true), 0);
                menu.frame.add(menu.addText(client.tradeData.offerMoney + "€", 1920/4-100, 460, 200, 20, true), 0);

                menu.frame.add(menu.addButton("-1", 1920/4-50-50-50, 600, 100, 25, client.tradeData.offerMoney >= 1, actionEvent -> {
                    if(client.tradeData.offerMoney >= 1) client.tradeData.offerMoney -= 1;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+1", 1920/4-50+50+50, 600, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 1, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 1) client.tradeData.offerMoney += 1;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-5", 1920/4-50-50-50, 600+30, 100, 25, client.tradeData.offerMoney >= 5, actionEvent -> {
                    if(client.tradeData.offerMoney >= 5) client.tradeData.offerMoney -= 5;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+5", 1920/4-50+50+50, 600+30, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 5, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 5) client.tradeData.offerMoney += 5;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-10", 1920/4-50-50-50, 600+60, 100, 25, client.tradeData.offerMoney >= 10, actionEvent -> {
                    if(client.tradeData.offerMoney >= 10) client.tradeData.offerMoney -= 10;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+10", 1920/4-50+50+50, 600+60, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 10, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 10) client.tradeData.offerMoney += 10;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-20", 1920/4-50-50-50, 600+90, 100, 25, client.tradeData.offerMoney >= 20, actionEvent -> {
                    if(client.tradeData.offerMoney >= 20) client.tradeData.offerMoney -= 20;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+20", 1920/4-50+50+50, 600+90, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 20, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 20) client.tradeData.offerMoney += 20;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-50", 1920/4-50-50-50, 600+120, 100, 25, client.tradeData.offerMoney >= 50, actionEvent -> {
                    if(client.tradeData.offerMoney >= 50) client.tradeData.offerMoney -= 50;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+50", 1920/4-50+50+50, 600+120, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 50, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 50) client.tradeData.offerMoney += 50;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-100", 1920/4-50-50-50, 600+150, 100, 25, client.tradeData.offerMoney >= 100, actionEvent -> {
                    if(client.tradeData.offerMoney >= 100) client.tradeData.offerMoney -= 100;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+100", 1920/4-50+50+50, 600+150, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 100, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 100) client.tradeData.offerMoney += 100;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-500", 1920/4-50-50-50, 600+180, 100, 25, client.tradeData.offerMoney >= 500, actionEvent -> {
                    if(client.tradeData.offerMoney >= 500) client.tradeData.offerMoney -= 500;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+500", 1920/4-50+50+50, 600+180, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 500, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 500) client.tradeData.offerMoney += 500;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("-1000", 1920/4-50-50-50, 600+210, 100, 25, client.tradeData.offerMoney >= 1000, actionEvent -> {
                    if(client.tradeData.offerMoney >= 1000) client.tradeData.offerMoney -= 1000;
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);
                menu.frame.add(menu.addButton("+1000", 1920/4-50+50+50, 600+210, 100, 25, client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 1000, actionEvent -> {
                    try {
                        if(client.serverMethod().getServerPlayer(player1).getMoney() >= client.tradeData.offerMoney + 1000) client.tradeData.offerMoney += 1000;
                    } catch (RemoteException e) {
                        client.close();
                        return;
                    }
                    client.tradeData.tradeState = TradeState.SEND_OFFER;
                }), 0);

                frame.add(menu.addButton("Handel abbrechen", 1920/2-100-150, 1080-150, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Angebot absenden", 1920/2-100+150, 1080-150, 200, 50, true, actionEvent -> {
                    client.tradeData.tradeState = TradeState.CONFIRM;
                    Object[] array = new Object[4];
                    array[0] = TradeState.SEND_OFFER;
                    array[1] = player1;
                    array[2] = client.tradeData.offerCards;
                    array[3] = client.tradeData.offerMoney;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);

                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client) return;
                            if(client.tradeData.tradeState != TradeState.CHANGE_OFFER) {
                                if(client.tradeData.tradeState == TradeState.SEND_OFFER) client.tradeData.tradeState = TradeState.CHANGE_OFFER;
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
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
                menu.prepareGame();
            }
            case CONFIRM -> {
                //Print the trade offers with an accept button
                if(player2 == null) return;
                frame.add(menu.addText("Angebot von dir", 0, 100, 1920/2, 20, true), 0);
                frame.add(menu.addText("Angebot von " + player2, 1920/2, 100, 1920/2, 20, true), 0);
                addTradeInfo(menu, client.tradeData.counterofferCards, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.tradeData.offerCards, player1, 1920/4-40-40-40-15-15-15-10-10, 200);
                menu.frame.add(menu.addText(client.tradeData.counterOfferMoney + "€", 1920/4+1920/4+1920/4-100, 460, 200, 20, true), 0);
                menu.frame.add(menu.addText(client.tradeData.offerMoney + "€", 1920/4-100, 460, 200, 20, true), 0);

                frame.add(menu.addButton("Handel abbrechen", 1920/2-100-300, 1080-150, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Angebot bearbeiten", 1920/2-100, 1080-150, 200, 50, !client.tradeData.tradePlayerConfirmed, actionEvent -> {
                    client.tradeData.tradeState = TradeState.CHANGE_OFFER;
                    Object[] array = new Object[3];
                    array[0] = TradeState.CHANGE_OFFER;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Handel abschließen", 1920/2-100+300, 1080-150, 200, 50, client.tradeData.counterOfferSend, actionEvent -> {
                    if(client.tradeData.tradePlayerConfirmed) client.tradeData.tradeState = TradeState.PERFORM; else client.tradeData.tradeState = TradeState.WAIT_FOR_CONFIRM;
                    Object[] array = new Object[2];
                    array[0] = TradeState.WAIT_FOR_CONFIRM;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);

                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client) return;
                            if(client.tradeData.tradeState != TradeState.CONFIRM) {
                                if(client.tradeData.tradeState == TradeState.CONFIRMED) client.tradeData.tradeState = TradeState.CONFIRM;
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
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
                frame.add(menu.addText("Warte auf " + player2 + "...", 1920/2-500, 1020/2-50, 1000, 20, true), 0);

                addTradeInfo(menu, client.tradeData.counterofferCards, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.tradeData.offerCards, player1, 1920/4-40-40-40-15-15-15-10-10, 200);
                menu.frame.add(menu.addText(client.tradeData.counterOfferMoney + "€", 1920/4+1920/4+1920/4-100, 460, 200, 20, true), 0);
                menu.frame.add(menu.addText(client.tradeData.offerMoney + "€", 1920/4-100, 460, 200, 20, true), 0);

                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client) return;
                            if(client.tradeData.tradePlayerConfirmed) client.tradeData.tradeState = TradeState.PERFORM;
                            if(client.tradeData.tradeState != TradeState.WAIT_FOR_CONFIRM) {
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(10);
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
                        client.close();
                    }
                }
                menu.prepareGame();
            }
            case FINISH -> {
                //Send a message to the server and print a success screen
                frame.add(menu.addText("Der Handel mit " + player2 + " wurder erfolgreich abgeschlossen!", 1920/2-500, 1020/2-50, 1000, 20, true), 0);

                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case SERVER_FAIL -> {
                //Print a screen that says that the trade failed because of an error on the server
                frame.add(menu.addText("Der Handel mit " + player2 + " konnte aus einem unbekannten Grund nich abgeschlossen werden.", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addText("Bitte versuche es später erneut.", 1920/2-500, 1020/2-100, 1000, 20, true), 0);

                addTradeInfo(menu, client.tradeData.counterofferCards, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.tradeData.offerCards, player1, 1920/4-40-40-40-15-15-15-10-10, 200);
                menu.frame.add(menu.addText(client.tradeData.counterOfferMoney + "€", 1920/4+1920/4+1920/4-100, 460, 200, 20, true), 0);
                menu.frame.add(menu.addText(client.tradeData.offerMoney + "€", 1920/4-100, 460, 200, 20, true), 0);

                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradeData.tradePlayer = null;
                    client.tradeData.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
        }
        for(JButton button : buttonsToDisable) {
            button.setEnabled(false);
        }
        frame.repaint();
        System.out.println("REPAINT - ClientEvents");
    }

    public static void updateAll(Client client) {
        updateOwner(client);
        //TODO update other things
    }

    public static void updateOwner(Client client) {
        try {
            for (Map.Entry<IPurchasable, String> entry : client.serverMethod().getOwnerMap().entrySet()) {
                if(entry.getKey() instanceof Street street) street.setOwner(entry.getValue());
                else if(entry.getKey() instanceof TrainStation trainStation) trainStation.setOwner(entry.getValue());
                else if(entry.getKey() instanceof Plant plant) plant.setOwner(entry.getValue());
            }
        } catch (RemoteException e) {
            client.close();
        }
    }

    private static void addTradeButtons(PrototypeMenu menu, Client client, String name, int x, int y) {
        JFrame frame = menu.frame;
        JButton button;
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+15, y, 20, 40, Street.BADSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.BADSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.BADSTRASSE)) client.tradeData.offerCards.remove(Street.BADSTRASSE); else client.tradeData.offerCards.add(Street.BADSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+45, y, 20, 40, Street.TURMSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.TURMSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.TURMSTRASSE)) client.tradeData.offerCards.remove(Street.TURMSTRASSE); else client.tradeData.offerCards.add(Street.TURMSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+75, y, 20, 40, Street.STADIONSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.STADIONSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.STADIONSTRASSE)) client.tradeData.offerCards.remove(Street.STADIONSTRASSE); else client.tradeData.offerCards.add(Street.STADIONSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+115, y, 20, 40, Street.CHAUSSESTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.CHAUSSESTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.CHAUSSESTRASSE)) client.tradeData.offerCards.remove(Street.CHAUSSESTRASSE); else client.tradeData.offerCards.add(Street.CHAUSSESTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+145, y, 20, 40, Street.ELISENSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.ELISENSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.ELISENSTRASSE)) client.tradeData.offerCards.remove(Street.ELISENSTRASSE); else client.tradeData.offerCards.add(Street.ELISENSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+175, y, 20, 40, Street.POSTSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.POSTSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.POSTSTRASSE)) client.tradeData.offerCards.remove(Street.POSTSTRASSE); else client.tradeData.offerCards.add(Street.POSTSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+205, y, 20, 40, Street.TIERGARTENSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.TIERGARTENSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.TIERGARTENSTRASSE)) client.tradeData.offerCards.remove(Street.TIERGARTENSTRASSE); else client.tradeData.offerCards.add(Street.TIERGARTENSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+245, y, 20, 40, Street.SEESTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.SEESTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.SEESTRASSE)) client.tradeData.offerCards.remove(Street.SEESTRASSE); else client.tradeData.offerCards.add(Street.SEESTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+275, y, 20, 40, Street.HAFENSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.HAFENSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.HAFENSTRASSE)) client.tradeData.offerCards.remove(Street.HAFENSTRASSE); else client.tradeData.offerCards.add(Street.HAFENSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+305, y, 20, 40, Street.NEUESTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.NEUESTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.NEUESTRASSE)) client.tradeData.offerCards.remove(Street.NEUESTRASSE); else client.tradeData.offerCards.add(Street.NEUESTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+335, y, 20, 40, Street.MARKTPLATZ.getOwner().equals(name), client.tradeData.offerCards.contains(Street.MARKTPLATZ), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.MARKTPLATZ)) client.tradeData.offerCards.remove(Street.MARKTPLATZ); else client.tradeData.offerCards.add(Street.MARKTPLATZ);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x, y+50, 20, 40, Street.MUENCHENERSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.MUENCHENERSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.MUENCHENERSTRASSE)) client.tradeData.offerCards.remove(Street.MUENCHENERSTRASSE); else client.tradeData.offerCards.add(Street.MUENCHENERSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+30, y+50, 20, 40, Street.WIENERSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.WIENERSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.WIENERSTRASSE)) client.tradeData.offerCards.remove(Street.WIENERSTRASSE); else client.tradeData.offerCards.add(Street.WIENERSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+60, y+50, 20, 40, Street.BERLINERSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.BERLINERSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.BERLINERSTRASSE)) client.tradeData.offerCards.remove(Street.BERLINERSTRASSE); else client.tradeData.offerCards.add(Street.BERLINERSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+90, y+50, 20, 40, Street.HAMBURGERSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.HAMBURGERSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.HAMBURGERSTRASSE)) client.tradeData.offerCards.remove(Street.HAMBURGERSTRASSE); else client.tradeData.offerCards.add(Street.HAMBURGERSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+130, y+50, 20, 40, Street.THEATERSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.THEATERSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.THEATERSTRASSE)) client.tradeData.offerCards.remove(Street.THEATERSTRASSE); else client.tradeData.offerCards.add(Street.THEATERSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+160, y+50, 20, 40, Street.MUSEUMSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.MUSEUMSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.MUSEUMSTRASSE)) client.tradeData.offerCards.remove(Street.MUSEUMSTRASSE); else client.tradeData.offerCards.add(Street.MUSEUMSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+190, y+50, 20, 40, Street.OPERNPLATZ.getOwner().equals(name), client.tradeData.offerCards.contains(Street.OPERNPLATZ), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.OPERNPLATZ)) client.tradeData.offerCards.remove(Street.OPERNPLATZ); else client.tradeData.offerCards.add(Street.OPERNPLATZ);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+220, y+50, 20, 40, Street.KONZERTHAUSSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.KONZERTHAUSSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.KONZERTHAUSSTRASSE)) client.tradeData.offerCards.remove(Street.KONZERTHAUSSTRASSE); else client.tradeData.offerCards.add(Street.KONZERTHAUSSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+260, y+50, 20, 40, Street.LESSINGSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.LESSINGSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.LESSINGSTRASSE)) client.tradeData.offerCards.remove(Street.LESSINGSTRASSE); else client.tradeData.offerCards.add(Street.LESSINGSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+290, y+50, 20, 40, Street.SCHILLERSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.SCHILLERSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.SCHILLERSTRASSE)) client.tradeData.offerCards.remove(Street.SCHILLERSTRASSE); else client.tradeData.offerCards.add(Street.SCHILLERSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+320, y+50, 20, 40, Street.GOETHESTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.GOETHESTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.GOETHESTRASSE)) client.tradeData.offerCards.remove(Street.GOETHESTRASSE); else client.tradeData.offerCards.add(Street.GOETHESTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+350, y+50, 20, 40, Street.RILKESTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.RILKESTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.RILKESTRASSE)) client.tradeData.offerCards.remove(Street.RILKESTRASSE); else client.tradeData.offerCards.add(Street.RILKESTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+80, y+100, 20, 40, Street.RATHAUSPLATZ.getOwner().equals(name), client.tradeData.offerCards.contains(Street.RATHAUSPLATZ), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.RATHAUSPLATZ)) client.tradeData.offerCards.remove(Street.RATHAUSPLATZ); else client.tradeData.offerCards.add(Street.RATHAUSPLATZ);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+110, y+100, 20, 40, Street.HAUPSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.HAUPSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.HAUPSTRASSE)) client.tradeData.offerCards.remove(Street.HAUPSTRASSE); else client.tradeData.offerCards.add(Street.HAUPSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+140, y+100, 20, 40, Street.BOERSENPLATZ.getOwner().equals(name), client.tradeData.offerCards.contains(Street.BOERSENPLATZ), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.BOERSENPLATZ)) client.tradeData.offerCards.remove(Street.BOERSENPLATZ); else client.tradeData.offerCards.add(Street.BOERSENPLATZ);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+170, y+100, 20, 40, Street.BAHNHOFSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.BAHNHOFSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.BAHNHOFSTRASSE)) client.tradeData.offerCards.remove(Street.BAHNHOFSTRASSE); else client.tradeData.offerCards.add(Street.BAHNHOFSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+210, y+100, 20, 40, Street.DOMPLATZ.getOwner().equals(name), client.tradeData.offerCards.contains(Street.DOMPLATZ), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.DOMPLATZ)) client.tradeData.offerCards.remove(Street.DOMPLATZ); else client.tradeData.offerCards.add(Street.DOMPLATZ);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+240, y+100, 20, 40, Street.PARKSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.PARKSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.PARKSTRASSE)) client.tradeData.offerCards.remove(Street.PARKSTRASSE); else client.tradeData.offerCards.add(Street.PARKSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+270, y+100, 20, 40, Street.SCHLOSSALLEE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.SCHLOSSALLEE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.SCHLOSSALLEE)) client.tradeData.offerCards.remove(Street.SCHLOSSALLEE); else client.tradeData.offerCards.add(Street.SCHLOSSALLEE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+80, y+150, 20, 40, TrainStation.SUEDBAHNHOF.getOwner().equals(name), client.tradeData.offerCards.contains(TrainStation.SUEDBAHNHOF), actionEvent -> {
            if(client.tradeData.offerCards.contains(TrainStation.SUEDBAHNHOF)) client.tradeData.offerCards.remove(TrainStation.SUEDBAHNHOF); else client.tradeData.offerCards.add(TrainStation.SUEDBAHNHOF);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+110, y+150, 20, 40, TrainStation.WESTBAHNHOF.getOwner().equals(name), client.tradeData.offerCards.contains(TrainStation.WESTBAHNHOF), actionEvent -> {
            if(client.tradeData.offerCards.contains(TrainStation.WESTBAHNHOF)) client.tradeData.offerCards.remove(TrainStation.WESTBAHNHOF); else client.tradeData.offerCards.add(TrainStation.WESTBAHNHOF);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+140, y+150, 20, 40, TrainStation.NORDBAHNHOF.getOwner().equals(name), client.tradeData.offerCards.contains(TrainStation.NORDBAHNHOF), actionEvent -> {
            if(client.tradeData.offerCards.contains(TrainStation.NORDBAHNHOF)) client.tradeData.offerCards.remove(TrainStation.NORDBAHNHOF); else client.tradeData.offerCards.add(TrainStation.NORDBAHNHOF);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+170, y+150, 20, 40, TrainStation.HAUPTBAHNHOF.getOwner().equals(name), client.tradeData.offerCards.contains(TrainStation.HAUPTBAHNHOF), actionEvent -> {
            if(client.tradeData.offerCards.contains(TrainStation.HAUPTBAHNHOF)) client.tradeData.offerCards.remove(TrainStation.HAUPTBAHNHOF); else client.tradeData.offerCards.add(TrainStation.HAUPTBAHNHOF);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "gas" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "gas" + ".png", x+210, y+150, 20, 40, Plant.GASWERK.getOwner().equals(name), client.tradeData.offerCards.contains(Plant.GASWERK), actionEvent -> {
            if(client.tradeData.offerCards.contains(Plant.GASWERK)) client.tradeData.offerCards.remove(Plant.GASWERK); else client.tradeData.offerCards.add(Plant.GASWERK);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "elec" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "elec" + ".png", x+240, y+150, 20, 40, Plant.ELEKTRIZITAETSWERK.getOwner().equals(name), client.tradeData.offerCards.contains(Plant.ELEKTRIZITAETSWERK), actionEvent -> {
            if(client.tradeData.offerCards.contains(Plant.ELEKTRIZITAETSWERK)) client.tradeData.offerCards.remove(Plant.ELEKTRIZITAETSWERK); else client.tradeData.offerCards.add(Plant.ELEKTRIZITAETSWERK);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "water" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "water" + ".png", x+270, y+150, 20, 40, Plant.WASSERWERK.getOwner().equals(name), client.tradeData.offerCards.contains(Plant.WASSERWERK), actionEvent -> {
            if(client.tradeData.offerCards.contains(Plant.WASSERWERK)) client.tradeData.offerCards.remove(Plant.WASSERWERK); else client.tradeData.offerCards.add(Plant.WASSERWERK);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);
    }
    private static void addTradeInfo(PrototypeMenu menu, ArrayList<IPurchasable> tradeItems, String name, int x, int y) {
        JFrame frame = menu.frame;

        frame.add(menu.addImage(Street.BADSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BADSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+15, y), 0);
        frame.add(menu.addImage(Street.TURMSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.TURMSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+45, y), 0);
        frame.add(menu.addImage(Street.STADIONSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.STADIONSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+75, y), 0);
        frame.add(menu.addImage(Street.CHAUSSESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.CHAUSSESTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+115, y), 0);
        frame.add(menu.addImage(Street.ELISENSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.ELISENSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+145, y), 0);
        frame.add(menu.addImage(Street.POSTSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.POSTSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+175, y), 0);
        frame.add(menu.addImage(Street.TIERGARTENSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.TIERGARTENSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+205, y), 0);
        frame.add(menu.addImage(Street.SEESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.SEESTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+245, y), 0);
        frame.add(menu.addImage(Street.HAFENSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAFENSTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+275, y), 0);
        frame.add(menu.addImage(Street.NEUESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.NEUESTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+305, y), 0);
        frame.add(menu.addImage(Street.MARKTPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.MARKTPLATZ) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+335, y), 0);

        frame.add(menu.addImage(Street.MUENCHENERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.MUENCHENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x, y+50), 0);
        frame.add(menu.addImage(Street.WIENERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.WIENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+30, y+50), 0);
        frame.add(menu.addImage(Street.BERLINERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BERLINERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+60, y+50), 0);
        frame.add(menu.addImage(Street.HAMBURGERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAMBURGERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+90, y+50), 0);
        frame.add(menu.addImage(Street.THEATERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.THEATERSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+130, y+50), 0);
        frame.add(menu.addImage(Street.MUSEUMSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.MUSEUMSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+160, y+50), 0);
        frame.add(menu.addImage(Street.OPERNPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.OPERNPLATZ) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+190, y+50), 0);
        frame.add(menu.addImage(Street.KONZERTHAUSSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.KONZERTHAUSSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+220, y+50), 0);
        frame.add(menu.addImage(Street.LESSINGSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.LESSINGSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+260, y+50), 0);
        frame.add(menu.addImage(Street.SCHILLERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.SCHILLERSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+290, y+50), 0);
        frame.add(menu.addImage(Street.GOETHESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.GOETHESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+320, y+50), 0);
        frame.add(menu.addImage(Street.RILKESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.RILKESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+350, y+50), 0);

        frame.add(menu.addImage(Street.RATHAUSPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.RATHAUSPLATZ) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+80, y+100), 0);
        frame.add(menu.addImage(Street.HAUPSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAUPSTRASSE) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+110, y+100), 0);
        frame.add(menu.addImage(Street.BOERSENPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.BOERSENPLATZ) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+140, y+100), 0);
        frame.add(menu.addImage(Street.BAHNHOFSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BAHNHOFSTRASSE) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+170, y+100), 0);
        frame.add(menu.addImage(Street.DOMPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.DOMPLATZ) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+210, y+100), 0);
        frame.add(menu.addImage(Street.PARKSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.PARKSTRASSE) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+240, y+100), 0);
        frame.add(menu.addImage(Street.SCHLOSSALLEE.getOwner().equals(name) ? tradeItems.contains(Street.SCHLOSSALLEE) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+270, y+100), 0);

        frame.add(menu.addImage(TrainStation.SUEDBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.SUEDBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+80, y+150), 0);
        frame.add(menu.addImage(TrainStation.WESTBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.WESTBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+110, y+150), 0);
        frame.add(menu.addImage(TrainStation.NORDBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.NORDBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+140, y+150), 0);
        frame.add(menu.addImage(TrainStation.HAUPTBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.HAUPTBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+170, y+150), 0);
        frame.add(menu.addImage(Plant.GASWERK.getOwner().equals(name) ? tradeItems.contains(Plant.GASWERK) ? "images/kleine_karten/gas_filled.png" : "images/kleine_karten/gas.png" : "images/kleine_karten/disabled.png", x+210, y+150), 0);
        frame.add(menu.addImage(Plant.ELEKTRIZITAETSWERK.getOwner().equals(name) ? tradeItems.contains(Plant.ELEKTRIZITAETSWERK) ? "images/kleine_karten/elec_filled.png" : "images/kleine_karten/elec.png" : "images/kleine_karten/disabled.png", x+240, y+150), 0);
        frame.add(menu.addImage(Plant.WASSERWERK.getOwner().equals(name) ? tradeItems.contains(Plant.WASSERWERK) ? "images/kleine_karten/water_filled.png" : "images/kleine_karten/water.png" : "images/kleine_karten/disabled.png", x+270, y+150), 0);
    }
}
