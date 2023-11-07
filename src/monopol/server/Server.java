package monopol.server;

import monopol.annotations.ServerOnly;
import monopol.data.IPurchasable;
import monopol.data.Plant;
import monopol.data.Street;
import monopol.data.TrainStation;
import monopol.core.GameState;
import monopol.core.Monopoly;
import monopol.log.ServerLogger;
import monopol.rules.BuildRule;
import monopol.rules.Events;
import monopol.rules.OwnedCardsOfColorGroup;
import monopol.rules.StandardEvents;
import monopol.utils.Json;
import monopol.message.Message;
import monopol.message.MessageType;
import monopol.utils.ProjectStructure;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements IServer {
    public static final int CLIENT_TIMEOUT = 5000;

    public ServerSocket server;
    private boolean pause = true;
    private String ip;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    public final HashMap<ServerPlayer, Socket> serverPlayers = new HashMap<>();
    public final ServerLogger logger = ServerLogger.INSTANCE;
    private boolean acceptNewClients = false;
    public ServerSettings serverSettings;
    private String host;
    private Events events = new StandardEvents(true, -1, true, false, true, true, 1000, 200, true, true, false, true, false, BuildRule.ANYWHERE, OwnedCardsOfColorGroup.NONE, OwnedCardsOfColorGroup.NONE, OwnedCardsOfColorGroup.NONE, OwnedCardsOfColorGroup.NONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL);

    private final Thread connectionThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    Socket newClient = server.accept();
                    if (clients.containsValue(newClient)) continue;
                    if (acceptNewClients) {
                        clients.put(clients.size() + 1, newClient);
                        ServerPlayer serverPlayer = newServerPlayer();
                        serverPlayers.put(serverPlayer, newClient);
                        Message.send(new Message(serverPlayer.getName(), MessageType.NAME), newClient);
                        logger.log().info("[Server]: New Client accepted (" + serverPlayer.getName() + ")");
                    } else {
                        logger.log().info("[Server]: New Client denied");
                        if (pause)
                            Message.send(new Message(DisconnectReason.SERVER_CLOSED, MessageType.DISCONNECT), newClient);
                        else if (Monopoly.INSTANCE.getState() == GameState.RUNNING)
                            Message.send(new Message(DisconnectReason.GAME_RUNNING, MessageType.DISCONNECT), newClient);
                        else if (clients.size() >= 10)
                            Message.send(new Message(DisconnectReason.SERVER_FULL, MessageType.DISCONNECT), newClient);
                        else Message.send(new Message(DisconnectReason.UNKNOWN, MessageType.DISCONNECT), newClient);
                    }
                } catch (Exception e) {
                    logger.log().severe("[Server]: Server crashed due to an Exception:\r\n" + e.getMessage());
                    close();
                    return;
                }
                acceptNewClients = Monopoly.INSTANCE.getState() == GameState.LOBBY || Monopoly.INSTANCE.getState() == GameState.WAITING_FOR_PLAYER;
                if (pause) acceptNewClients = false;
                if (clients.size() >= 10) acceptNewClients = false;
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        private ServerPlayer newServerPlayer() {
            String name;
            int i = 1;
            boolean okay;
            do {
                okay = true;
                name = "Player " + i;
                for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
                    if (entry.getKey().getName().equals(name)) {
                        okay = false;
                        break;
                    }
                }
                i++;
            } while (!okay);
            ServerPlayer serverPlayer = new ServerPlayer(name);
            return serverPlayer;
        }
    };
    private final Thread requestThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                if(!pause) {
                    try {
                        clients.forEach((id, client) -> {
                            try {
                                DataInputStream input = new DataInputStream(client.getInputStream());
                                String data = input.readUTF();
                                logger.log().fine("[Server]: Message received");
                                messageReceived(data, client);
                            } catch (IOException ignored) {
                            }
                        });
                    } catch (ConcurrentModificationException ignored) {
                    }
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    };

    private final Thread pingThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                if (!pause) {
                    List<Socket> kick = new ArrayList<>();
                    clients.forEach((id, client) -> {
                        if (!pingCheck.containsKey(client)) pingCheck.put(client, true);
                        if (!pingCheck.get(client)) kick.add(client);
                        pingCheck.replace(client, false);
                        try {
                            Message.sendPing(client);
                        } catch (IOException ignored) {
                        }
                    });
                    for (Socket client : kick) {
                        String name = "unknown";
                        for(Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
                            if(entry.getValue() == client) name = entry.getKey().getName();
                        }
                        logger.log().warning("[Server]: Client lost connection: timed out (" + name + ")");
                        kick(client, DisconnectReason.CONNECTION_LOST);
                    }
                    try {
                        sleep(Server.CLIENT_TIMEOUT);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    };

    public Server(int port) throws IOException{
        logger.log().info("[Server]: Initialing server...");
        server = new ServerSocket(port);
        //server.setSoTimeout(10000);

        serverSettings = new ServerSettings(false, false);

        connectionThread.start();
        requestThread.start();
        pingThread.start();

        StandardEvents events = new StandardEvents(false, -1, false, true, true, true, 1500, 200, true, true, true, true, true, BuildRule.ON_COLOR_GROUP, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL);
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(1199);
            registry.rebind("Server", this);
        } catch(Exception ignored) {
            try {
                registry = LocateRegistry.createRegistry(1199);
                registry.rebind("Server", this);
            } catch(Exception e) {
                logger.log().severe("[Server]: Failed to start server\r\n" + e.getMessage());
                close();
                throw new RuntimeException(e);
            }
        }
    }

    public void open(ServerSettings serverSettings) {
        Monopoly.INSTANCE.setState(GameState.LOBBY);
        logger.log().info("[Server]: Starting server...");
        try {
            logger.log().info("[Server]: IP-Address: " + InetAddress.getLocalHost().getHostAddress());
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.log().severe("[Server]: Server crashed due to an Exception:\r\n" + e.getMessage());
            close();
            throw new RuntimeException();
        }
        this.serverSettings = serverSettings;
        pause = false;
        acceptNewClients = true;
        logger.log().info("[Server]: Listening for new clients...");
    }

    public void close() {
        acceptNewClients = false;
        List<Socket> list = new ArrayList<>();
        clients.forEach((id, client) -> list.add(client));
        for (Socket client : list) {
            try {
                Message.send(new Message(DisconnectReason.SERVER_CLOSED, MessageType.DISCONNECT), client);
            } catch (IOException ignored) {}
            kick(client, DisconnectReason.SERVER_CLOSED);
        }
        pause = true;
        logger.log().warning("[Server]: Server closed...");
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
        logger.log().warning("[Server]: Kicked client");
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
                    String name = "unknown";
                    for(Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
                        if(entry.getValue() == client) name = entry.getKey().getName();
                    }
                    if(delay < 2500) logger.log().fine("[Server]: Ping to " + name + " is " + delay + "ms");
                    else logger.log().warning("[Server]: Ping to " + name + " is " + delay + "ms");
                }
                case DISCONNECT -> kick(client, DisconnectReason.CLIENT_CLOSED);
                case NULL -> {}
                default -> throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.log().severe("[Server]: Server crashed due to an Exception:\r\n" + e.getMessage());
            close();
            throw new RuntimeException(e);
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public ServerSettings getServerSettings() throws RemoteException {
        return serverSettings;
    }

    @Override
    public ArrayList<ServerPlayer> getServerPlayers() throws RemoteException {
        ArrayList<ServerPlayer> list = new ArrayList<>(serverPlayers.keySet());
        list.removeIf(serverPlayer -> serverPlayers.get(serverPlayer) == null);
        return list;
    }

    @Override
    public ServerPlayer getServerPlayer(String name) throws RemoteException {
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String getIp() throws RemoteException {
        return ip;
    }

    @Override
    public void kick(String name, DisconnectReason reason) throws RemoteException {
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(name)) kick(entry.getValue(), reason);
        }
    }

    @Override
    public boolean changeName(String oldName, String newName) throws RemoteException {
        if(newName.length() > 15) return false;
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(newName)) return false;
        }
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            if(entry.getKey().getName().equals(oldName)) {
                entry.getKey().setName(newName);
                if(oldName.equals(host)) host = newName;
                logger.log().info("[Server]: Changed name from " + oldName + " to " + newName);
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendMessage(String name, MessageType type, Object[] value) throws IOException {
        for (Map.Entry<ServerPlayer, Socket> entrySet : serverPlayers.entrySet()) {
            if(entrySet.getKey().getName().equals(name)) Message.send(new Message(value, type), entrySet.getValue());
        }
    }

    @Override
    public void sendMessage(String name, MessageType type, Object value) throws IOException {
        for (Map.Entry<ServerPlayer, Socket> entrySet : serverPlayers.entrySet()) {
            if(entrySet.getKey().getName().equals(name)) Message.send(new Message(value, type), entrySet.getValue());
        }
    }

    @Override
    public HashMap<IPurchasable, String> getOwnerMap() throws RemoteException {
        HashMap<IPurchasable, String> ownerMap = new HashMap<>();
        for(Street street : Street.values()) {
            ownerMap.put(street, street.getOwner());
        }
        for(TrainStation trainStation : TrainStation.values()) {
            ownerMap.put(trainStation, trainStation.getOwner());
        }
        for(Plant plant : Plant.values()) {
            ownerMap.put(plant, plant.getOwner());
        }
        return ownerMap;
    }

    @Override
    public boolean trade(String player1, String player2, ArrayList<IPurchasable> offer1, ArrayList<IPurchasable> offer2, int money1, int money2) throws RemoteException {
        for (IPurchasable purchasable : offer1) {
            if(!purchasable.getOwner().equals(player1)) return false;
        }
        for (IPurchasable purchasable : offer2) {
            if(!purchasable.getOwner().equals(player2)) return false;
        }
        for (IPurchasable purchasable : offer1) {
            purchasable.setOwner(player2);
        }
        for (IPurchasable purchasable : offer2) {
            purchasable.setOwner(player1);
        }
        for(ServerPlayer serverPlayer : serverPlayers.keySet()) {
            if(serverPlayer.getName().equals(player1)) {
                serverPlayer.contractMoney(money1);
                serverPlayer.addMoney(money2);
            }
            if(serverPlayer.getName().equals(player2)) {
                serverPlayer.contractMoney(money2);
                serverPlayer.addMoney(money1);
            }
        }
        return true;
    }

    @Override
    public boolean triggerEvent(String methodName, Object... args) throws RemoteException {
        if(events == null) return false;
        if(Modifier.isAbstract(events.getClass().getModifiers())) return false;
        try {
            Method method = events.getClass().getMethod(methodName);
            if(ProjectStructure.isAnnotated(method, ServerOnly.class)) return false;
            if(Modifier.isStatic(method.getModifiers())) return false;
            if(method.getParameterCount() == 0) method.invoke(events);
            else method.invoke(events, args);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean acceptsNewClient() throws RemoteException {
        return acceptNewClients;
    }

    @Override
    public boolean isHost(String name) throws RemoteException {
        return name.equals(host);
    }

    @Override
    public boolean stopped() throws RemoteException {
        return pause;
    }

    @Override
    public void start() throws IOException {
        //TODO initialize the game
        for (Map.Entry<ServerPlayer, Socket> entry : serverPlayers.entrySet()) {
            Message.send(new Message(null, MessageType.START), entry.getValue());
        }
    }
}
