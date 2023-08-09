package monopol.test;

import monopol.server.Server;

public class TestA {
    public static Server server;
    public static void main(String[] args) {
        server = new Server(25565);
        server.start();
    }
}