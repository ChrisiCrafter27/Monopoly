package monopol.client;

import monopol.constants.IPurchasable;
import monopol.constants.Plant;
import monopol.constants.Street;
import monopol.constants.TrainStation;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.server.DisconnectReason;
import monopol.rules.IEvents;
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
    private final IEvents eventsInterface;
    private final IServer serverInterface;
    public final ClientPlayer player;
    public DisconnectReason disconnectReason = null;
    public TradeState tradeState = TradeState.NULL;
    public String tradePlayer = null;
    public boolean counterOfferSend;
    public boolean tradePlayerConfirmed;
    public final ArrayList<IPurchasable> offer = new ArrayList<>();
    public final ArrayList<IPurchasable> counteroffer = new ArrayList<>();

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
            Registry registry1 = LocateRegistry.getRegistry(ip, 1299);
            eventsInterface = (IEvents) registry1.lookup("Events");
            Registry registry2 = LocateRegistry.getRegistry(ip, 1199);
            serverInterface = (IServer) registry2.lookup("Server");
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
                    if (player.getName() == null) player.setName((String) message.getMessage()[0]);
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
                            if(tradeState != TradeState.NULL && tradePlayer.equals(message.getMessage()[1])) {
                                tradeState = TradeState.ABORT;
                            }
                        }
                        case ACCEPT -> {
                            if(tradeState == TradeState.WAIT_FOR_ACCEPT && tradePlayer.equals(message.getMessage()[1])) {
                                tradeState = TradeState.CHANGE_OFFER;
                            }
                        }
                        case CHANGE_OFFER -> {
                            if(tradeState != TradeState.NULL && tradePlayer.equals(message.getMessage()[1])) {
                                counterOfferSend = false;
                            }
                        }
                        case CONFIRM -> {
                            if((tradeState == TradeState.CONFIRM || tradeState == TradeState.WAIT_FOR_CONFIRM) && tradePlayer.equals(message.getMessage()[1])) {
                                tradePlayerConfirmed = true;
                                tradeState = TradeState.CONFIRMED;
                            }
                        }
                        case DENY -> {
                            if(tradeState != TradeState.NULL && tradePlayer.equals(message.getMessage()[1])) {
                                tradeState = TradeState.DENY;
                            }
                        }
                        case IN_PROGRESS -> {
                            if(tradeState != TradeState.NULL && tradePlayer.equals(message.getMessage()[1])) {
                                tradeState = TradeState.IN_PROGRESS;
                            }
                        }
                        case NULL -> {
                            if(tradeState != TradeState.NULL) {
                                tradeState = TradeState.NULL;
                                tradePlayer = null;
                            }
                        }
                        case SEND_OFFER -> {
                            if(tradeState == TradeState.CHANGE_OFFER && tradePlayer.equals(message.getMessage()[1])) {

                                counteroffer.removeAll(counteroffer);
                                for(String string : (ArrayList<String>) message.getMessage()[2]) {
                                    try {
                                        counteroffer.add(Street.valueOf(string));
                                    } catch (Exception ignored) {}
                                    try {
                                        counteroffer.add(TrainStation.valueOf(string));
                                    } catch (Exception ignored) {}
                                    try {
                                        counteroffer.add(Plant.valueOf(string));
                                    } catch (Exception ignored) {}
                                }

                                counterOfferSend = true;
                                tradeState = TradeState.SEND_OFFER;
                            }
                        }
                        case SERVER_FAIL -> {
                            if(tradeState != TradeState.NULL /*TODO other conditions*/) {
                                tradeState = TradeState.SERVER_FAIL;
                            }
                        }
                        case WAIT_FOR_CONFIRM -> {

                        }
                        case WAIT_FOR_ACCEPT -> {
                            if(tradeState == TradeState.NULL) {
                                tradeState = TradeState.ACCEPT;
                                tradePlayer = (String) message.getMessage()[1];
                            } else {
                                Object[] array = new Object[2];
                                array[0] = TradeState.IN_PROGRESS;
                                array[1] = player.getName();
                                serverMethod().sendMessage((String) message.getMessage()[1], MessageType.TRADE, array);
                            }
                        }
                    }
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

    public IEvents eventMethod() {
        return eventsInterface;
    }
    public IServer serverMethod() {
        return serverInterface;
    }
}