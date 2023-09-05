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

public class ClientEvents {
    public static void trade(PrototypeMenu menu, String player2, TradeState state) throws RemoteException {
        JFrame frame = menu.frame;
        Client client = menu.client;
        ClientPlayer clientPlayer = client.player;
        String player1 = clientPlayer.getName();

        System.out.println(player1 + ": " + client.tradeState);

        client.tradeState = state;

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
                client.offer.removeAll(client.offer);
                client.counteroffer.removeAll(client.counteroffer);
                client.counterOfferSend = false;
                client.tradePlayerConfirmed = false;
                int i = 0;
                for(ServerPlayer serverPlayer : client.serverMethod().getServerPlayers()) {
                    if(!player1.equals(serverPlayer.getName())) {
                        frame.add(menu.addButton(serverPlayer.getName(), 1920 / 2 - 250, 200 + (75 * i), 500, 50, true, actionEvent -> {
                            try {
                                client.tradePlayer = serverPlayer.getName();
                                client.tradeState = TradeState.WAIT_FOR_ACCEPT;
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
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case WAIT_FOR_ACCEPT -> {
                //Print a waiting screen and an interrupt button for accepting
                client.tradePlayer = player2;
                if(player2 == null) return;
                frame.add(menu.addText("Warte, bis " + player2 + " deine Einladungs akzeptiert", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Handel abbrechen", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
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
                            if(client.tradeState != TradeState.WAIT_FOR_ACCEPT) {
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }.start();
            }
            case ACCEPT -> {
                //Print buttons to accept or deny invite
                if(player2 == null) return;
                client.offer.removeAll(client.offer);
                client.counteroffer.removeAll(client.counteroffer);
                client.counterOfferSend = false;
                client.tradePlayerConfirmed = false;
                client.tradePlayer = player2;
                frame.add(menu.addText(player2 + " möchte mit dir handeln", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Angebot ablehnen", 1920/2-100-150, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
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
                    client.tradeState = TradeState.CHANGE_OFFER;
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
                            if(client.tradeState != TradeState.ACCEPT) {
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }.start();
            }
            case DENY -> {
                //Print the info that the other player denied your trade invite
                if(player2 == null) return;
                frame.add(menu.addText(player2 + " hat deine Einladung abgelehnt", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case ABORT -> {
                if(player2 == null) return;
                frame.add(menu.addText(player2 + " hat den Handel abgebrochen", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case IN_PROGRESS -> {
                if(player2 == null) return;
                frame.add(menu.addText(player2 + " handelt schon", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case CHANGE_OFFER -> {
                //Print the trade offers and buttons to change offer
                updateOwner(client);

                if(player2 == null) return;
                frame.add(menu.addText("Angebot von dir", 0, 100, 1920/2, 20, true), 0);
                frame.add(menu.addText("Angebot von " + player2, 1920/2, 100, 1920/2, 20, true), 0);

                addTradeInfo(menu, client.counteroffer, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeButtons(menu, client, player1, 1920/4-40-40-40-15-15-15-10-10, 200);

                frame.add(menu.addButton("Handel abbrechen", 1920/2-100, 1080-225, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Angebot absenden", 1920/2-100, 1080-150, 200, 50, true, actionEvent -> {
                    client.tradeState = TradeState.CONFIRM;
                    Object[] array = new Object[3];
                    array[0] = TradeState.SEND_OFFER;
                    array[1] = player1;
                    array[2] = client.offer;
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
                            if(client.tradeState != TradeState.CHANGE_OFFER) {
                                if(client.tradeState == TradeState.SEND_OFFER) client.tradeState = TradeState.CHANGE_OFFER;
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }.start();
            }
            case SEND_OFFER -> {
                if(player2 == null) return;
                client.tradeState = TradeState.CHANGE_OFFER;
                menu.prepareGame();
            }
            case CONFIRM -> {
                //Print the trade offers with an accept button
                if(player2 == null) return;
                frame.add(menu.addText("Angebot von dir", 0, 100, 1920/2, 20, true), 0);
                frame.add(menu.addText("Angebot von " + player2, 1920/2, 100, 1920/2, 20, true), 0);
                addTradeInfo(menu, client.counteroffer, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.offer, player1, 1920/4-40-40-40-15-15-15-10-10, 200);

                frame.add(menu.addButton("Handel abbrechen", 1920/2-175, 1080-150, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Angebot bearbeiten", 1920/2-100, 1080-150, 200, 50, !client.tradePlayerConfirmed, actionEvent -> {
                    client.tradeState = TradeState.CHANGE_OFFER;
                    Object[] array = new Object[3];
                    array[0] = TradeState.CHANGE_OFFER;
                    array[1] = player1;
                    try {
                        client.serverMethod().sendMessage(player2, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client.close();
                    }
                }), 0);
                frame.add(menu.addButton("Handel abschließen", 1920/2-175, 1080-225, 550, 50, client.counterOfferSend, actionEvent -> {
                    client.tradeState = TradeState.WAIT_FOR_CONFIRM;
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
                            if(client.tradeState != TradeState.CONFIRM) {
                                if(client.tradeState == TradeState.CONFIRMED) client.tradeState = TradeState.CONFIRM;
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(100);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }.start();
            }
            case CONFIRMED -> {
                if(player2 == null) return;
                client.tradeState = TradeState.CONFIRM;
                menu.prepareGame();
            }
            case WAIT_FOR_CONFIRM -> {
                //Print a waiting screen and an interrupt button for confirmation
                if(player2 == null) return;
                frame.add(menu.addText("Warte auf " + player2 + "...", 1920/2-500, 1020/2-50, 1000, 20, true), 0);

                addTradeInfo(menu, client.counteroffer, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.offer, player1, 1920/4-40-40-40-15-15-15-10-10, 200);

                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client) return;
                            if(client.tradeState != TradeState.WAIT_FOR_CONFIRM) {
                                if(client.tradeState == TradeState.CONFIRMED) client.tradeState = TradeState.FINISH;
                                menu.prepareGame();
                                return;
                            }
                            try {
                                sleep(10);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }.start();
            }
            case FINISH -> {
                //Send a message to the server and print a success screen
                frame.add(menu.addText("Der Handel mit " + player2 + " wurder erfolgreich abgeschlossen!", 1920/2-500, 1020/2-50, 1000, 20, true), 0);

                addTradeInfo(menu, client.counteroffer, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.offer, player1, 1920/4-40-40-40-15-15-15-10-10, 200);

                //TODO send a message to the server to perform the trade

                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case SERVER_FAIL -> {
                frame.add(menu.addText("Der Handel mit " + player2 + " konnte aus einem unbekannten Grund nich abgeschlossen werden.", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addText("Bitte versuche es später erneut.", 1920/2-500, 1020/2-100, 1000, 20, true), 0);

                addTradeInfo(menu, client.counteroffer, player2, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeInfo(menu, client.offer, player1, 1920/4-40-40-40-15-15-15-10-10, 200);

                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client.tradePlayer = null;
                    client.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
        }
        for(JButton button : buttonsToDisable) {
            button.setEnabled(false);
        }
        frame.repaint();
    }

    //TODO Owner sync between client and server

    public static void updateAll(Client client) {
        updateOwner(client);
        //TODO update other things
    }

    public static void updateOwner(Client client) {
        //TODO do it
    }

    private static void addTradeButtons(PrototypeMenu menu, Client client, String name, int x, int y) {
        JFrame frame = menu.frame;
        JButton button;
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+15, y, 20, 40, Street.BADSTRASSE.getOwner().equals(name), client.offer.contains(Street.BADSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.BADSTRASSE)) client.offer.remove(Street.BADSTRASSE); else client.offer.add(Street.BADSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+45, y, 20, 40, Street.TURMSTRASSE.getOwner().equals(name), client.offer.contains(Street.TURMSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.TURMSTRASSE)) client.offer.remove(Street.TURMSTRASSE); else client.offer.add(Street.TURMSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+75, y, 20, 40, Street.STADIONSTRASSE.getOwner().equals(name), client.offer.contains(Street.STADIONSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.STADIONSTRASSE)) client.offer.remove(Street.STADIONSTRASSE); else client.offer.add(Street.STADIONSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+115, y, 20, 40, Street.CHAUSSESTRASSE.getOwner().equals(name), client.offer.contains(Street.CHAUSSESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.CHAUSSESTRASSE)) client.offer.remove(Street.CHAUSSESTRASSE); else client.offer.add(Street.CHAUSSESTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+145, y, 20, 40, Street.ELISENSTRASSE.getOwner().equals(name), client.offer.contains(Street.ELISENSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.ELISENSTRASSE)) client.offer.remove(Street.ELISENSTRASSE); else client.offer.add(Street.ELISENSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+175, y, 20, 40, Street.POSTSTRASSE.getOwner().equals(name), client.offer.contains(Street.POSTSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.POSTSTRASSE)) client.offer.remove(Street.POSTSTRASSE); else client.offer.add(Street.POSTSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+205, y, 20, 40, Street.TIERGARTENSTRASSE.getOwner().equals(name), client.offer.contains(Street.TIERGARTENSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.TIERGARTENSTRASSE)) client.offer.remove(Street.TIERGARTENSTRASSE); else client.offer.add(Street.TIERGARTENSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+245, y, 20, 40, Street.SEESTRASSE.getOwner().equals(name), client.offer.contains(Street.SEESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.SEESTRASSE)) client.offer.remove(Street.SEESTRASSE); else client.offer.add(Street.SEESTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+275, y, 20, 40, Street.HAFENSTRASSE.getOwner().equals(name), client.offer.contains(Street.HAFENSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.HAFENSTRASSE)) client.offer.remove(Street.HAFENSTRASSE); else client.offer.add(Street.HAFENSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+305, y, 20, 40, Street.NEUESTRASSE.getOwner().equals(name), client.offer.contains(Street.NEUESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.NEUESTRASSE)) client.offer.remove(Street.NEUESTRASSE); else client.offer.add(Street.NEUESTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+335, y, 20, 40, Street.MARKTPLATZ.getOwner().equals(name), client.offer.contains(Street.MARKTPLATZ), actionEvent -> {
            if(client.offer.contains(Street.MARKTPLATZ)) client.offer.remove(Street.MARKTPLATZ); else client.offer.add(Street.MARKTPLATZ);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x, y+50, 20, 40, Street.MUENCHENERSTRASSE.getOwner().equals(name), client.offer.contains(Street.MUENCHENERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.MUENCHENERSTRASSE)) client.offer.remove(Street.MUENCHENERSTRASSE); else client.offer.add(Street.MUENCHENERSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+30, y+50, 20, 40, Street.WIENERSTRASSE.getOwner().equals(name), client.offer.contains(Street.WIENERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.WIENERSTRASSE)) client.offer.remove(Street.WIENERSTRASSE); else client.offer.add(Street.WIENERSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+60, y+50, 20, 40, Street.BERLINERSTRASSE.getOwner().equals(name), client.offer.contains(Street.BERLINERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.BERLINERSTRASSE)) client.offer.remove(Street.BERLINERSTRASSE); else client.offer.add(Street.BERLINERSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+90, y+50, 20, 40, Street.HAMBURGERSTRASSE.getOwner().equals(name), client.offer.contains(Street.HAMBURGERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.HAMBURGERSTRASSE)) client.offer.remove(Street.HAMBURGERSTRASSE); else client.offer.add(Street.HAMBURGERSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+130, y+50, 20, 40, Street.THEATERSTRASSE.getOwner().equals(name), client.offer.contains(Street.THEATERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.THEATERSTRASSE)) client.offer.remove(Street.THEATERSTRASSE); else client.offer.add(Street.THEATERSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+160, y+50, 20, 40, Street.MUSEUMSTRASSE.getOwner().equals(name), client.offer.contains(Street.MUSEUMSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.MUSEUMSTRASSE)) client.offer.remove(Street.MUSEUMSTRASSE); else client.offer.add(Street.MUSEUMSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+190, y+50, 20, 40, Street.OPERNPLATZ.getOwner().equals(name), client.offer.contains(Street.OPERNPLATZ), actionEvent -> {
            if(client.offer.contains(Street.OPERNPLATZ)) client.offer.remove(Street.OPERNPLATZ); else client.offer.add(Street.OPERNPLATZ);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+220, y+50, 20, 40, Street.KONZERTHAUSSTRASSE.getOwner().equals(name), client.offer.contains(Street.KONZERTHAUSSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.KONZERTHAUSSTRASSE)) client.offer.remove(Street.KONZERTHAUSSTRASSE); else client.offer.add(Street.KONZERTHAUSSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+260, y+50, 20, 40, Street.LESSINGSTRASSE.getOwner().equals(name), client.offer.contains(Street.LESSINGSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.LESSINGSTRASSE)) client.offer.remove(Street.LESSINGSTRASSE); else client.offer.add(Street.LESSINGSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+290, y+50, 20, 40, Street.SCHILLERSTRASSE.getOwner().equals(name), client.offer.contains(Street.SCHILLERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.SCHILLERSTRASSE)) client.offer.remove(Street.SCHILLERSTRASSE); else client.offer.add(Street.SCHILLERSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+320, y+50, 20, 40, Street.GOETHESTRASSE.getOwner().equals(name), client.offer.contains(Street.GOETHESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.GOETHESTRASSE)) client.offer.remove(Street.GOETHESTRASSE); else client.offer.add(Street.GOETHESTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+350, y+50, 20, 40, Street.RILKESTRASSE.getOwner().equals(name), client.offer.contains(Street.RILKESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.RILKESTRASSE)) client.offer.remove(Street.RILKESTRASSE); else client.offer.add(Street.RILKESTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+80, y+100, 20, 40, Street.RATHAUSPLATZ.getOwner().equals(name), client.offer.contains(Street.RATHAUSPLATZ), actionEvent -> {
            if(client.offer.contains(Street.RATHAUSPLATZ)) client.offer.remove(Street.RATHAUSPLATZ); else client.offer.add(Street.RATHAUSPLATZ);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+110, y+100, 20, 40, Street.HAUPSTRASSE.getOwner().equals(name), client.offer.contains(Street.HAUPSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.HAUPSTRASSE)) client.offer.remove(Street.HAUPSTRASSE); else client.offer.add(Street.HAUPSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+140, y+100, 20, 40, Street.BOERSENPLATZ.getOwner().equals(name), client.offer.contains(Street.BOERSENPLATZ), actionEvent -> {
            if(client.offer.contains(Street.BOERSENPLATZ)) client.offer.remove(Street.BOERSENPLATZ); else client.offer.add(Street.BOERSENPLATZ);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+170, y+100, 20, 40, Street.BAHNHOFSTRASSE.getOwner().equals(name), client.offer.contains(Street.BAHNHOFSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.BAHNHOFSTRASSE)) client.offer.remove(Street.BAHNHOFSTRASSE); else client.offer.add(Street.BAHNHOFSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+210, y+100, 20, 40, Street.DOMPLATZ.getOwner().equals(name), client.offer.contains(Street.DOMPLATZ), actionEvent -> {
            if(client.offer.contains(Street.DOMPLATZ)) client.offer.remove(Street.DOMPLATZ); else client.offer.add(Street.DOMPLATZ);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+240, y+100, 20, 40, Street.PARKSTRASSE.getOwner().equals(name), client.offer.contains(Street.PARKSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.PARKSTRASSE)) client.offer.remove(Street.PARKSTRASSE); else client.offer.add(Street.PARKSTRASSE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+270, y+100, 20, 40, Street.SCHLOSSALLEE.getOwner().equals(name), client.offer.contains(Street.SCHLOSSALLEE), actionEvent -> {
            if(client.offer.contains(Street.SCHLOSSALLEE)) client.offer.remove(Street.SCHLOSSALLEE); else client.offer.add(Street.SCHLOSSALLEE);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+80, y+150, 20, 40, TrainStation.SUEDBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.SUEDBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.SUEDBAHNHOF)) client.offer.remove(TrainStation.SUEDBAHNHOF); else client.offer.add(TrainStation.SUEDBAHNHOF);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+110, y+150, 20, 40, TrainStation.WESTBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.WESTBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.WESTBAHNHOF)) client.offer.remove(TrainStation.WESTBAHNHOF); else client.offer.add(TrainStation.WESTBAHNHOF);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+140, y+150, 20, 40, TrainStation.NORDBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.NORDBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.NORDBAHNHOF)) client.offer.remove(TrainStation.NORDBAHNHOF); else client.offer.add(TrainStation.NORDBAHNHOF);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+170, y+150, 20, 40, TrainStation.HAUPTBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.HAUPTBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.HAUPTBAHNHOF)) client.offer.remove(TrainStation.HAUPTBAHNHOF); else client.offer.add(TrainStation.HAUPTBAHNHOF);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "gas" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "gas" + ".png", x+210, y+150, 20, 40, Plant.GASWERK.getOwner().equals(name), client.offer.contains(Plant.GASWERK), actionEvent -> {
            if(client.offer.contains(Plant.GASWERK)) client.offer.remove(Plant.GASWERK); else client.offer.add(Plant.GASWERK);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "elec" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "elec" + ".png", x+240, y+150, 20, 40, Plant.ELEKTRIZITAETSWERK.getOwner().equals(name), client.offer.contains(Plant.ELEKTRIZITAETSWERK), actionEvent -> {
            if(client.offer.contains(Plant.ELEKTRIZITAETSWERK)) client.offer.remove(Plant.ELEKTRIZITAETSWERK); else client.offer.add(Plant.ELEKTRIZITAETSWERK);
            client.tradeState = TradeState.SEND_OFFER;
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "water" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "water" + ".png", x+270, y+150, 20, 40, Plant.WASSERWERK.getOwner().equals(name), client.offer.contains(Plant.WASSERWERK), actionEvent -> {
            if(client.offer.contains(Plant.WASSERWERK)) client.offer.remove(Plant.WASSERWERK); else client.offer.add(Plant.WASSERWERK);
            client.tradeState = TradeState.SEND_OFFER;
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
        frame.add(menu.addImage(Street.WIENERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.WIENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+30, y+50));
        frame.add(menu.addImage(Street.BERLINERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BERLINERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+60, y+50), 0);
        frame.add(menu.addImage(Street.HAMBURGERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAMBURGERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+90, y+50), 0);
        frame.add(menu.addImage(Street.THEATERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.THEATERSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+130, y+50), 0);
        frame.add(menu.addImage(Street.MUSEUMSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.MUSEUMSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+170, y+50), 0);
        frame.add(menu.addImage(Street.OPERNPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.OPERNPLATZ) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+200, y+50), 0);
        frame.add(menu.addImage(Street.KONZERTHAUSSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.KONZERTHAUSSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+230, y+50), 0);
        frame.add(menu.addImage(Street.LESSINGSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.LESSINGSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+270, y+50), 0);
        frame.add(menu.addImage(Street.SCHILLERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.SCHILLERSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+300, y+50), 0);
        frame.add(menu.addImage(Street.GOETHESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.GOETHESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+330, y+50), 0);
        frame.add(menu.addImage(Street.RILKESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.RILKESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+360, y+50), 0);

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
