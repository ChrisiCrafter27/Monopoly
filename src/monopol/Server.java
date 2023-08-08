package monopol;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Server {
    public final ServerSocket server;
    public final HashMap<Integer, Socket> clients = new HashMap<>();

    private final Thread connectionThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted())
            {
                try {
                    System.out.println("Waiting for client at port " + server.getLocalPort());
                    Socket newClient = server.accept();
                    if(clients.containsValue(newClient)) continue;
                    clients.put(clients.size() + 1, newClient);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }
        }
    };
    private final Thread serverThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted())
            {
                clients.forEach((id, client) -> {
                    try {
                        DataInputStream input = new DataInputStream(client.getInputStream());
                        try {
                            String data = input.readUTF();
                            messageReceived(data, client);
                        } catch (IOException e) {}
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                });
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            //server.setSoTimeout(100000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void start() {
        try {
            System.out.println("IP-Address: " + InetAddress.getLocalHost().getHostAddress());
            if(!serverActive()) serverThread.start();
            if(!serverActive()) connectionThread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("[Server]: Failed to print out IP address");
        }
    }

    private void messageReceived(String value, Socket client) {
        Message message;
        try {
            message = Json.toObject(value, Message.class);
            switch (message.getMessageType()) {
                case PRINTLN:
                    System.out.println(message.getMessage()[0]);
                    break;
                case PING:
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    Object[] array = new Object[1];
                    array[0] = message.getMessage()[0];
                    output.writeUTF(Json.toString(new Message(array, MessageType.PING_BACK), false));
                    break;
                case PING_BACK:
                    long delay = System.currentTimeMillis() - (long) message.getMessage()[0];
                    System.out.println("[Server]: Ping to " + client.getInetAddress().getHostAddress() + " is " + delay + "ms");
                    break;
                case DISCONNECT:
                    if(!clients.containsValue(client)) throw new RuntimeException();
                    clients.forEach((k, v) -> {
                        if(v == client) {
                            int id = k;
                            for(int i = id; i < clients.size(); i++) {
                                clients.replace(i, clients.get(i + 1));
                            }
                            clients.remove(clients.size());
                        }
                    });
                    break;
                case NULL:
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean serverActive() {
        return !serverThread.isInterrupted() && serverThread.isAlive() && !connectionThread.isInterrupted() && connectionThread.isAlive();
    }

    public static void main(String[] args) {
        Server s = new Server(25565);
        s.start();
    }
}