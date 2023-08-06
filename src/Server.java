import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket server;

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            System.out.println("IP-Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                System.out.println("Waiting for client at port " + server.getLocalPort());
                Socket client = server.accept();
                DataInputStream input = new DataInputStream(client.getInputStream());
                messageReceived(input.readUTF());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
                Object[] array = new Object[1];
                array[0] = "*** Das sollte der Client ausgeben ***";
                Message message = new Message(array, MessageType.PRINTLN);
                output.writeUTF(Json.toString(message, false));
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
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
            case NULL:
                break;
            default:
                throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        Server s = new Server(25565);
        s.start();
    }
}