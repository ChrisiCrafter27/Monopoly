package monopol.client;

import monopol.constants.Street;
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
                frame.add(menu.addText("Du handelst mit " + player2Name, 1920/2-500, 1020/2-50, 1000, 20, true), 0);
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
