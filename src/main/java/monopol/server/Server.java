package monopol.server;

import monopol.common.data.*;
import monopol.common.log.ServerLogger;
import monopol.common.message.IServer;
import monopol.common.message.Message;
import monopol.common.packets.Packet;
import monopol.common.packets.PacketManager;
import monopol.common.packets.ServerSide;
import monopol.common.packets.custom.*;
import monopol.common.packets.custom.update.UpdateFreeParkingS2CPacket;
import monopol.common.packets.custom.update.UpdatePurchasablesS2CPacket;
import monopol.common.packets.custom.update.UpdatePlayerDataS2CPacket;
import monopol.common.packets.custom.update.UpdatePositionS2CPacket;
import monopol.common.utils.Json;
import monopol.common.utils.MapUtils;
import monopol.common.message.DisconnectReason;
import monopol.common.utils.ServerSettings;
import monopol.common.data.Player;
import monopol.server.events.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class Server extends UnicastRemoteObject implements IServer {
    public static final int CLIENT_TIMEOUT = 5000;
    public static final Map<Color, String> COLORS = new HashMap<>();
    static {
        COLORS.putAll(Map.of(Color.YELLOW, "<html><font color=#ffff00>■</font>", Color.ORANGE, "<html><font color=#ffc800>■</font>", Color.RED, "<html><font color=#ff0000>■</font>", Color.MAGENTA, "<html><font color=#ff00ff>■</font>", Color.PINK, "<html><font color=#ffafaf>■</font>"));
        COLORS.putAll(Map.of(Color.CYAN, "<html><font color=#00ffff>■</font>", Color.BLUE, "<html><font color=#0000ff>■</font>", Color.GREEN, "<html><font color=#00ff00>■</font>", Color.WHITE, "<html><font color=#ffffff>■</font>", Color.LIGHT_GRAY, "<html><font color=#c0c0c0>■</font>"));
        COLORS.putAll(Map.of(Color.GRAY, "<html><font color=#808080>■</font>", Color.DARK_GRAY, "<html><font color=#404040>■</font>", Color.BLACK, "<html><font color=#000000>■</font>"));
    }

    public ServerSocket server;
    private ServerState serverState = ServerState.CLOSED;
    private boolean pause = true;
    private final List<Player> waitForRejoin = new ArrayList<>();
    private String ip;
    public final HashMap<Integer, Socket> clients = new HashMap<>();
    public final HashMap<Socket, Boolean> pingCheck = new HashMap<>();
    private final HashMap<Player, Socket> players = new HashMap<>();
    public final ServerLogger logger = ServerLogger.INSTANCE;
    private boolean acceptNewClients = false;
    public ServerSettings serverSettings;
    private String host;
    private boolean hostJoined = false;
    private Events.Factory<?> eventsType = StandardEvents::new;
    private Events events = eventsType.create(false, 16, true, true, true, true, 2500, 200, true, true, true, false, false, BuildRule.ANYWHERE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL_BUT_ONE, OwnedCardsOfColorGroup.ALL);
    private GameData gameData = new GameData();
    
    private final Thread connectionThread = new Thread() {
        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    Socket newClient = server.accept();
                    if (clients.containsValue(newClient)) continue;
                    if (!waitForRejoin.isEmpty()) {
                        new Thread(() -> {
                            PacketManager.sendS2C(new AskRejoinS2CPacket(waitForRejoin.stream().map(Player::getName).toList()), newClient, Throwable::printStackTrace);
                            try {
                                DataInputStream input = new DataInputStream(newClient.getInputStream());
                                Packet<?> p;
                                do {
                                    String data = input.readUTF();
                                    p = PacketManager.packet(Json.toObject(data, Message.class));
                                } while (!(p instanceof RequestRejoinC2SPacket));
                                RequestRejoinC2SPacket packet = (RequestRejoinC2SPacket) p;
                                for (Player player : waitForRejoin) {
                                    if (player.getName().equals(packet.name())) {
                                        clients.put(clients.size() + 1, newClient);
                                        players.put(player, newClient);
                                        PacketManager.sendS2C(new NameS2CPacket(player.getName()), newClient, Throwable::printStackTrace);
                                        PacketManager.sendS2C(new StartS2CPacket(), newClient, Throwable::printStackTrace);
                                        logger.log().info("[Server]: New Client rejoined (" + player.getName() + ")");
                                        //TODO: send necessary information
                                        PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
                                        PacketManager.sendS2C(new UpdatePositionS2CPacket(false), PacketManager.all(), Throwable::printStackTrace);
                                        PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
                                        PacketManager.sendS2C(new UpdateFreeParkingS2CPacket(gameData.getFreeParkingAmount()), PacketManager.all(), Throwable::printStackTrace);
                                        events.onRejoin();
                                        return;
                                    }
                                }
                                throw new IllegalStateException();
                            } catch (Exception e) {
                                PacketManager.sendS2C(new DisconnectS2CPacket(DisconnectReason.SERVER_FULL), PacketManager.all(), Throwable::printStackTrace);
                                e.printStackTrace();
                            }
                        }).start();
                    } else if (acceptNewClients && clients.size() < 6 && serverState == ServerState.LOBBY) {
                        clients.put(clients.size() + 1, newClient);
                        Player player = newServerPlayer();
                        players.put(player, newClient);
                        List<Color> colors = COLORS.keySet().stream().filter(color -> players.keySet().stream().map(Player::getColor).noneMatch(color::equals)).toList();
                        player.setColor(colors.get(new Random().nextInt(colors.size())));
                        PacketManager.sendS2C(new NameS2CPacket(player.getName()), newClient, Throwable::printStackTrace);
                        PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
                        logger.log().info("[Server]: New Client accepted (" + player.getName() + ")");
                    } else {
                        logger.log().info("[Server]: New Client declined");
                        if (pause)
                            PacketManager.sendS2C(new DisconnectS2CPacket(DisconnectReason.SERVER_CLOSED), PacketManager.all(), Throwable::printStackTrace);
                        else if (serverState == ServerState.GAME)
                            PacketManager.sendS2C(new DisconnectS2CPacket(DisconnectReason.GAME_RUNNING), PacketManager.all(), Throwable::printStackTrace);
                        else if (clients.size() >= 10)
                            PacketManager.sendS2C(new DisconnectS2CPacket(DisconnectReason.SERVER_FULL), PacketManager.all(), Throwable::printStackTrace);
                        else PacketManager.sendS2C(new DisconnectS2CPacket(DisconnectReason.UNKNOWN), PacketManager.all(), Throwable::printStackTrace);
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    logger.log().severe("[Server]: Server crashed due to an Exception:\r\n" + e.getMessage());
                    close();
                    return;
                }
                acceptNewClients = serverState == ServerState.LOBBY;
                if (pause) acceptNewClients = false;
                try {
                    Thread.sleep(10);
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
                name = "Spieler " + i;
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
                                if(Thread.interrupted()) return;
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
                    if(!thread.isAlive()) {
                        thread.start();
                    }
                });
            }
            int status = waitForRejoin.size();
            waitForRejoin.removeIf(player -> !players.containsKey(player) || players.get(player) != null);
            waitForRejoin.addAll(players.keySet().stream().filter(player -> !waitForRejoin.contains(player) && players.get(player) == null).toList());
            if (status != waitForRejoin.size()) {
                PacketManager.sendS2C(new RejoinStatusS2CPacket(waitForRejoin.stream().map(Player::getName).toList()), PacketManager.all(), Throwable::printStackTrace);
            }
            if(hostJoined && players.keySet().stream().filter(player -> players.get(player) != null).map(Player::getName).noneMatch(name -> name.equals(host))) close();
            if(players.keySet().stream().filter(player -> players.get(player) != null).map(Player::getName).anyMatch(name -> name.equals(host))) hostJoined = true;
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
                    PacketManager.sendS2C(new PingS2CPacket(), client, Throwable::printStackTrace);
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

    public Server(int port, Consumer<Boolean> success) throws IOException{
        logger.log().info("[Server]: Initialing server...");

        serverSettings = new ServerSettings(false, false);

        try {
            server = new ServerSocket(port);
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1199);
                registry.rebind("Server", this);
            } catch(Exception ignored) {
                try {
                    registry = LocateRegistry.createRegistry(1199);
                    registry.rebind("Server", this);
                } catch(Exception e) {
                    logger.log().warning("[Server]: Failed to start server\r\n" + e.getMessage());
                    close();
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            success.accept(false);
            new Thread(() -> {
                JOptionPane.showMessageDialog(null, "Der Server konnte nicht gestartet werden:\n" + e.getMessage() + "\nDu kannst daher nur anderen Servern beitreten.", "Error", JOptionPane.WARNING_MESSAGE);
            }).start();
            return;
        }

        success.accept(true);

        connectionThread.start();
        requestThread.start();
        pingThread.start();
    }

    public void open(ServerSettings serverSettings) {
        serverState = ServerState.LOBBY;
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
        serverState = ServerState.LOBBY;
        acceptNewClients = true;
        logger.log().info("[Server]: Listening for new clients...");
    }

    public void close() {
        events.onGameStop();
        players.clear();
        acceptNewClients = false;
        hostJoined = false;
        host = null;
        List<Socket> list = new ArrayList<>();
        clients.forEach((id, client) -> list.add(client));
        for (Socket client : list) {
            PacketManager.sendS2C(new DisconnectS2CPacket(DisconnectReason.SERVER_CLOSED), PacketManager.all(), Throwable::printStackTrace);
            kick(client, DisconnectReason.SERVER_CLOSED);
        }
        pause = true;
        serverState = ServerState.CLOSED;
        logger.log().warning("[Server]: Server closed...");
    }

    public void remove(String name) {
        List<Player> list = players.keySet().stream().filter(player -> players.get(player) == null && player.getName().equals(name)).toList();
        if (list.size() == 1) {
            players.remove(list.get(0));
            Field.purchasables().stream().filter(purchasable -> list.get(0).getName().equals(purchasable.getOwner())).forEach(purchasable -> purchasable.setOwner(null));
            events().onTryNextRound(name);
        }
    }

    public void kick(Socket client, DisconnectReason reason) {
        if(!clients.containsValue(client)) return;
        int id = MapUtils.key(clients, client).orElseThrow();
        PacketManager.sendS2C(new DisconnectS2CPacket(reason), client, Throwable::printStackTrace);
        for(int i = id; i < clients.size(); i++) {
            clients.replace(i, clients.get(i + 1));
        }
        clients.remove(clients.size());
        for(Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getValue() == client) {
                players.replace(entry.getKey(), null);
                if(serverState == ServerState.LOBBY) players.remove(entry.getKey());
                break;
            }
        }
        logger.log().warning("[Server]: Kicked client");
        PacketManager.sendS2C(new UpdatePositionS2CPacket(false), PacketManager.all(), Throwable::printStackTrace);
        PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
        PacketManager.sendS2C(new UpdateFreeParkingS2CPacket(gameData.getFreeParkingAmount()), PacketManager.all(), Throwable::printStackTrace);
        if(clients.isEmpty()) close();
    }

    private void messageReceived(String value, Socket client) {
        Message message;
        try {
            message = Json.toObject(value, Message.class);
            PacketManager.handle(message, new ServerSide(this, client));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            logger.log().severe("[Server]: Server crashed due to an Exception:\r\n" + e.getMessage());
            close();
            throw new RuntimeException(e);
        }
    }

    public void ping(Socket source, long delay) {
        pingCheck.put(source, true);
        String name = "unknown";
        for(Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getValue() == source) name = entry.getKey().getName();
        }
        if(delay < 2500) logger.log().fine("[Server]: Ping to " + name + " is " + delay + "ms");
        else logger.log().warning("[Server]: Ping to " + name + " is " + delay + "ms");
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public ServerSettings getServerSettings() throws RemoteException {
        return serverSettings;
    }

    public ArrayList<Player> getPlayersServerSide() {
        ArrayList<Player> list = new ArrayList<>(players.keySet());
        list.removeIf(serverPlayer -> players.get(serverPlayer) == null);
        return list;
    }

    @Override
    public ArrayList<Player> getPlayers() throws RemoteException {
        ArrayList<Player> list = new ArrayList<>(players.keySet());
        list.removeIf(serverPlayer -> players.get(serverPlayer) == null);
        return list;
    }

    public Player getPlayerServerSide(String name) {
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getKey().getName().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Player getPlayer(String name) throws RemoteException {
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
    public synchronized boolean changeName(String oldName, String newName) throws RemoteException {
        if(serverState != ServerState.LOBBY) return false;
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
    public synchronized boolean changeColor(String name, Color color) throws RemoteException {
        if(serverState != ServerState.LOBBY) return false;
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getKey().getColor() == color) return false;
        }
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
            if(entry.getKey().getName().equals(name)) {
                entry.getKey().setColor(color);
                PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IPurchasable> getPurchasables() throws RemoteException {
        return Field.purchasables();
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
        PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), player -> true, e -> {});
        return true;
    }

    @Override
    public boolean acceptsNewClient() throws RemoteException {
        return acceptNewClients || !waitForRejoin.isEmpty();
    }

    @Override
    public boolean isHost(String name) throws RemoteException {
        return name.equals(host);
    }

    @Override
    public boolean stopped() throws RemoteException {
        return pause;
    }

    public Socket socket(Player player) {
        return players.get(player);
    }

    public void start() {
        if(!pause && serverState == ServerState.LOBBY && (players.size() >= events().minPlayers())) {
            events.prepareGame();
            gameData = new GameData();
            PacketManager.sendS2C(new StartS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdatePurchasablesS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdatePositionS2CPacket(false), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new UpdateFreeParkingS2CPacket(gameData.getFreeParkingAmount()), PacketManager.all(), Throwable::printStackTrace);
            PacketManager.sendS2C(new CommunityCardS2CPacket(null, new ArrayList<>(), new ArrayList<>(), CommunityCard.unusedSize()), PacketManager.all(), Throwable::printStackTrace);
            serverState = ServerState.GAME;
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                if(serverState == ServerState.GAME) events.onGameStart(players.keySet().stream().map(Player::getName).toList());
            }).start();
        }
    }

    public void updatePosition(boolean anim) {
        PacketManager.sendS2C(new UpdatePositionS2CPacket(anim), PacketManager.all(), Throwable::printStackTrace);
    }

    public void updateFreeParking() {
        PacketManager.sendS2C(new UpdateFreeParkingS2CPacket(gameData.getFreeParkingAmount()), PacketManager.all(), Throwable::printStackTrace);
    }

    public void updatePlayerData() {
        PacketManager.sendS2C(new UpdatePlayerDataS2CPacket(), PacketManager.all(), Throwable::printStackTrace);
    }

    public GameData gameData() {
        return gameData;
    }
    public Events events() {
        return events;
    }
    public Events.Factory<?> getEventsType() {
        return eventsType;
    }

    public void setEvents(Events events) {
        if(serverState == ServerState.LOBBY) this.events = events;
    }
    public void setEventsType(Events.Factory<?> eventsType) {
        if(serverState == ServerState.LOBBY) this.eventsType = eventsType;
    }
}
