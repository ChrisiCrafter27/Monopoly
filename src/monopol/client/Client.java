package monopol.client;

import monopol.server.DisconnectReason;
import monopol.server.rules.EventsInterface;
import monopol.utils.Json;
import monopol.utils.message.Message;
import monopol.utils.message.MessageType;

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
            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            eventsInterface = (EventsInterface) registry.lookup("Events");
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
                case DISCONNECT:
                    clientThread.interrupt();
                    switch (DisconnectReason.valueOf((String) message.getMessage()[0])) {
                        case CONNECTION_LOST -> System.out.println("[Server]: Connection lost: Timed out.");
                        case SERVER_CLOSED -> System.out.println("[Server]: Connection lost: Server closed.");
                        case CLIENT_CLOSED -> System.out.println("[Server]: Connection lost: Left game");
                        case KICKED -> System.out.println("[Server]: Connection lost: Kicked by host.");
                        default -> System.out.println("[Server]: Connection lost: No further information.");
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

    public EventsInterface triggerEvent() {
        return eventsInterface;
    }

    public static void main(String[] args) throws NotBoundException {
        Client c = new Client("localhost", 25565);
        try {
            //Message.sendPing(c.client);
            c.triggerEvent().printSth("Print this");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}