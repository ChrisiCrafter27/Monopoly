package monopol.server;

import monopol.log.ServerLogger;
import monopol.rules.BuildRule;
import monopol.rules.OwnedCardsOfColorGroup;
import monopol.rules.StandardEvents;
import monopol.utils.Json;
import monopol.message.Message;
import monopol.message.MessageType;

import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

public class Server {
    public static final int CLIENT_TIMEOUT = 10000;

    public final ServerSocket server;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    public final ServerLogger logger = ServerLogger.INSTANCE;
    private boolean acceptNewClients = false;

    private final Thread connectionThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                if(acceptNewClients) {
                    try {
                        logger.getLogger().info("[Server]: Waiting for new Client");
                        Socket newClient = server.accept();
                        if (clients.containsValue(newClient)) continue;
                        logger.getLogger().info("[Server]: New Client accepted");
                        clients.put(clients.size() + 1, newClient);
                    } catch (Exception e) {
                        logger.getLogger().severe("[Server]: Server crashed due to an Exception\r\n" + e.getMessage());
                        connectionThread.interrupt();
                        requestThread.interrupt();
                        pingThread.interrupt();
                        throw new RuntimeException();
                    }
                }
            }
        }
    };
    private final Thread requestThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted())
            {
                try {
                    clients.forEach((id, client) -> {
                        try {
                            DataInputStream input = new DataInputStream(client.getInputStream());
                            String data = input.readUTF();
                            logger.getLogger().fine("[Server]: Message received");
                            messageReceived(data, client);
                        } catch (IOException ignored) {}
                    });
                } catch (ConcurrentModificationException ignored) {}
                try {
                    sleep(10);
                } catch (InterruptedException ignored) {}
            }
        }
    };

    private final Thread pingThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted())
            {
                List<Socket> kick = new ArrayList<>();
                clients.forEach((id, client) -> {
                    if(!pingCheck.containsKey(client)) pingCheck.put(client, true);
                    if(!pingCheck.get(client)) kick.add(client);
                    pingCheck.replace(client, false);
                    try {
                        Message.sendPing(client);
                    } catch (IOException ignored) {}
                });
                for (Socket client : kick) {
                    logger.getLogger().warning("[Server]: Client lost connection: timed out");
                    kick(client, DisconnectReason.CONNECTION_LOST);
                }
                try {
                    sleep(Server.CLIENT_TIMEOUT);
                } catch (InterruptedException ignored) {}
            }
        }
    };

    public Server(int port) {
        logger.getLogger().info("[Server]: Staring server...");
        try {
            server = new ServerSocket(port);
            connectionThread.start();
            requestThread.start();
            pingThread.start();
            //server.setSoTimeout(100000);
            StandardEvents events = new StandardEvents(false, -1, false, true, true, true, 1500, 200, true, true, true, true, true, BuildRule.ON_COLOR_GROUP, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Events", events);
            logger.getLogger().info("[Server]: Server online!");
        } catch (IOException e) {
            logger.getLogger().severe("[Server]: Failed to start server\r\n" + e.getMessage());
            connectionThread.interrupt();
            requestThread.interrupt();
            pingThread.interrupt();
            throw new RuntimeException();
        }
    }

    public void open() {
        try {
            logger.getLogger().info("[Server]: IP-Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            logger.getLogger().severe("[Server]: Server crashed due to an Exception\r\n" + e.getMessage());
            connectionThread.interrupt();
            requestThread.interrupt();
            pingThread.interrupt();
            throw new RuntimeException(e);
        }
        acceptNewClients = true;
        logger.getLogger().info("[Server]: Listening for new clients...");
    }

    public void close() {
        acceptNewClients = false;
        List<Socket> list = new ArrayList<>();
        clients.forEach((id, client) -> {
            list.add(client);
        });
        for (Socket client : list) {
            try {
                Message.send(new Message(DisconnectReason.SERVER_CLOSED, MessageType.DISCONNECT), client);
            } catch (IOException ignored) {}
            kick(client, DisconnectReason.SERVER_CLOSED);
        }
        logger.getLogger().warning("[Server]: Server closed...");
    }

    public void kick(Socket client, DisconnectReason reason) {
        if(!clients.containsValue(client)) throw new RuntimeException();
        final int[] idArray = new int[1];
        clients.forEach((k, v) -> {
            if(v == client) {
                idArray[0] = k;
            }
        });
        int id = idArray[0];
        try {
            Message.send(new Message(reason, MessageType.DISCONNECT), client);
        } catch (IOException ignored) {}
        for(int i = id; i < clients.size(); i++) {
            clients.replace(i, clients.get(i + 1));
        }
        clients.remove(clients.size());
        logger.getLogger().warning("[Server]: Kicked client");
    }

    private void messageReceived(String value, Socket client) {
        Message message;
        try {
            message = Json.toObject(value, Message.class);
            switch (message.getMessageType()) {
                case PRINTLN -> System.out.println(message.getMessage()[0]);
                case PING -> {
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    Object[] array = new Object[1];
                    array[0] = message.getMessage()[0];
                    output.writeUTF(Json.toString(new Message(array, MessageType.PING_BACK), false));
                }
                case PING_BACK -> {
                    long delay = System.currentTimeMillis() - (long) message.getMessage()[0];
                    if (pingCheck.containsKey(client)) {
                        pingCheck.replace(client, true);
                    }
                    logger.getLogger().fine("[Server]: Ping to " + client.getInetAddress().getHostAddress() + " is " + delay + "ms");
                }
                case DISCONNECT -> kick(client, DisconnectReason.CLIENT_CLOSED);
                case NULL -> {
                }
                default -> throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.getLogger().severe("[Server]: Server crashed due to an Exception\r\n" + e.getMessage());
            connectionThread.interrupt();
            requestThread.interrupt();
            pingThread.interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server(25565);
        server.open();

        new Thread() {
            @Override
            public void run() {

            }
        }.start();
    }
}
