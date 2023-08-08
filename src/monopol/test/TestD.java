package monopol.test;

import monopol.utils.Message;
import monopol.server.Server;

import java.io.IOException;

public class TestD {
    public static void main(String[] args) {
        Server server = new Server(25565);
        server.start();
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(server.clients.containsKey(1)) {
                        try {
                            Message.sendPing(server.clients.get(1));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        thread.start();
    }
}
