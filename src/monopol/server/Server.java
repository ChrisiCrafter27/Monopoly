package monopol.server;

import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.log.ServerLogger;
import monopol.rules.BuildRule;
import monopol.rules.OwnedCardsOfColorGroup;
import monopol.rules.StandardEvents;
import monopol.utils.Json;
import monopol.message.Message;
import monopol.message.MessageType;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface {
    public static final int CLIENT_TIMEOUT = 10000;

    public final ServerSocket server;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    public final HashMap<ServerPlayer, Socket> serverPlayers = new HashMap<>();
    public final ServerLogger logger = ServerLogger.INSTANCE;
    private boolean acceptNewClients = false;
    public ServerSettings serverSettings;

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
                        ServerPlayer serverPlayer = new ServerPlayer("Player " + (serverPlayers.size() + 1));
                        serverPlayers.put(serverPlayer, newClient);
                        Message.send(new Message(serverPlayer.getName(), MessageType.NAME), newClient);
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

    public Server(int port, ServerSettings serverSettings) throws IOException{
        logger.getLogger().info("[Server]: Staring server...");
        server = new ServerSocket(port);

        connectionThread.start();
        requestThread.start();
        pingThread.start();

        StandardEvents events = new StandardEvents(false, -1, false, true, true, true, 1500, 200, true, true, true, true, true, BuildRule.ON_COLOR_GROUP, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL);
        Registry registry1 = LocateRegistry.createRegistry(1099);
        registry1.rebind("Events", events);
        Registry registry2 = LocateRegistry.createRegistry(1199);
        registry2.rebind("Server", this);

        this.serverSettings = serverSettings;
        /*
        logger.getLogger().info("[Server]: Server online!");
        logger.getLogger().severe("[Server]: Failed to start server\r\n" + e.getMessage());
        connectionThread.interrupt();
        requestThread.interrupt();
        pingThread.interrupt();
        */

        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean drin = false;
                while(true) {
                    if(serverPlayers.size() == 1 && serverPlayers.values().toArray()[0] != null) {
                        drin = true;
                        //System.out.println("JETZT DRIN");
                    }
                }
            }
        };
        thread.start();
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
        for(Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getValue() == client) {
                serverPlayers.replace(entry.getKey(), null);
                if(Monopoly.INSTANCE.getState() == GameState.LOBBY) serverPlayers.remove(entry.getKey());
                break;
            }
        }
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

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public ArrayList<ServerPlayer> getServerPlayers() {
        ArrayList<ServerPlayer> list = new ArrayList<>(serverPlayers.keySet());
        list.removeIf(serverPlayer -> serverPlayers.get(serverPlayer) == null);
        return list;
    }

    @Override
    public void kick(String name, DisconnectReason reason) throws RemoteException {
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(name)) kick(entry.getValue(), reason);
        }
    }

    public static void main(String[] args) throws IOException{
        Server server = new Server(25565, new ServerSettings(false, true));
        server.open();

        new Thread() {
            @Override
            public void run() {

            }
        }.start();
    }
}
