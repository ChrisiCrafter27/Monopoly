package ourgames;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Message {
    public Object[] objects;
    public MessageType messageType;

    public Message() {}

    public Message(Object[] objects, MessageType messageType) {
        this.objects = objects;
        this.messageType = messageType;
    }

    public Object[] getMessage() {
        return objects;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public static void sendString(Object value, MessageType messageType, Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Object[] array = new Object[1];
        array[0] = value;
        Message message = new Message(array, messageType);
        output.writeUTF(Json.toString(message, false));
    }

    public static void sendString(String value, MessageType messageType, Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Object[] array = new Object[1];
        array[0] = value;
        Message message = new Message(array, messageType);
        output.writeUTF(Json.toString(message, false));
    }

    public static void sendPing(Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Object[] array = new Object[1];
        array[0] = System.currentTimeMillis();
        Message message = new Message(array, MessageType.PING);
        output.writeUTF(Json.toString(message, false));
    }
}
