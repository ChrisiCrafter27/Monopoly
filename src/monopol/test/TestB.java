package monopol.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import monopol.utils.Json;
import monopol.utils.message.Message;
import monopol.utils.message.MessageType;
import monopol.server.Server;

public class TestB {
    public static void main(String[] args) {
        try {
            Object[] array = new Object[1];
            array[0] = new Server(25565);
            System.out.println(Json.toString(array, true));
            System.out.println("--------------------------------------------------");
            System.out.println(Json.toString(new Message(array, MessageType.NULL), true));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
