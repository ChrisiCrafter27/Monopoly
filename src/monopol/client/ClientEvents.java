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
    public static void clickedOnStreet(String name, Street street) {

    }

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
                                client1.serverMethod().sendMessage(serverPlayer.getName(), MessageType.TRADE, TradeState.WAIT_FOR_ACCEPT);
                                menu.prepareGame();
                            } catch (IOException e) {
                                client1.close();
                            }
                        }), 0);
                        i += 1;
                    }
                }
            }
            case WAIT_FOR_ACCEPT -> {
                //Print a waiting screen and an interrupt button for accepting
                if(player2Name == null) return;
                frame.add(menu.addText("Waiting for " + player2Name + " to accept your invite", 1920/2-500, 1020/2-50, 1000, 20, true), 0);
                frame.add(menu.addButton("abort", 1920/2-100, 1020/2+50, 200, 50, true, actionEvent -> {
                    client1.tradePlayer = null;
                    client1.tradeState = TradeState.NULL;
                    menu.prepareGame();
                }), 0);
            }
            case CHANGE_OFFER -> {
                //Print the trade offers and buttons to change offer
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
