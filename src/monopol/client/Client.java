package monopol.client;

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

public class Client {
    private final Socket client;
    private final IEvents eventsInterface;
    private final IServer serverInterface;
    public final boolean isHost;
    public DisconnectReason disconnectReason = null;
    public String name = null;

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
            this.isHost = isHost;
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
                case PRINTLN:
                    System.out.println(message.getMessage()[0]);
                    break;
                case PING:
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    Object[] array = new Object[1];
                    array[0] = message.getMessage()[0];
                    output.writeUTF(Json.toString(new Message(array, MessageType.PING_BACK), false));
                    break;
                case PING_BACK:
                    long delay = System.currentTimeMillis() - (long) message.getMessage()[0];
                    System.out.println("[Server]: Your ping is " + delay + "ms");
                    break;
                case NAME:
                    if(name == null) name = (String) message.getMessage()[0];
                    break;
                case DISCONNECT:
                    disconnectReason = DisconnectReason.valueOf((String) message.getMessage()[0]);
                    clientThread.interrupt();
                    switch (DisconnectReason.valueOf((String) message.getMessage()[0])) {
                        case CONNECTION_LOST -> System.out.println("[Client]: Connection lost: Timed out.");
                        case SERVER_CLOSED -> System.out.println("[Client]: Connection lost: Server closed.");
                        case CLIENT_CLOSED -> System.out.println("[Client]: Connection lost: Left game");
                        case KICKED -> System.out.println("[Client]: Connection lost: Kicked by other player.");
                        default -> System.out.println("[Client]: Connection lost: No further information.");
                    }
                    break;
                case START:
                    Monopoly.INSTANCE.setState(GameState.RUNNING);
                    break;
                case NULL:
                    break;
                default:
                    throw new RuntimeException();
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