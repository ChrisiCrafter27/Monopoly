package monopol.common.message;

import monopol.common.data.IPurchasable;
import monopol.common.data.Player;
import monopol.common.utils.ServerSettings;

import java.awt.*;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IServer extends Remote {
    ServerSettings getServerSettings() throws RemoteException;
    ArrayList<Player> getPlayers() throws RemoteException;
    Player getPlayer(String name) throws RemoteException;
    String getIp() throws RemoteException;
    void kick(String name, DisconnectReason reason) throws RemoteException;
    boolean changeName(String oldName, String newName) throws RemoteException;
    boolean changeColor(String name, Color color) throws RemoteException;
    boolean trade(String player1, String player2, ArrayList<IPurchasable> offer1, ArrayList<IPurchasable> offer2, int money1, int money2) throws RemoteException;
    boolean acceptsNewClient() throws RemoteException;
    boolean isHost(String name) throws RemoteException;
    boolean stopped() throws RemoteException;
    boolean enoughPlayers() throws RemoteException;
}
