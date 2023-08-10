package monopol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;

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

    public Message(Object object, MessageType messageType) {
        objects = new Object[1];
        objects[0] = object;
        this.messageType = messageType;
    }

    public Object[] getMessage() {
        return objects;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public static void sendString(String value, MessageType messageType, Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Object[] array = new Object[1];
        array[0] = value;
        Message message = new Message(array, messageType);
        output.writeUTF(Json.toString(message, false));
    }

    public static void send(Message message, Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        output.writeUTF(Json.toString(message, false));
    }

    public static void sendPing(Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Object[] array = new Object[1];
        array[0] = System.currentTimeMillis();
        Message message = new Message(array, MessageType.PING);
        output.writeUTF(Json.toString(message, false));
    }

    public static void sendTypeOnly(MessageType messageType, Socket client) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Object[] array = new Object[0];
        Message message = new Message(array, messageType);
        output.writeUTF(Json.toString(message, false));
    }
}
