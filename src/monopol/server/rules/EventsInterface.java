package monopol.server.rules;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventsInterface extends Remote {
    void printSth(String value) throws RemoteException;
}
