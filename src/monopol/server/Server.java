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
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface {
    public static final int CLIENT_TIMEOUT = 10000;

    public ServerSocket server;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    public final HashMap<ServerPlayer, Socket> serverPlayers = new HashMap<>();
    private Registry registry1, registry2;
    public final ServerLogger logger = ServerLogger.INSTANCE;
    private boolean acceptNewClients = false;
    public ServerSettings serverSettings;

    private final Thread connectionThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    logger.getLogger().info("[Server]: Waiting for new Client");
                    Socket newClient = server.accept();
                    if (clients.containsValue(newClient)) continue;
                    if(acceptNewClients) {
                        logger.getLogger().info("[Server]: New Client accepted");
                        clients.put(clients.size() + 1, newClient);
                        ServerPlayer serverPlayer = new ServerPlayer("Spieler " + (serverPlayers.size() + 1));
                        serverPlayers.put(serverPlayer, newClient);
                        Message.send(new Message(serverPlayer.getName(), MessageType.NAME), newClient);
                    } else {
                        logger.getLogger().info("[Server]: New Client denied");
                        if(Monopoly.INSTANCE.getState() == GameState.RUNNING) Message.send(new Message(DisconnectReason.GAME_RUNNING, MessageType.DISCONNECT), newClient); else if(clients.size() >= 10) Message.send(new Message(DisconnectReason.SERVER_FULL, MessageType.DISCONNECT), newClient); else Message.send(new Message(DisconnectReason.UNKNOWN, MessageType.DISCONNECT), newClient);
                    }
                } catch (Exception e) {
                    logger.getLogger().severe("[Server]: Server crashed due to an Exception\r\n" + e.getMessage());
                    close();
                    return;
                }
                acceptNewClients = Monopoly.INSTANCE.getState() == GameState.LOBBY || Monopoly.INSTANCE.getState() == GameState.WAITING_FOR_PLAYER;
                if(clients.size() >= 10) acceptNewClients = false;
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
                } catch (InterruptedException e) {
                    return;
                }
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
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    };

    public Server(int port, ServerSettings serverSettings) throws IOException{
        logger.getLogger().info("[Server]: Staring server...");
        server = new ServerSocket(port);
        //server.setSoTimeout(10000);

        connectionThread.start();
        requestThread.start();
        pingThread.start();

        StandardEvents events = new StandardEvents(false, -1, false, true, true, true, 1500, 200, true, true, true, true, true, BuildRule.ON_COLOR_GROUP, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL);
        try {
            registry1 = LocateRegistry.createRegistry(1099);
            registry1.rebind("Events", events);
            registry2 = LocateRegistry.createRegistry(1199);
            registry2.rebind("Server", this);
        } catch(Exception ignored) {
            try {
                registry1 = LocateRegistry.createRegistry(1099);
                registry1.rebind("Events", events);
                registry2 = LocateRegistry.createRegistry(1199);
                registry2.rebind("Server", this);
            } catch(Exception e) {
                close();
                throw new RuntimeException(e);
            }
        }

        this.serverSettings = serverSettings;
        /*
        logger.getLogger().info("[Server]: Server online!");
        logger.getLogger().severe("[Server]: Failed to start server\r\n" + e.getMessage());
        close();
        */
    }

    public void open() {
        try {
            logger.getLogger().info("[Server]: IP-Address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            logger.getLogger().severe("[Server]: Server crashed due to an Exception\r\n" + e.getMessage());
            close();
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
        connectionThread.interrupt();
        requestThread.interrupt();
        pingThread.interrupt();
        try {
            server.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            registry1.unbind("Events");
            registry2.unbind("Server");
            UnicastRemoteObject.unexportObject(registry1, true);
            UnicastRemoteObject.unexportObject(registry2, true);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
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
        if(clients.isEmpty()) close();
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
            close();
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

    @Override
    public boolean changeName(String oldName, String newName) {
        if(newName.contains("Spieler")) return false;
        if(newName.length() > 20) return false;
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(newName)) return false;
        }
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(oldName)) {
                entry.getKey().setName(newName);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsNewClient() throws RemoteException {
        return acceptNewClients;
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        Server server = new Server(25565, new ServerSettings(false, true));
        server.open();

        //server.connectionThread.interrupt();
        //server.requestThread.interrupt();
        //server.pingThread.interrupt();

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }
}
