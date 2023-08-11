package monopol.server.rules;

import java.rmi.Remote;

public interface EventsInterface extends Remote {
    void printSth(String value);
}
