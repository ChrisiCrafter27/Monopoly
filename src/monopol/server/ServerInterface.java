package monopol.server;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public interface ServerInterface extends Remote {
    public ServerSettings getServerSettings() throws RemoteException;
    public ArrayList<ServerPlayer> getServerPlayers() throws RemoteException;
    public void kick(String name, DisconnectReason reason) throws RemoteException;
    public boolean changeName(String oldName, String newName) throws RemoteException;
}
