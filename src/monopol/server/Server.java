package monopol.server;

import monopol.common.Player;
import monopol.common.data.IPurchasable;
import monopol.common.data.Plant;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.core.GameState;
import monopol.common.core.Monopoly;
import monopol.common.log.ServerLogger;
import monopol.common.packets.PacketManager;
import monopol.common.packets.ServerSide;
import monopol.common.packets.custom.AskRejoinS2CPacket;
import monopol.common.packets.custom.RejoinStatusS2CPacket;
import monopol.common.packets.custom.RequestRejoinC2SPacket;
import monopol.common.packets.custom.UpdateOwnerS2CPacket;
import monopol.common.utils.MapUtils;
import monopol.server.rules.BuildRule;
import monopol.server.rules.Events;
import monopol.server.rules.OwnedCardsOfColorGroup;
import monopol.server.rules.StandardEvents;
import monopol.common.utils.Json;
import monopol.common.message.Message;
import monopol.common.message.MessageType;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class Server extends UnicastRemoteObject implements IServer {
    public static final int CLIENT_TIMEOUT = 5000;

    public ServerSocket server;
    private boolean pause = true;
    private final List<Player> waitForRejoin = new ArrayList<>();
    private String ip;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    public final HashMap<Player, Socket> players = new HashMap<>();
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
                    if (!waitForRejoin.isEmpty()) {
                        new Thread(() -> {
                            PacketManager.send(new AskRejoinS2CPacket(waitForRejoin.stream().map(Player::getName).toList()), newClient, e -> e.printStackTrace(System.err));
                            try {
                                DataInputStream input = new DataInputStream(newClient.getInputStream());
                                Message message;
                                do {
                                    String data = input.readUTF();
                                    message = Json.toObject(data, Message.class);
                                } while (message.getMessageType() != MessageType.PACKET);
                                RequestRejoinC2SPacket packet = (RequestRejoinC2SPacket) PacketManager.packet(message.getMessage());
                                for (Player player : waitForRejoin) {
                                    if (player.getName().equals(packet.name())) {
                                        clients.put(clients.size() + 1, newClient);
                                        Message.send(new Message(player.getName(), MessageType.NAME), newClient);
                                        Message.send(new Message(null, MessageType.START), newClient);
                                        logger.log().info("[Server]: New Client rejoined (" + player.getName() + ")");
                                        //TODO: send necessary information
                                        PacketManager.sendS2C(new UpdateOwnerS2CPacket(), p -> true, e -> {});
                                        return;
                                    }
                                }
                                throw new IllegalStateException();
                            } catch (Exception e) {
                                try {
                                    Message.send(new Message(DisconnectReason.SERVER_FULL, MessageType.DISCONNECT), newClient);
                                    e.printStackTrace();
                                } catch (IOException ignored) {}
                            }
                        }).start();
                    }
                    else if (acceptNewClients && clients.size() < 6) {
                        clients.put(clients.size() + 1, newClient);
                        Player player = newServerPlayer();
                        players.put(player, newClient);
                        Message.send(new Message(player.getName(), MessageType.NAME), newClient);
                        logger.log().info("[Server]: New Client accepted (" + player.getName() + ")");
                    } else {
                        logger.log().info("[Server]: New Client declined");
                        if (pause)
                            Message.send(new Message(DisconnectReason.SERVER_CLOSED, MessageType.DISCONNECT), newClient);
                        else if (Monopoly.INSTANCE.getState() == GameState.RUNNING)
                            Message.send(new Message(DisconnectReason.GAME_RUNNING, MessageType.DISCONNECT), newClient);
                        else if (clients.size() >= 10)
                            Message.send(new Message(DisconnectReason.SERVER_FULL, MessageType.DISCONNECT), newClient);
                        else Message.send(new Message(DisconnectReason.UNKNOWN, MessageType.DISCONNECT), newClient);
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    logger.log().severe("[Server]: Server crashed due to an Exception:\r\n" + e.getMessage());
                    close();
                    return;
                }
                acceptNewClients = Monopoly.INSTANCE.getState() == GameState.LOBBY;
                if (pause) acceptNewClients = false;
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        private Player newServerPlayer() {
            String name;
            int i = 1;
            boolean okay;
            do {
                okay = true;
                name = "Player " + i;
                for (Map.Entry<Player, Socket> entry : players.entrySet()) {
                    if (entry.getKey().getName().equals(name)) {
                        okay = false;
                        break;
                    }
                }
                i++;
            } while (!okay);
            Player player = new Player(name);
            return player;
        }
    };
    private final Thread requestThread = new Thread(() -> {
        HashMap<Socket, Thread> threadMap = new HashMap<>();
        while(!Thread.interrupted()) {
            if(!pause) {
                clients.values().forEach(socket -> {
                    if (!threadMap.containsKey(socket)) threadMap.put(socket, new Thread(() -> {
                        while (!Thread.interrupted()) {
                            try {
                                DataInputStream input = new DataInputStream(socket.getInputStream());
                                String data = input.readUTF();
                                logger.log().fine("[Server]: Message received");
                                messageReceived(data, socket);
                            } catch (IOException e) {
                                if(clients.containsValue(socket)) e.printStackTrace(System.err);
                            }
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }));
                });
                List<Socket> toRemove = new ArrayList<>();
                threadMap.keySet().forEach(socket -> {
                    if (!clients.containsValue(socket)) {
                        threadMap.get(socket).interrupt();
                        toRemove.add(socket);
                    }
                });
                toRemove.forEach(threadMap::remove);
                threadMap.values().forEach(thread -> {
                    if(!thread.isAlive()) thread.start();
                });
            }
            int status = waitForRejoin.size();
            waitForRejoin.removeIf(player -> !players.containsKey(player) || players.get(player) != null);
            waitForRejoin.addAll(players.keySet().stream().filter(player -> !waitForRejoin.contains(player) && players.get(player) == null).toList());
            if (status != waitForRejoin.size()) {
                PacketManager.sendS2C(new RejoinStatusS2CPacket(waitForRejoin.stream().map(Player::getName).toList()), player -> true, e -> e.printStackTrace(System.err));
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                return;
            }
        }
    });

    private final Thread pingThread = new Thread(() -> {
        while(!Thread.interrupted()) {
            if (!pause) {
                List<Socket> kick = new ArrayList<>();
                clients.forEach((id, client) -> {
                    if (!pingCheck.containsKey(client)) pingCheck.put(client, true);
                    if (!pingCheck.get(client)) kick.add(client);
                    pingCheck.replace(client, false);
                    try {
                        Message.sendPing(client);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                });
                for (Socket client : kick) {
                    String name = "unknown";
                    for(Map.Entry<Player, Socket> entry : players.entrySet()) {
                        if(entry.getValue() == client) name = entry.getKey().getName();
                    }
                    logger.log().warning("[Server]: Client lost connection: timed out (" + name + ")");
                    kick(client, DisconnectReason.CONNECTION_LOST);
                }
            }
            try {
                Thread.sleep(Server.CLIENT_TIMEOUT);
            } catch (InterruptedException e) {
                return;
            }
        }
    });

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
            e.printStackTrace(System.err);
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
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            kick(client, DisconnectReason.SERVER_CLOSED);
        }
        pause = true;
        logger.log().warning("[Server]: Server closed...");
    }

    public void remove(String name) {
        List<Player> list = players.keySet().stream().filter(player -> players.get(player) == null && player.getName().equals(name)).toList();
        if (list.size() == 1) {
            players.remove(list.get(0));
        }
    }

    public void kick(Socket client, DisconnectReason reason) {
        if(!clients.containsValue(client)) throw new IllegalStateException();
        int id = MapUtils.key(clients, client);
        try {
            Message.send(new Message(reason, MessageType.DISCONNECT), client);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        for(int i = id; i < clients.size(); i++) {
            clients.replace(i, clients.get(i + 1));
        }
        clients.remove(clients.size());
        for(Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getValue() == client) {
                if(entry.getKey().getName().equals(host)) close();
                players.replace(entry.getKey(), null);
                if(Monopoly.INSTANCE.getState() == GameState.LOBBY) players.remove(entry.getKey());
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
                case PACKET -> PacketManager.handle(message, new ServerSide(this));
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
                    for(Map.Entry<Player, Socket> entry : players.entrySet()) {
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
            e.printStackTrace(System.err);
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
    public ArrayList<Player> getPlayers() throws RemoteException {
        ArrayList<Player> list = new ArrayList<>(players.keySet());
        list.removeIf(serverPlayer -> players.get(serverPlayer) == null);
        return list;
    }

    @Override
    public Player getServerPlayer(String name) throws RemoteException {
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
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
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getKey().getName().equals(name)) {
                kick(entry.getValue(), reason);
                return;
            }
        }
    }

    @Override
    public boolean changeName(String oldName, String newName) throws RemoteException {
        if(newName.length() > 15) return false;
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getKey().getName().equals(newName)) return false;
        }
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
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
        for (Map.Entry<Player, Socket> entrySet : players.entrySet()) {
            if(entrySet.getKey().getName().equals(name)) Message.send(new Message(value, type), entrySet.getValue());
        }
    }

    @Override
    public void sendMessage(String name, MessageType type, Object value) throws IOException {
        for (Map.Entry<Player, Socket> entrySet : players.entrySet()) {
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
        for(Player player : players.keySet()) {
            if(player.getName().equals(player1)) {
                player.contractMoney(money1);
                player.addMoney(money2);
            }
            if(player.getName().equals(player2)) {
                player.contractMoney(money2);
                player.addMoney(money1);
            }
        }
        PacketManager.sendS2C(new UpdateOwnerS2CPacket(), player -> true, e -> {});
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
        for (Socket socket : players.values()) {
            Message.send(new Message(null, MessageType.START), socket);
            PacketManager.sendS2C(new UpdateOwnerS2CPacket(), player -> true, e -> {});
        }
    }
}
