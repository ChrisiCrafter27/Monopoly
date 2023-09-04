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
import java.util.ArrayList;

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
                client1.counteroffer.removeAll(client1.counteroffer);
                client1.purchasedCardsInfo.removeAll(client1.purchasedCardsInfo);
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
                client1.counteroffer.removeAll(client1.counteroffer);
                client1.purchasedCardsInfo.removeAll(client1.purchasedCardsInfo);
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

                addTradeInfo(menu, client1, player2Name, 1920/4+1920/4+1920/4-40-40-40-15-15-15-10-10, 200);
                addTradeButtons(menu, client1, player1Name, 1920/4-40-40-40-15-15-15-10-10, 200);

                frame.add(menu.addButton("Absenden", 1920/2-100, 1080-225, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.CONFIRM;
                    Object[] array = new Object[3];
                    array[0] = TradeState.ABORT;
                    array[1] = player1Name;
                    array[2] = client1.offer;
                    try {
                        client1.serverMethod().sendMessage(player2Name, MessageType.TRADE, array);
                    } catch (IOException e) {
                        client1.close();
                    }
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
            case CONFIRM -> {
                //Print the trade offers with an accept button
            }
            case WAIT_FOR_CONFIRM -> {
                //Print a waiting screen and an interrupt button for confirmation
            }
        }
        frame.repaint();
    }

    private static void addTradeButtons(PrototypeMenu menu, Client client, String name, int x, int y) {
        JFrame frame = menu.frame;
        JButton button;
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+15, y, 20, 40, Street.BADSTRASSE.getOwner().equals(name), client.offer.contains(Street.BADSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.BADSTRASSE)) client.offer.remove(Street.BADSTRASSE); else client.offer.add(Street.BADSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+45, y, 20, 40, Street.TURMSTRASSE.getOwner().equals(name), client.offer.contains(Street.TURMSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.TURMSTRASSE)) client.offer.remove(Street.TURMSTRASSE); else client.offer.add(Street.TURMSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+75, y, 20, 40, Street.STADIONSTRASSE.getOwner().equals(name), client.offer.contains(Street.STADIONSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.STADIONSTRASSE)) client.offer.remove(Street.STADIONSTRASSE); else client.offer.add(Street.STADIONSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+115, y, 20, 40, Street.CHAUSSESTRASSE.getOwner().equals(name), client.offer.contains(Street.CHAUSSESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.CHAUSSESTRASSE)) client.offer.remove(Street.CHAUSSESTRASSE); else client.offer.add(Street.CHAUSSESTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+145, y, 20, 40, Street.ELISENSTRASSE.getOwner().equals(name), client.offer.contains(Street.ELISENSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.ELISENSTRASSE)) client.offer.remove(Street.ELISENSTRASSE); else client.offer.add(Street.ELISENSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+175, y, 20, 40, Street.POSTSTRASSE.getOwner().equals(name), client.offer.contains(Street.POSTSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.POSTSTRASSE)) client.offer.remove(Street.POSTSTRASSE); else client.offer.add(Street.POSTSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "cyan" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "cyan" + ".png", x+205, y, 20, 40, Street.TIERGARTENSTRASSE.getOwner().equals(name), client.offer.contains(Street.TIERGARTENSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.TIERGARTENSTRASSE)) client.offer.remove(Street.TIERGARTENSTRASSE); else client.offer.add(Street.TIERGARTENSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+245, y, 20, 40, Street.SEESTRASSE.getOwner().equals(name), client.offer.contains(Street.SEESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.SEESTRASSE)) client.offer.remove(Street.SEESTRASSE); else client.offer.add(Street.SEESTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+275, y, 20, 40, Street.HAFENSTRASSE.getOwner().equals(name), client.offer.contains(Street.HAFENSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.HAFENSTRASSE)) client.offer.remove(Street.HAFENSTRASSE); else client.offer.add(Street.HAFENSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+305, y, 20, 40, Street.NEUESTRASSE.getOwner().equals(name), client.offer.contains(Street.NEUESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.NEUESTRASSE)) client.offer.remove(Street.NEUESTRASSE); else client.offer.add(Street.NEUESTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "pink" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "pink" + ".png", x+335, y, 20, 40, Street.MARKTPLATZ.getOwner().equals(name), client.offer.contains(Street.MARKTPLATZ), actionEvent -> {
            if(client.offer.contains(Street.MARKTPLATZ)) client.offer.remove(Street.MARKTPLATZ); else client.offer.add(Street.MARKTPLATZ);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x, y+50, 20, 40, Street.MUENCHENERSTRASSE.getOwner().equals(name), client.offer.contains(Street.MUENCHENERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.MUENCHENERSTRASSE)) client.offer.remove(Street.MUENCHENERSTRASSE); else client.offer.add(Street.MUENCHENERSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+30, y+50, 20, 40, Street.WIENERSTRASSE.getOwner().equals(name), client.offer.contains(Street.WIENERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.WIENERSTRASSE)) client.offer.remove(Street.WIENERSTRASSE); else client.offer.add(Street.WIENERSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+60, y+50, 20, 40, Street.BERLINERSTRASSE.getOwner().equals(name), client.offer.contains(Street.BERLINERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.BERLINERSTRASSE)) client.offer.remove(Street.BERLINERSTRASSE); else client.offer.add(Street.BERLINERSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "orange" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "orange" + ".png", x+90, y+50, 20, 40, Street.HAMBURGERSTRASSE.getOwner().equals(name), client.offer.contains(Street.HAMBURGERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.HAMBURGERSTRASSE)) client.offer.remove(Street.HAMBURGERSTRASSE); else client.offer.add(Street.HAMBURGERSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+130, y+50, 20, 40, Street.THEATERSTRASSE.getOwner().equals(name), client.offer.contains(Street.THEATERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.THEATERSTRASSE)) client.offer.remove(Street.THEATERSTRASSE); else client.offer.add(Street.THEATERSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+160, y+50, 20, 40, Street.MUSEUMSTRASSE.getOwner().equals(name), client.offer.contains(Street.MUSEUMSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.MUSEUMSTRASSE)) client.offer.remove(Street.MUSEUMSTRASSE); else client.offer.add(Street.MUSEUMSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+190, y+50, 20, 40, Street.OPERNPLATZ.getOwner().equals(name), client.offer.contains(Street.OPERNPLATZ), actionEvent -> {
            if(client.offer.contains(Street.OPERNPLATZ)) client.offer.remove(Street.OPERNPLATZ); else client.offer.add(Street.OPERNPLATZ);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "red" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "red" + ".png", x+220, y+50, 20, 40, Street.KONZERTHAUSSTRASSE.getOwner().equals(name), client.offer.contains(Street.KONZERTHAUSSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.KONZERTHAUSSTRASSE)) client.offer.remove(Street.KONZERTHAUSSTRASSE); else client.offer.add(Street.KONZERTHAUSSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+260, y+50, 20, 40, Street.LESSINGSTRASSE.getOwner().equals(name), client.offer.contains(Street.LESSINGSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.LESSINGSTRASSE)) client.offer.remove(Street.LESSINGSTRASSE); else client.offer.add(Street.LESSINGSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+290, y+50, 20, 40, Street.SCHILLERSTRASSE.getOwner().equals(name), client.offer.contains(Street.SCHILLERSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.SCHILLERSTRASSE)) client.offer.remove(Street.SCHILLERSTRASSE); else client.offer.add(Street.SCHILLERSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+320, y+50, 20, 40, Street.GOETHESTRASSE.getOwner().equals(name), client.offer.contains(Street.GOETHESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.GOETHESTRASSE)) client.offer.remove(Street.GOETHESTRASSE); else client.offer.add(Street.GOETHESTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "yellow" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "yellow" + ".png", x+350, y+50, 20, 40, Street.RILKESTRASSE.getOwner().equals(name), client.offer.contains(Street.RILKESTRASSE), actionEvent -> {
            if(client.offer.contains(Street.RILKESTRASSE)) client.offer.remove(Street.RILKESTRASSE); else client.offer.add(Street.RILKESTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+80, y+100, 20, 40, Street.RATHAUSPLATZ.getOwner().equals(name), client.offer.contains(Street.RATHAUSPLATZ), actionEvent -> {
            if(client.offer.contains(Street.RATHAUSPLATZ)) client.offer.remove(Street.RATHAUSPLATZ); else client.offer.add(Street.RATHAUSPLATZ);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+110, y+100, 20, 40, Street.HAUPSTRASSE.getOwner().equals(name), client.offer.contains(Street.HAUPSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.HAUPSTRASSE)) client.offer.remove(Street.HAUPSTRASSE); else client.offer.add(Street.HAUPSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+140, y+100, 20, 40, Street.BOERSENPLATZ.getOwner().equals(name), client.offer.contains(Street.BOERSENPLATZ), actionEvent -> {
            if(client.offer.contains(Street.BOERSENPLATZ)) client.offer.remove(Street.BOERSENPLATZ); else client.offer.add(Street.BOERSENPLATZ);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "green" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "green" + ".png", x+170, y+100, 20, 40, Street.BAHNHOFSTRASSE.getOwner().equals(name), client.offer.contains(Street.BAHNHOFSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.BAHNHOFSTRASSE)) client.offer.remove(Street.BAHNHOFSTRASSE); else client.offer.add(Street.BAHNHOFSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+210, y+100, 20, 40, Street.DOMPLATZ.getOwner().equals(name), client.offer.contains(Street.DOMPLATZ), actionEvent -> {
            if(client.offer.contains(Street.DOMPLATZ)) client.offer.remove(Street.DOMPLATZ); else client.offer.add(Street.DOMPLATZ);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+240, y+100, 20, 40, Street.PARKSTRASSE.getOwner().equals(name), client.offer.contains(Street.PARKSTRASSE), actionEvent -> {
            if(client.offer.contains(Street.PARKSTRASSE)) client.offer.remove(Street.PARKSTRASSE); else client.offer.add(Street.PARKSTRASSE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "blue" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "blue" + ".png", x+270, y+100, 20, 40, Street.SCHLOSSALLEE.getOwner().equals(name), client.offer.contains(Street.SCHLOSSALLEE), actionEvent -> {
            if(client.offer.contains(Street.SCHLOSSALLEE)) client.offer.remove(Street.SCHLOSSALLEE); else client.offer.add(Street.SCHLOSSALLEE);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+80, y+150, 20, 40, TrainStation.SUEDBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.SUEDBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.SUEDBAHNHOF)) client.offer.remove(TrainStation.SUEDBAHNHOF); else client.offer.add(TrainStation.SUEDBAHNHOF);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+110, y+150, 20, 40, TrainStation.WESTBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.WESTBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.WESTBAHNHOF)) client.offer.remove(TrainStation.WESTBAHNHOF); else client.offer.add(TrainStation.WESTBAHNHOF);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+140, y+150, 20, 40, TrainStation.NORDBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.NORDBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.NORDBAHNHOF)) client.offer.remove(TrainStation.NORDBAHNHOF); else client.offer.add(TrainStation.NORDBAHNHOF);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "train" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "train" + ".png", x+170, y+150, 20, 40, TrainStation.HAUPTBAHNHOF.getOwner().equals(name), client.offer.contains(TrainStation.HAUPTBAHNHOF), actionEvent -> {
            if(client.offer.contains(TrainStation.HAUPTBAHNHOF)) client.offer.remove(TrainStation.HAUPTBAHNHOF); else client.offer.add(TrainStation.HAUPTBAHNHOF);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "gas" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "gas" + ".png", x+210, y+150, 20, 40, Plant.GASWERK.getOwner().equals(name), client.offer.contains(Plant.GASWERK), actionEvent -> {
            if(client.offer.contains(Plant.GASWERK)) client.offer.remove(Plant.GASWERK); else client.offer.add(Plant.GASWERK);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "elec" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "elec" + ".png", x+240, y+150, 20, 40, Plant.ELEKTRIZITAETSWERK.getOwner().equals(name), client.offer.contains(Plant.ELEKTRIZITAETSWERK), actionEvent -> {
            if(client.offer.contains(Plant.ELEKTRIZITAETSWERK)) client.offer.remove(Plant.ELEKTRIZITAETSWERK); else client.offer.add(Plant.ELEKTRIZITAETSWERK);
            menu.prepareGame();
        }), 0);
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "water" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        frame.add(menu.addButton(button, "", "images/kleine_karten/" + "water" + ".png", x+270, y+150, 20, 40, Plant.WASSERWERK.getOwner().equals(name), client.offer.contains(Plant.WASSERWERK), actionEvent -> {
            if(client.offer.contains(Plant.WASSERWERK)) client.offer.remove(Plant.WASSERWERK); else client.offer.add(Plant.WASSERWERK);
            menu.prepareGame();
        }), 0);
    }
    private static void addTradeInfo(PrototypeMenu menu, Client client, String name, int x, int y) {
        JFrame frame = menu.frame;

        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.BADSTRASSE) ? client.counteroffer.contains(Street.BADSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+15, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.TURMSTRASSE) ? client.counteroffer.contains(Street.TURMSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+45, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.STADIONSTRASSE) ? client.counteroffer.contains(Street.STADIONSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+75, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.CHAUSSESTRASSE) ? client.counteroffer.contains(Street.CHAUSSESTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+115, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.ELISENSTRASSE) ? client.counteroffer.contains(Street.ELISENSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+145, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.POSTSTRASSE) ? client.counteroffer.contains(Street.POSTSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+175, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.TIERGARTENSTRASSE) ? client.counteroffer.contains(Street.TIERGARTENSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+205, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.SEESTRASSE) ? client.counteroffer.contains(Street.SEESTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+245, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.HAFENSTRASSE) ? client.counteroffer.contains(Street.HAFENSTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+275, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.NEUESTRASSE) ? client.counteroffer.contains(Street.NEUESTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+305, y), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.MARKTPLATZ) ? client.counteroffer.contains(Street.MARKTPLATZ) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+335, y), 0);

        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.MUENCHENERSTRASSE) ? client.counteroffer.contains(Street.MUENCHENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.WIENERSTRASSE) ? client.counteroffer.contains(Street.WIENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+30, y+50));
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.BERLINERSTRASSE) ? client.counteroffer.contains(Street.BERLINERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+60, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.HAMBURGERSTRASSE) ? client.counteroffer.contains(Street.HAMBURGERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+90, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.THEATERSTRASSE) ? client.counteroffer.contains(Street.THEATERSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+130, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.MUSEUMSTRASSE) ? client.counteroffer.contains(Street.MUSEUMSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+170, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.OPERNPLATZ) ? client.counteroffer.contains(Street.OPERNPLATZ) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+200, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.KONZERTHAUSSTRASSE) ? client.counteroffer.contains(Street.KONZERTHAUSSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+230, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.LESSINGSTRASSE) ? client.counteroffer.contains(Street.LESSINGSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+270, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.SCHILLERSTRASSE) ? client.counteroffer.contains(Street.SCHILLERSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+300, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.GOETHESTRASSE) ? client.counteroffer.contains(Street.GOETHESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+330, y+50), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.RILKESTRASSE) ? client.counteroffer.contains(Street.RILKESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+360, y+50), 0);

        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.RATHAUSPLATZ) ? client.counteroffer.contains(Street.RATHAUSPLATZ) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+80, y+100), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.HAUPSTRASSE) ? client.counteroffer.contains(Street.HAUPSTRASSE) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+110, y+100), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.BOERSENPLATZ) ? client.counteroffer.contains(Street.BOERSENPLATZ) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+140, y+100), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.BAHNHOFSTRASSE) ? client.counteroffer.contains(Street.BAHNHOFSTRASSE) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+170, y+100), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.DOMPLATZ) ? client.counteroffer.contains(Street.DOMPLATZ) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+210, y+100), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.PARKSTRASSE) ? client.counteroffer.contains(Street.PARKSTRASSE) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+240, y+100), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Street.SCHLOSSALLEE) ? client.counteroffer.contains(Street.SCHLOSSALLEE) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+270, y+100), 0);

        frame.add(menu.addImage(client.purchasedCardsInfo.contains(TrainStation.SUEDBAHNHOF) ? client.counteroffer.contains(TrainStation.SUEDBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+80, y+150), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(TrainStation.WESTBAHNHOF) ? client.counteroffer.contains(TrainStation.WESTBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+110, y+150), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(TrainStation.NORDBAHNHOF) ? client.counteroffer.contains(TrainStation.NORDBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+140, y+150), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(TrainStation.HAUPTBAHNHOF) ? client.counteroffer.contains(TrainStation.HAUPTBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+170, y+150), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Plant.GASWERK) ? client.counteroffer.contains(Plant.GASWERK) ? "images/kleine_karten/gas_filled.png" : "images/kleine_karten/gas.png" : "images/kleine_karten/disabled.png", x+210, y+150), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Plant.ELEKTRIZITAETSWERK) ? client.counteroffer.contains(Plant.ELEKTRIZITAETSWERK) ? "images/kleine_karten/elec_filled.png" : "images/kleine_karten/elec.png" : "images/kleine_karten/disabled.png", x+240, y+150), 0);
        frame.add(menu.addImage(client.purchasedCardsInfo.contains(Plant.WASSERWERK) ? client.counteroffer.contains(Plant.WASSERWERK) ? "images/kleine_karten/water_filled.png" : "images/kleine_karten/water.png" : "images/kleine_karten/disabled.png", x+270, y+150), 0);
    }
}
