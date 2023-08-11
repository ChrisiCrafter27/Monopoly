package monopol.test;

import monopol.client.Client;
import monopol.server.ServerLogger;
import monopol.server.rules.Events;
import monopol.server.rules.StandardEvents;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class TestF {
    public Events eventObject;

    public TestF(Events eventObject) {
        this.eventObject = eventObject;
    }

    public void esWurdeGeradeAufDenKaufButtonGedrückt() {
        //eventObject.onPurchaseCard();
    }

    public static void main(String[] args) throws NotBoundException, RemoteException {
        ServerLogger.INSTANCE.getLogger().finer("Everything fine");
        ServerLogger.INSTANCE.getLogger().config("Here is some config");
        ServerLogger.INSTANCE.getLogger().info("This is an information");
        ServerLogger.INSTANCE.getLogger().warning("Oh no, there is a problem");
        ServerLogger.INSTANCE.getLogger().severe("This probably causes a crash");

        //TestF f = new TestF(new StandardEvents());

        //Client client = new Client("localhost", 25565);
        //client.triggerEvent().printSth("Print this!");
    }
}
