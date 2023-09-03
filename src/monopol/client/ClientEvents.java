package monopol.client;

import monopol.constants.IPurchasable;
import monopol.constants.Plant;
import monopol.constants.Street;
import monopol.constants.TrainStation;
import monopol.message.MessageType;
import monopol.screen.PrototypeMenu;
import monopol.server.ServerPlayer;
import monopol.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.rmi.RemoteException;

public class ClientEvents {
    public static void trade(PrototypeMenu menu, String player2Name, TradeState state) throws RemoteException {
        JFrame frame = menu.frame;
        Client client1 = menu.client;
        ClientPlayer player1 = client1.player;
        String player1Name = player1.getName();

        client1.tradeState = state;

        for(Component component : frame.getContentPane().getComponents()) {
            if(component instanceof JButton button) {
                if(button.getY() != 0) button.setEnabled(false);
            }
        }

        frame.add(menu.addImage("images/global/gray_background.png", 0, 60), 0);

        switch (state) {
            case CHOOSE_PLAYER -> {
                //Print a list of all other players with a button to send a trade invite
                client1.offer.removeAll(client1.offer);
                int i = 0;
                for(ServerPlayer serverPlayer : client1.serverMethod().getServerPlayers()) {
                    if(!player1Name.equals(serverPlayer.getName())) {
                        frame.add(menu.addButton(serverPlayer.getName(), 1920 / 2 - 250, 200 + (75 * i), 500, 50, true, actionEvent -> {
                            try {
                                client1.tradePlayer = serverPlayer.getName();
                                client1.tradeState = TradeState.WAIT_FOR_ACCEPT;
                                Object[] array = new Object[2];
                                array[0] = TradeState.WAIT_FOR_ACCEPT;
                                array[1] = player1Name;
                                client1.serverMethod().sendMessage(serverPlayer.getName(), MessageType.TRADE, array);
                                menu.prepareGame();
                            } catch (IOException e) {
                                client1.close();
                            }
                        }), 0);
                        i += 1;
                    }
                }
                frame.add(menu.addButton("Abbrechen", 1920/2-100, 1080-100, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case WAIT_FOR_ACCEPT -> {
                //Print a waiting screen and an interrupt button for accepting
                client1.tradePlayer = player2Name;
                if(player2Name == null) return;
                frame.add(menu.addText("Warte, bis " + player2Name + " deine Einladungs akzeptiert", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Abbrechen", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1Name;
                    try {
                        client1.serverMethod().sendMessage(player2Name, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client1.close();
                    }
                    menu.prepareGame();
                }), 0);
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client1) return;
                            if(client1.tradeState != TradeState.WAIT_FOR_ACCEPT) {
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
                if(player2Name == null) return;
                client1.offer.removeAll(client1.offer);
                client1.tradePlayer = player2Name;
                frame.add(menu.addText(player2Name + " möchte mit dir handeln", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Ablehnen", 1920/2-100-150, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.DENY;
                    array[1] = player1Name;
                    try {
                        client1.serverMethod().sendMessage(player2Name, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client1.close();
                    }
                    menu.prepareGame();
                }), 0);
                frame.add(menu.addButton("Annehmen", 1920/2-100+150, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradeState = TradeState.CHANGE_OFFER;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ACCEPT;
                    array[1] = player1Name;
                    try {
                        client1.serverMethod().sendMessage(player2Name, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client1.close();
                    }
                    menu.prepareGame();
                }), 0);
                new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            if(menu.client != client1) return;
                            if(client1.tradeState != TradeState.ACCEPT) {
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
                if(player2Name == null) return;
                frame.add(menu.addText(player2Name + " hat deine Einladung abgelehnt", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case ABORT -> {
                if(player2Name == null) return;
                frame.add(menu.addText(player2Name + " hat den Handel abgebrochen", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case IN_PROGRESS -> {
                if(player2Name == null) return;
                frame.add(menu.addText(player2Name + " handelt schon", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("Okay", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case CHANGE_OFFER -> {
                //Print the trade offers and buttons to change offer
                if(player2Name == null) return;
                frame.add(menu.addText("Angebot von dir", 0, 100, 1920/2, 20, true), 0);
                frame.add(menu.addText("Angebot von " + player2Name, 1920/2, 100, 1920/2, 20, true), 0);

                JButton button;
                int x = 1920/4-80-80-80-30-30-30-20-20;
                int y = 200;
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+15, y, 20, 40, Street.BADSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.BADSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.BADSTRASSE)) client1.offer.remove(Street.BADSTRASSE); else client1.offer.add(Street.BADSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+45, y, 20, 40, Street.TURMSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.TURMSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.TURMSTRASSE)) client1.offer.remove(Street.TURMSTRASSE); else client1.offer.add(Street.TURMSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+75, y, 20, 40, Street.STADIONSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.STADIONSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.STADIONSTRASSE)) client1.offer.remove(Street.STADIONSTRASSE); else client1.offer.add(Street.STADIONSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+115, y, 20, 40, Street.CHAUSSESTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.CHAUSSESTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.CHAUSSESTRASSE)) client1.offer.remove(Street.CHAUSSESTRASSE); else client1.offer.add(Street.CHAUSSESTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+145, y, 20, 40, Street.ELISENSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.ELISENSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.ELISENSTRASSE)) client1.offer.remove(Street.ELISENSTRASSE); else client1.offer.add(Street.ELISENSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+175, y, 20, 40, Street.POSTSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.POSTSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.POSTSTRASSE)) client1.offer.remove(Street.POSTSTRASSE); else client1.offer.add(Street.POSTSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+205, y, 20, 40, Street.TIERGARTENSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.TIERGARTENSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.TIERGARTENSTRASSE)) client1.offer.remove(Street.TIERGARTENSTRASSE); else client1.offer.add(Street.TIERGARTENSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+245, y, 20, 40, Street.SEESTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.SEESTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.SEESTRASSE)) client1.offer.remove(Street.SEESTRASSE); else client1.offer.add(Street.SEESTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+275, y, 20, 40, Street.HAFENSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.HAFENSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.HAFENSTRASSE)) client1.offer.remove(Street.HAFENSTRASSE); else client1.offer.add(Street.HAFENSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+305, y, 20, 40, Street.NEUESTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.NEUESTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.NEUESTRASSE)) client1.offer.remove(Street.NEUESTRASSE); else client1.offer.add(Street.NEUESTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+335, y, 20, 40, Street.MARKTPLATZ.getOwner().equals(player1Name), client1.offer.contains(Street.MARKTPLATZ), actionEvent -> {
                    if(client1.offer.contains(Street.MARKTPLATZ)) client1.offer.remove(Street.MARKTPLATZ); else client1.offer.add(Street.MARKTPLATZ);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x, y+50, 20, 40, Street.MUENCHENERSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.MUENCHENERSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.MUENCHENERSTRASSE)) client1.offer.remove(Street.MUENCHENERSTRASSE); else client1.offer.add(Street.MUENCHENERSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+30, y+50, 20, 40, Street.WIENERSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.WIENERSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.WIENERSTRASSE)) client1.offer.remove(Street.WIENERSTRASSE); else client1.offer.add(Street.WIENERSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+60, y+50, 20, 40, Street.BERLINERSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.BERLINERSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.BERLINERSTRASSE)) client1.offer.remove(Street.BERLINERSTRASSE); else client1.offer.add(Street.BERLINERSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+90, y+50, 20, 40, Street.HAMBURGERSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.HAMBURGERSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.HAMBURGERSTRASSE)) client1.offer.remove(Street.HAMBURGERSTRASSE); else client1.offer.add(Street.HAMBURGERSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+130, y+50, 20, 40, Street.THEATERSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.THEATERSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.THEATERSTRASSE)) client1.offer.remove(Street.THEATERSTRASSE); else client1.offer.add(Street.THEATERSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+160, y+50, 20, 40, Street.MUSEUMSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.MUSEUMSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.MUSEUMSTRASSE)) client1.offer.remove(Street.MUSEUMSTRASSE); else client1.offer.add(Street.MUSEUMSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+190, y+50, 20, 40, Street.OPERNPLATZ.getOwner().equals(player1Name), client1.offer.contains(Street.OPERNPLATZ), actionEvent -> {
                    if(client1.offer.contains(Street.OPERNPLATZ)) client1.offer.remove(Street.OPERNPLATZ); else client1.offer.add(Street.OPERNPLATZ);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+220, y+50, 20, 40, Street.KONZERTHAUSSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.KONZERTHAUSSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.KONZERTHAUSSTRASSE)) client1.offer.remove(Street.KONZERTHAUSSTRASSE); else client1.offer.add(Street.KONZERTHAUSSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+260, y+50, 20, 40, Street.LESSINGSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.LESSINGSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.LESSINGSTRASSE)) client1.offer.remove(Street.LESSINGSTRASSE); else client1.offer.add(Street.LESSINGSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+290, y+50, 20, 40, Street.SCHILLERSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.SCHILLERSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.SCHILLERSTRASSE)) client1.offer.remove(Street.SCHILLERSTRASSE); else client1.offer.add(Street.SCHILLERSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+320, y+50, 20, 40, Street.GOETHESTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.GOETHESTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.GOETHESTRASSE)) client1.offer.remove(Street.GOETHESTRASSE); else client1.offer.add(Street.GOETHESTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+350, y+50, 20, 40, Street.RILKESTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.RILKESTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.RILKESTRASSE)) client1.offer.remove(Street.RILKESTRASSE); else client1.offer.add(Street.RILKESTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+80, y+100, 20, 40, Street.RATHAUSPLATZ.getOwner().equals(player1Name), client1.offer.contains(Street.RATHAUSPLATZ), actionEvent -> {
                    if(client1.offer.contains(Street.RATHAUSPLATZ)) client1.offer.remove(Street.RATHAUSPLATZ); else client1.offer.add(Street.RATHAUSPLATZ);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+110, y+100, 20, 40, Street.HAUPSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.HAUPSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.HAUPSTRASSE)) client1.offer.remove(Street.HAUPSTRASSE); else client1.offer.add(Street.HAUPSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+140, y+100, 20, 40, Street.BOERSENPLATZ.getOwner().equals(player1Name), client1.offer.contains(Street.BOERSENPLATZ), actionEvent -> {
                    if(client1.offer.contains(Street.BOERSENPLATZ)) client1.offer.remove(Street.BOERSENPLATZ); else client1.offer.add(Street.BOERSENPLATZ);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+170, y+100, 20, 40, Street.BAHNHOFSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.BAHNHOFSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.BAHNHOFSTRASSE)) client1.offer.remove(Street.BAHNHOFSTRASSE); else client1.offer.add(Street.BAHNHOFSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+210, y+100, 20, 40, Street.DOMPLATZ.getOwner().equals(player1Name), client1.offer.contains(Street.DOMPLATZ), actionEvent -> {
                    if(client1.offer.contains(Street.DOMPLATZ)) client1.offer.remove(Street.DOMPLATZ); else client1.offer.add(Street.DOMPLATZ);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+240, y+100, 20, 40, Street.PARKSTRASSE.getOwner().equals(player1Name), client1.offer.contains(Street.PARKSTRASSE), actionEvent -> {
                    if(client1.offer.contains(Street.PARKSTRASSE)) client1.offer.remove(Street.PARKSTRASSE); else client1.offer.add(Street.PARKSTRASSE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+270, y+100, 20, 40, Street.SCHLOSSALLEE.getOwner().equals(player1Name), client1.offer.contains(Street.SCHLOSSALLEE), actionEvent -> {
                    if(client1.offer.contains(Street.SCHLOSSALLEE)) client1.offer.remove(Street.SCHLOSSALLEE); else client1.offer.add(Street.SCHLOSSALLEE);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+80, y+150, 20, 40, TrainStation.SUEDBAHNHOF.getOwner().equals(player1Name), client1.offer.contains(TrainStation.SUEDBAHNHOF), actionEvent -> {
                    if(client1.offer.contains(TrainStation.SUEDBAHNHOF)) client1.offer.remove(TrainStation.SUEDBAHNHOF); else client1.offer.add(TrainStation.SUEDBAHNHOF);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+110, y+150, 20, 40, TrainStation.WESTBAHNHOF.getOwner().equals(player1Name), client1.offer.contains(TrainStation.WESTBAHNHOF), actionEvent -> {
                    if(client1.offer.contains(TrainStation.WESTBAHNHOF)) client1.offer.remove(TrainStation.WESTBAHNHOF); else client1.offer.add(TrainStation.WESTBAHNHOF);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+140, y+150, 20, 40, TrainStation.NORDBAHNHOF.getOwner().equals(player1Name), client1.offer.contains(TrainStation.NORDBAHNHOF), actionEvent -> {
                    if(client1.offer.contains(TrainStation.NORDBAHNHOF)) client1.offer.remove(TrainStation.NORDBAHNHOF); else client1.offer.add(TrainStation.NORDBAHNHOF);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+170, y+150, 20, 40, TrainStation.HAUPTBAHNHOF.getOwner().equals(player1Name), client1.offer.contains(TrainStation.HAUPTBAHNHOF), actionEvent -> {
                    if(client1.offer.contains(TrainStation.HAUPTBAHNHOF)) client1.offer.remove(TrainStation.HAUPTBAHNHOF); else client1.offer.add(TrainStation.HAUPTBAHNHOF);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "gas" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "gas" + ".png", x+210, y+150, 20, 40, Plant.GASWERK.getOwner().equals(player1Name), client1.offer.contains(Plant.GASWERK), actionEvent -> {
                    if(client1.offer.contains(Plant.GASWERK)) client1.offer.remove(Plant.GASWERK); else client1.offer.add(Plant.GASWERK);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "elec" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "elec" + ".png", x+240, y+150, 20, 40, Plant.ELEKTRIZITAETSWERK.getOwner().equals(player1Name), client1.offer.contains(Plant.ELEKTRIZITAETSWERK), actionEvent -> {
                    if(client1.offer.contains(Plant.ELEKTRIZITAETSWERK)) client1.offer.remove(Plant.ELEKTRIZITAETSWERK); else client1.offer.add(Plant.ELEKTRIZITAETSWERK);
                    menu.prepareGame();
                }), 0);
                button = new JButton();
                button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "water" + "_filled.png"));
                button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
                frame.add(menu.addButton(button, "", "images/kleine_karten/" + "water" + ".png", x+270, y+150, 20, 40, Plant.WASSERWERK.getOwner().equals(player1Name), client1.offer.contains(Plant.WASSERWERK), actionEvent -> {
                    if(client1.offer.contains(Plant.WASSERWERK)) client1.offer.remove(Plant.WASSERWERK); else client1.offer.add(Plant.WASSERWERK);
                    menu.prepareGame();
                }), 0);

                frame.add(menu.addButton("Abbrechen", 1920/2-100, 1080-150, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    Object[] array = new Object[2];
                    array[0] = TradeState.ABORT;
                    array[1] = player1Name;
                    try {
                        client1.serverMethod().sendMessage(player2Name, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client1.close();
                    }
                    menu.prepareGame();
                }), 0);
            }
            case WAIT_FOR_OFFER -> {
                //Print the trade offers
            }
            case CONFIRM -> {
                //Print the trade offers with an accept button
            }
            case WAIT_FOR_CONFIRM -> {
                //Print a waiting screen and an interrupt button for confirmation
            }
        }
        frame.repaint();
    }
}
