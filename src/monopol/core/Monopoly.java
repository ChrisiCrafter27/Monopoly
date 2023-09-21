package monopol.core;

import monopol.annotations.AnnotationManager;
import monopol.annotations.Autostart;
import monopol.screen.PrototypeMenu;
import monopol.server.Server;
import monopol.server.ServerSettings;
import monopol.utils.ProjectStructure;

import java.io.IOException;
import java.rmi.RemoteException;

public class Monopoly {

    public static final Monopoly INSTANCE = new Monopoly();
    private final Server server;
    GameState state;

    private Monopoly() {
        try {
            server = new Server(25565);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setState(GameState gameState) {
        state = gameState;
    }
    public GameState getState() {
        return state;
    }
    public void openServer(ServerSettings settings) {
        server.open(settings);
    }
    public void setHost(String name) {
        server.setHost(name);
    }
    public void closeServer() {
        server.close();
    }

    public static void main(String[] args) {
        INSTANCE.state = GameState.MAIN_MENU;
        AnnotationManager.setup();
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }

    @Autostart
    public static void printStartupInfo() {
        System.out.println("Starting Monopoly...");
        ProjectStructure.printProjectStructureAsTree(false);
        System.out.println("Done!");
    }

    @Autostart
    public static void test() {

    }
}