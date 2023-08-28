package monopol.server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IServer extends Remote {
    public ServerSettings getServerSettings() throws RemoteException;
    public ArrayList<ServerPlayer> getServerPlayers() throws RemoteException;
    public String getIp() throws RemoteException;
    public void kick(String name, DisconnectReason reason) throws RemoteException;
    public boolean changeName(String oldName, String newName) throws RemoteException;
    public boolean acceptsNewClient() throws RemoteException;
    public boolean stopped() throws RemoteException;
    public void start() throws IOException;
}
