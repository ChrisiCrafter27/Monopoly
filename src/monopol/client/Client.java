package monopol.client;

import monopol.common.data.Plant;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.core.GameState;
import monopol.common.core.Monopoly;
import monopol.common.packets.ClientSide;
import monopol.common.packets.PacketManager;
import monopol.client.screen.RootPane;
import monopol.server.DisconnectReason;
import monopol.server.IServer;
import monopol.common.utils.Json;
import monopol.common.message.Message;
import monopol.common.message.MessageType;
import monopol.server.Server;

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
    private final RootPane root;
    public DisconnectReason disconnectReason = null;
    public final TradeData tradeData = new TradeData();
    private long ping = -1;
    boolean received = true;

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
                        e.printStackTrace(System.err);
                        System.out.println("[Client]: Connection lost: No further information.");
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

    private final Thread pingThread = new Thread(() -> {
        while(!Thread.interrupted()) {
            try {
                Message.sendPing(socket());
            } catch (IOException e) {
                if(!socket().isClosed()) e.printStackTrace(System.err);
            }
            if (!received) {
                try {
                    serverMethod().kick(player().getName(), DisconnectReason.CONNECTION_LOST);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            received = false;
            try {
                Thread.sleep(Server.CLIENT_TIMEOUT);
            } catch (InterruptedException e) {
                return;
            }
        }
    });

    public Client(String ip, int port, boolean isHost, RootPane root) throws NotBoundException {
        try {
            this.root = root;
            this.player = new ClientPlayer(isHost);
            client = new Socket(ip, port);
            Registry registry = LocateRegistry.getRegistry(ip, 1199);
            serverInterface = (IServer) registry.lookup("Server");
            if(serverMethod().stopped() || !serverInterface.acceptsNewClient()) {
                JOptionPane.showMessageDialog(null, "Beitreten nicht möglich", "Spiel beitreten", JOptionPane.WARNING_MESSAGE);
                client.close();
                return;
            }
            clientThread.start();
            pingThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void messageReceived(String value) {
        Message message;
        try {
            message = Json.toObject(value, Message.class);
            switch (message.getMessageType()) {
                case PACKET -> PacketManager.handle(message, new ClientSide(this, root));
                case PRINTLN -> System.out.println(message.getMessage()[0]);
                case PING -> {
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    Object[] array = new Object[1];
                    array[0] = message.getMessage()[0];
                    output.writeUTF(Json.toString(new Message(array, MessageType.PING_BACK), false));
                }
                case PING_BACK -> {
                    ping = System.currentTimeMillis() - (long) message.getMessage()[0];
                    received = true;
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
                    pingThread.interrupt();
                    String s = switch (disconnectReason) {
                        case CONNECTION_LOST -> "Verbindung verloren: Zeitüberschreitung.";
                        case SERVER_CLOSED -> "Verbindung verloren: Server geschlossen.";
                        case CLIENT_CLOSED -> "Verbindung verloren: Spiel verlassen";
                        case KICKED -> "Verbindung verloren: Von anderem Spieler gekickt";
                        case SERVER_FULL -> "Verbindung verloren: Der Server ist voll.";
                        case GAME_RUNNING -> "Verbindung verloren: Das Spiel wurde schon gestartet.";
                        default -> "Verbindung verloren: Keine weiteren Informationen.";
                    };
                    new Thread(() -> JOptionPane.showMessageDialog(root, s, "Verbindung verloren: " + player.getName(), JOptionPane.WARNING_MESSAGE)).start();
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
                    ClientTrade.updateOwner(this);
                }
                case START -> Monopoly.INSTANCE.setState(GameState.RUNNING);
                case NULL -> {
                }
                default -> throw new RuntimeException();
            }
        } catch (IOException e) {
            System.out.println("[Client]: Connection lost: No further information.");
            clientThread.interrupt();
            pingThread.interrupt();
            throw new RuntimeException(e);
        }
    }

    public Socket socket() {
        return client;
    }

    private ClientPlayer player() {
        return player;
    }

    public long getPing() {
        return ping;
    }

    public void close() {
        try {
            Message.send(new Message(DisconnectReason.CLIENT_CLOSED, MessageType.DISCONNECT), client);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        clientThread.interrupt();
        pingThread.interrupt();
    }

    public boolean closed() {
        return client.isClosed();
    }

    public IServer serverMethod() {
        return serverInterface;
    }

}