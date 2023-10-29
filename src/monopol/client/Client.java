package monopol.client;

import monopol.constants.Plant;
import monopol.constants.Street;
import monopol.constants.TrainStation;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.server.DisconnectReason;
import monopol.server.IServer;
import monopol.utils.Json;
import monopol.message.Message;
import monopol.message.MessageType;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Client {
    private final Socket client;
    private final IServer serverInterface;
    public final ClientPlayer player;
    public DisconnectReason disconnectReason = null;
    public final TradeData tradeData = new TradeData();

    private final Thread clientThread = new Thread() {
        @Override
        public void run() {
            while(!interrupted()) {
                try {
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    String data = input.readUTF();
                    messageReceived(data);
                } catch (IOException e) {
                    if(!interrupted()) {
                        System.out.println("[Server]: Connection lost: No further information.");
                        interrupt();
                    }
                }
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public Client(String ip, int port, boolean isHost) throws NotBoundException {
        try {
            this.player = new ClientPlayer(isHost);
            client = new Socket(ip, port);
            Registry registry = LocateRegistry.getRegistry(ip, 1199);
            serverInterface = (IServer) registry.lookup("Server");
            if(serverMethod().stopped()) {
                System.out.println("Target server closed");
                JOptionPane.showMessageDialog(null, "The target server is currently stopped!", "Connection failed", JOptionPane.WARNING_MESSAGE);
                client.close();
                return;
            }
            clientThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void messageReceived(String value) {
        Message message;
        try {
            message = Json.toObject(value, Message.class);
            switch (message.getMessageType()) {
                case PRINTLN -> System.out.println(message.getMessage()[0]);
                case PING -> {
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    Object[] array = new Object[1];
                    array[0] = message.getMessage()[0];
                    output.writeUTF(Json.toString(new Message(array, MessageType.PING_BACK), false));
                }
                case PING_BACK -> {
                    long delay = System.currentTimeMillis() - (long) message.getMessage()[0];
                    System.out.println("[Server]: Your ping is " + delay + "ms");
                }
                case NAME -> {
                    if (player.getName() == null) {
                        player.setName((String) message.getMessage()[0]);
                        if(player.isHost) Monopoly.INSTANCE.setHost(player.getName());
                    }
                }
                case DISCONNECT -> {
                    disconnectReason = DisconnectReason.valueOf((String) message.getMessage()[0]);
                    clientThread.interrupt();
                    switch (disconnectReason) {
                        case CONNECTION_LOST -> System.out.println("[Client]: Connection lost: Timed out.");
                        case SERVER_CLOSED -> System.out.println("[Client]: Connection lost: Server closed.");
                        case CLIENT_CLOSED -> System.out.println("[Client]: Connection lost: Left game");
                        case KICKED -> System.out.println("[Client]: Connection lost: Kicked by other player.");
                        default -> System.out.println("[Client]: Connection lost: No further information.");
                    }
                }
                case TRADE -> {
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
                        case DENY -> {
                            if(tradeData.tradeState != TradeState.NULL && tradeData.tradePlayer.equals(message.getMessage()[1])) {
                                tradeData.tradeState = TradeState.DENY;
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
                            if(tradeData.tradeState != TradeState.NULL /*TODO other conditions*/) {
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
                }
                case UPDATE_OWNER -> {
                    ClientEvents.updateOwner(this);
                }
                case START -> Monopoly.INSTANCE.setState(GameState.RUNNING);
                case NULL -> {
                }
                default -> throw new RuntimeException();
            }
        } catch (IOException e) {
            System.out.println("[Server]: Connection lost: No further information.");
            clientThread.interrupt();
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            Message.send(new Message(DisconnectReason.CLIENT_CLOSED, MessageType.DISCONNECT), client);
        } catch (IOException ignored) {}
        clientThread.interrupt();
    }

    public boolean closed() {
        return client.isClosed();
    }

    public IServer serverMethod() {
        return serverInterface;
    }

}