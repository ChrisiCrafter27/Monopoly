package monopol.server;

import monopol.constants.IPurchasable;
import monopol.message.MessageType;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface IServer extends Remote {
    public ServerSettings getServerSettings() throws RemoteException;
    public ArrayList<ServerPlayer> getServerPlayers() throws RemoteException;
    public ServerPlayer getServerPlayer(String name) throws RemoteException;
    public String getIp() throws RemoteException;
    public void kick(String name, DisconnectReason reason) throws RemoteException;
    public boolean changeName(String oldName, String newName) throws RemoteException;
    public void sendMessage(String name, MessageType type, Object[] value) throws IOException;
    public void sendMessage(String name, MessageType type, Object value) throws IOException;
    public HashMap<IPurchasable, String> getOwnerMap() throws RemoteException;
    public boolean trade(String player1, String player2, ArrayList<IPurchasable> offer1, ArrayList<IPurchasable> offer2, int money1, int money2) throws RemoteException;
    public boolean acceptsNewClient() throws RemoteException;
    public boolean isHost(String name) throws RemoteException;
    public boolean stopped() throws RemoteException;
    public void start(String host) throws IOException;
}
