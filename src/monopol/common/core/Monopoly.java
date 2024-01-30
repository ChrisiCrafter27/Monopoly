package monopol.common.core;

import monopol.client.screen.PrototypeMenu;
import monopol.server.Server;
import monopol.server.ServerSettings;
import monopol.common.utils.ProjectStructure;

import java.io.IOException;

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
    public Server server() {
        return server;
    }
    public void closeServer() {
        server.close();
    }

    public static void main(String[] args) {
        INSTANCE.state = GameState.MAIN_MENU;
        printStartupInfo();
        PrototypeMenu menu = new PrototypeMenu();
        menu.prepareMenu();
    }

    public static void printStartupInfo() {
        System.out.println("Starting Monopoly...");
        ProjectStructure.printProjectStructureAsTree(false);
        System.out.println("Done!");
    }
}