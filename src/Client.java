import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            Socket client = new Socket("localhost", 25565);
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            Object[] array = new Object[1];
            array[0] = "*** Das sollte der Server ausgeben ***";
            Message message = new Message(array, MessageType.PRINTLN);
            output.writeUTF(Json.toString(message, false));
            DataInputStream input = new DataInputStream(client.getInputStream());
            messageReceived(input.readUTF());
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void messageReceived(String value) {
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
            case NULL:
                break;
            default:
                throw new RuntimeException();
        }
    }
}