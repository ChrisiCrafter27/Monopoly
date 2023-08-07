package ourgames.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import ourgames.Json;
import ourgames.Message;
import ourgames.MessageType;
import ourgames.Server;

public class TestB {
    public static void main(String[] args) {
        try {
            Object[] array = new Object[1];
            array[0] = new Server(25565);
            System.out.println(Json.toString(new Message(array, MessageType.NULL), true));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
