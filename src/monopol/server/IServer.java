package monopol.server;

import monopol.data.IPurchasable;
import monopol.message.MessageType;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface IServer extends Remote {
    ServerSettings getServerSettings() throws RemoteException;
    ArrayList<ServerPlayer> getServerPlayers() throws RemoteException;
    ServerPlayer getServerPlayer(String name) throws RemoteException;
    String getIp() throws RemoteException;
    void kick(String name, DisconnectReason reason) throws RemoteException;
    boolean changeName(String oldName, String newName) throws RemoteException;
    void sendMessage(String name, MessageType type, Object[] value) throws IOException;
    void sendMessage(String name, MessageType type, Object value) throws IOException;
    HashMap<IPurchasable, String> getOwnerMap() throws RemoteException;
    boolean trade(String player1, String player2, ArrayList<IPurchasable> offer1, ArrayList<IPurchasable> offer2, int money1, int money2) throws RemoteException;
    boolean triggerEvent(String methodName, Object... args) throws RemoteException;
    boolean acceptsNewClient() throws RemoteException;
    boolean isHost(String name) throws RemoteException;
    boolean stopped() throws RemoteException;
    void start() throws IOException;
}
