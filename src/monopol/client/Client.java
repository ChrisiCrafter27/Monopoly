package monopol.client;

import monopol.utils.Json;
import monopol.utils.Message;
import monopol.utils.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private final Socket client;
    private final Thread clientThread = new Thread() {
        @Override
        public void run() {
            while(!interrupted()) {
                try {
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    String data = input.readUTF();
                    messageReceived(data);
                } catch (IOException e) {}
            }
        }
    };

    public Client() {
        try {
            client = new Socket("localhost", 25565);
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
                case TERMINATE:
                    clientThread.interrupt();
                    client.close();
                    System.out.println("[Server]: [ERROR]: Connection terminated!");
                case NULL:
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeUTF(Json.toString(new Message(new Object[0], MessageType.DISCONNECT), false));
            clientThread.interrupt();
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Client c = new Client();
        try {
            //Message.sendPing(c.client);
            //c.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}