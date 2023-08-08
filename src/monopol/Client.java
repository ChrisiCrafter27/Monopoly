package monopol;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        switch (message.getMessageType()) {
            case PRINTLN:
                System.out.println(message.getMessage()[0]);
                break;
            case PING:
                long delay = System.currentTimeMillis() - (long) message.getMessage()[0];
                System.out.println("[Server]: Your ping: " + delay + "ms");
                break;
            case NULL:
                break;
            default:
                throw new RuntimeException();
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
            Message.sendPing(c.client);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}