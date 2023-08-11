package monopol.server.rules;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Events extends UnicastRemoteObject implements EventsInterface {

    public Events() throws RemoteException {}

    @Override
    public void printSth(String value) {
        System.out.println(value);
    }
}
