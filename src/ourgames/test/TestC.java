package ourgames.test;

import ourgames.*;

import java.io.IOException;
import java.net.Socket;

public class TestC {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 25565);
        Message.sendString("Print this", MessageType.PRINTLN, socket);
    }
}
