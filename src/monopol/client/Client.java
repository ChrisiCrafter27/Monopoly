package monopol.client;

import monopol.server.DisconnectReason;
import monopol.rules.EventsInterface;
import monopol.server.ServerInterface;
import monopol.utils.Json;
import monopol.message.Message;
import monopol.message.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private final Socket client;
    private final EventsInterface eventsInterface;
    private final ServerInterface serverInterface;
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

    public Client(String ip, int port) throws NotBoundException {
        try {
            client = new Socket(ip, port);
            clientThread.start();
            Registry registry1 = LocateRegistry.getRegistry(ip, 1299);
            eventsInterface = (EventsInterface) registry1.lookup("Events");
            Registry registry2 = LocateRegistry.getRegistry(ip, 1199);
            serverInterface = (ServerInterface) registry2.lookup("Server");
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

    public EventsInterface eventMethod() {
        return eventsInterface;
    }
    public ServerInterface serverMethod() {
        return serverInterface;
    }

    public static void main(String[] args) throws NotBoundException {
        Client c = new Client("localhost", 25565);
        try {
            //Message.sendPing(c.client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}